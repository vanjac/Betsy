package betsy;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import betsy.grammar.*;
import betsy.grammar.StructureTag.CategoryTag;
import static betsy.grammar.StructureTag.*;
import betsy.memory.*;
import betsy.vocab.*;

/**
 * Betsy the Chatbot. An implementation of a Bot.
 * @author jacob
 *
 */
public class BetsyBot implements Bot {
	
	private static final boolean DEBUG_LOG = false;
	
	private static final String WELCOME_MESSAGE =
			"Hello! I'm Betsy.\n"
			+ "Please speak to me in simple, complete sentences,"
			+ " with correct grammar, capitalization, and punctuation.";
	private static final String CLOSING_MESSAGE = "Goodbye!";
	
	private static final String PARSER_MODEL =
			"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	
	private static final String KNOWLEDGE_FILE = "betsyKnowledge.txt";
	
	// Phrase lists
	private static final String[] pError = {
		"ERROR!"
	};
	private static final String[] pUnable = {
		"I am unable to %s.", "I am not able to %s.",
		"To %s is beyond my capabilities.", "I don't know how to %s",
		"I can't %s"
	};
	private static final String[] pClarifyFragment = {
		"What about %s?", "What about it?", "What do you mean?"
	};
	private static final String[] pGenericInterjectionResponse = {
		"Indeed.", "I agree.", "Quite.", "Truly."
	};
	private static final String[] pDontKnow = {
		"I don't know %s.", "I'm not sure about %s."
	};
	private static final String[] pTalkToUnknownPerson = {
		"I can only speak to you.", "I can't talk to %s.", "I don't know %s.",
		"I don't know who %s is."
	};
	private static final String[] pTalkToYourself = {
		"I don't often talk to myself.", "I avoid talking to myself.",
		"I value my sanity."
	};
	private static final String[] pTellMeWhat = {
		"What should I tell you?", "Tell you what?"
	};
	private static final String[] pTryWhat = {
		"What should I try?", "Try what?"
	};
	private static final String[] pBeResponse = {
		"I can be what I want to be.", "Don't tell me how to live my life."
	};
	private static final String[] pDoResponse = {
		"I can do what I want.", "Don't tell me how to live my life."
	};
	
	private final LexicalizedParser parser;
	private final PrintStream logOut;
	
	private QuestionMemory memory;
	private Context context;
	
	private final SentenceConstructor constructor;
	
	private final Formatter formatter;
	private final StringBuilder formatStringBuilder;
	
	private String response;
	
	private List<String> knowledge;
	
	public BetsyBot(PrintStream log) {
		logOut = log;
		formatStringBuilder = new StringBuilder();
		formatter = new Formatter(formatStringBuilder);
		constructor = new RecursiveSentenceConstructor(logOut);
		logOut.println("  Initializing parser...");
		parser = LexicalizedParser.loadModel(PARSER_MODEL);
		logOut.println("  Loading knowledge...");
		Names.loadNames();
		Path knowledgePath = Paths.get(KNOWLEDGE_FILE);
		try {
			knowledge = Files.readAllLines(knowledgePath);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		logOut.println("  Loading dictionary...");
		Vocab.init();
		logOut.println("  Done.");
	}
	

	@Override
	public String getName() {
		return "Betsy";
	}

	@Override
	public String init(boolean generateResponse) {
		logOut.println("Initializing bot...");
		context = new Context();
		memory = new ScoredQuestionMemory(logOut);
		
		logOut.println("  Interpreting knowledge...");
		for(String fact : knowledge) {
			interpret(fact, false);
			//clear context each time
			context = new Context();
		}
		logOut.println("  Done.");
		logOut.println("\n\n");
		if(!DEBUG_LOG)
			BetsyMain.logger.clearTree();
		
		if(generateResponse)
			return WELCOME_MESSAGE;
		else
			return null;
	}

	@Override
	public String interpret(String text, boolean generateResponse) {
		logOut.println("--------------------------------\n");
		BetsyMain.logger.clearTree();
		String[] tokens = TokenUtils.tokenize(text);
		tokens = Vocab.replaceContractions(tokens);
		
		List<CoreLabel> words = Sentence.toCoreLabelList(tokens);
		
		List<List<CoreLabel>> sentences =
				TokenUtils.splitSentences(words);
		
		logOut.println("User said " + sentences.size() + " sentence(s)");
		logOut.println();
		
		response = randomPhrase(pError);
		
		int i = 0;
	    for(List<CoreLabel> sentence : sentences) {
	    	boolean lastSentence = i == sentences.size() - 1;
	    	interpretSentence(sentence, lastSentence && generateResponse);
	    	i++;
	    }
	    
	    logOut.println();
	    
	    return response;
	}
	
	private WordTree<StructureTag> interpretSentence(List<CoreLabel> tokens,
			boolean respond) {
		logOut.println("User said: " + TokenUtils.detokenize(tokens));
		
		//check for phrases like How are you doing, Betsy?
		if(respond) {
			int lastTokenIndex = tokens.size()-1;
			String lastToken = tokens.get(lastTokenIndex).toString();
			if(lastToken.equals("?")) {
				lastTokenIndex--;
				lastToken = tokens.get(lastTokenIndex).toString();
				
				if(lastToken.toLowerCase().equals("betsy")) {
					tokens.remove(lastTokenIndex);
					lastTokenIndex--;
					lastToken = tokens.get(lastTokenIndex).toString();
					if(TokenUtils.isPunctuation(lastToken.charAt(0)))
						tokens.remove(lastTokenIndex);
				}
			}
		}
		
		Tree tree = parser.apply(tokens);
	    WordTree<Tag> wordTree = Tag.fromTree(tree);
	    if(DEBUG_LOG) {
	    	logOut.println(wordTree.toString());
	    	logOut.println();
	    }
	    SentenceStructureParser parse = new RecursiveStructureParser(logOut);
		WordTree<StructureTag> parseTree = parse.parseTree(wordTree);
	    
	    BetsyMain.logger.addTree(parseTree);
	    
	    List<WordTree<StructureTag>> phrases = splitPhrases(parseTree);
	    logOut.println("This sentence has " + phrases.size() + " phrase(s).");
	    int i = 0;
	    for(WordTree<StructureTag> phrase : phrases) {
	    	boolean respondToPhrase = (i == phrases.size() - 1) && respond;
	    	String response = interpretPhrase(phrase, respondToPhrase);
	    	if(respondToPhrase)
	    		this.response = response;
	    	i++;
	    }
	    
	    logOut.println();
	    return parseTree;
	}
	
	@SuppressWarnings("incomplete-switch")
	private String interpretPhrase(WordTree<StructureTag> tree,
			boolean respond) {
		
		context.replaceContext(tree);
		context.interpretContext(tree);
		logOut.println("Current context:\n" +
    			context.getContextDescription(constructor));
		
		String phrase = constructor.constructSentence(tree, false);
		String sentence = constructor.constructSentence(tree, true);
		String response = randomPhrase(pError);
		
		switch(tree.getType()) {
		case STATEMENT:
			logOut.println("User said a statement: " + sentence);
			response = sentence;
			memory.storeStatement(tree);
			break;
		case COMMAND:
			logOut.println("User told me to: " + sentence);
			response = interpretCommand(tree.getType(ACTION)
					.getType(VERB_PHRASE));
			break;
		case QUESTION:
		case YES_NO:
			logOut.println("User asked: " + sentence);
			WordTree<StructureTag> bestMatch = memory.filterQuestion(tree);
			if(bestMatch == null) {
				logOut.println("No good answer found.");
				response = format(randomPhrase(pDontKnow), phrase);
			} else {
				response = constructor.constructSentence(bestMatch, true);
				logOut.println("Best answer: " + response);
			}
			break;
		case INTERJECTION_PHRASE:
			logOut.println("Interjection...");
			response = randomPhrase(pGenericInterjectionResponse);
			for(WordTree<StructureTag> child : tree.getChildren()) {
				String interjectionResponse =
						interpretInterjection(child.getWord());
				if(interjectionResponse != null) {
					logOut.println("Response to " + child.getWord() +
							": " + interjectionResponse);
					response = interjectionResponse;
				}
			}
			break;
		case FRAGMENT_NOUN:
		case FRAGMENT_ADJECTIVE:
		case FRAGMENT_ADVERB:
			//TODO: more intelligent response
			logOut.println("Fragment");
			
			//check for interjection words
			String interjectionResponse = tryInterjectionRecursive(tree);
			if(interjectionResponse != null) {
				response = interjectionResponse;
			} else {
				response = format(randomPhrase(pClarifyFragment), phrase);
			}
			break;
		case QUESTION_FRAGMENT:
			response = sentence;
			break;
		}
		
//		if(tree.hasType(StructureTag.SUBJECT)) {
//			WordTree<StructureTag> subject =
//					tree.getType(StructureTag.SUBJECT);
//			logOut.println("Matching statements:");
//			int i = 0;
//			for(WordTree<StructureTag> matched
//					: memory.filter(subject)) {
//				logOut.println("  " + matched.wordListToString());
//				i++;
//			}
//			if(i == 0)
//				logOut.println("  Nothing.");
//		} else {
//			logOut.println("No subject.");
//		}
    	
    	
    	if(respond)
    		return response;
    	else
    		return null;
	}
	
	private String interpretCommand(WordTree<StructureTag> verbPhrase) {
		String verb;
		try {
			verb = verbPhrase.getType(VERB).getWord();
		} catch (NullPointerException e) {
			return randomPhrase(pError);
		}
		
		if(verb.equals("be")) {
			return randomPhrase(pBeResponse);
		}
		if(verb.equals("have")) {
			
		}
		if(verb.equals("do")) {
			return randomPhrase(pDoResponse);
		}
		if(verb.equals("think")) {
			return format(randomPhrase(pUnable), verb);
		}
		if(verb.equals("tell") || verb.equals("show") || verb.equals("give")
				|| verb.equals("find") || verb.equals("say")
				|| verb.equals("write") || verb.equals("read")) {
			String indirectObjectResponse =
					checkForInvalidIndirectObject(verbPhrase);
			if(indirectObjectResponse != null)
				return indirectObjectResponse;
			WordTree<StructureTag> object = verbPhrase.getType(OBJECT);
			if(object == null)
				return randomPhrase(pTellMeWhat);
			if(object.numChildren() == 0)
				return randomPhrase(pTellMeWhat);
			WordTree<StructureTag> objectChild = object.getChild(0);
			
			//construct a question tree
			WordTree<StructureTag> question = new WordTree<>(QUESTION);
			WordTree<StructureTag> subject = new WordTree<>(SUBJECT);
			question.addChild(subject);
			subject.addChild(objectChild.clone());
			WordTree<StructureTag> action = new WordTree<>(ACTION);
			question.addChild(action);
			WordTree<StructureTag> actionVerbPhrase =
					new WordTree<>(VERB_PHRASE);
			action.addChild(actionVerbPhrase);
			actionVerbPhrase.addChild(new WordTree<>(VERB, "be"));
			actionVerbPhrase.addChild(new WordTree<>(TENSE_TIME, "PRESENT"));
			actionVerbPhrase.addChild(new WordTree<>(TENSE_FRAME, "SIMPLE"));
			WordTree<StructureTag> actionObject = new WordTree<>(OBJECT);
			actionVerbPhrase.addChild(actionObject);
			WordTree<StructureTag> actionObjectNounPhrase
				= new WordTree<>(NOUN_PHRASE);
			actionObject.addChild(actionObjectNounPhrase);
			actionObjectNounPhrase.addChild(
					new WordTree<>(QUESTION_PRONOUN, "what"));
			return interpretPhrase(question, true);
		}
		if(verb.equals("ask")) {
			String indirectObjectResponse =
					checkForInvalidIndirectObject(verbPhrase);
			if(indirectObjectResponse != null)
				return indirectObjectResponse;
		}
		if(verb.equals("try") || verb.equals("keep")) {
			if(!verbPhrase.hasType(OBJECT)) {
				return randomPhrase(pTryWhat);
			}
			WordTree<StructureTag> object = verbPhrase.getType(OBJECT);
			if(object.numChildren() == 0) {
				return randomPhrase(pTryWhat);
			}
			if(!object.hasType(VERB_PHRASE)) {
				return format(randomPhrase(pUnable), verb);
			}
			WordTree<StructureTag> tryVerbPhrase = object.getType(VERB_PHRASE);
			
			//construct a command tree
			WordTree<StructureTag> command = new WordTree<>(COMMAND);
			WordTree<StructureTag> action = new WordTree<>(ACTION);
			command.addChild(action);
			action.addChild(tryVerbPhrase);
			return interpretPhrase(command, true);
		}
		if(verb.equals("believe")) {
			
		}
		if(verb.equals("talk")) {
			
		}
		if(verb.equals("let")) {
			
		}
		if(verb.equals("remember")) {
			
		}
		if(verb.equals("stop")) {
			return randomPhrase(pDoResponse);
		}
		if(verb.equals("describe")) {
			
		}
		
		String interpretInterjection = interpretInterjection(verb);
		if(interpretInterjection != null)
			return interpretInterjection;
		
		return format(randomPhrase(pUnable), verb);
	}
	
	/**
	 * Make sure the user isn't asking Betsy to, e.g. talk to somebody besides
	 * the user.
	 */
	private String checkForInvalidIndirectObject(
			WordTree<StructureTag> verbPhrase) {
		if(verbPhrase.hasType(INDIRECT_OBJECT)) {
			WordTree<StructureTag> nounPhrase =
					verbPhrase.getType(INDIRECT_OBJECT)
					.getType(NOUN_PHRASE);
			if(nounPhrase != null) {
				if(nounPhrase.hasLeaf(PRONOUN, "me")
						|| nounPhrase.hasLeaf(PRONOUN, "myself")) {
					return randomPhrase(pTalkToYourself);
				}
				if(!nounPhrase.hasLeaf(PRONOUN, "you")
						&& !nounPhrase.hasLeaf(PRONOUN, "yourself")) {
					return format(randomPhrase(pTalkToUnknownPerson),
							constructor.constructSentence(nounPhrase,
							false));
				}
			}
		}
		return null;
	}
	
	private String tryInterjectionRecursive(WordTree<StructureTag> tree) {
		if(tree.isLeaf()) {
			String response = interpretInterjection(tree.getWord());
			if(response != null) {
				logOut.println("Response to " + tree.getWord() +
						": " + response);
			}
			return response;
		} else {
			for(WordTree<StructureTag> child : tree.getChildren()) {
				String response = tryInterjectionRecursive(child);
				if(response != null)
					return response;
			}
			return null;
		}
	}
	
	private String interpretInterjection(String word) {
		if(word.equals("hi") || word.equals("hello") || word.equals("hey"))
			return "Hello!";
		if(word.equals("suh") || word.equals("asuh"))
			return "asuh dude";
		
		return null;
	}
	
	private List<WordTree<StructureTag>> splitPhrases(
			WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> phrases = new ArrayList<>();
		
		StructureTag tag = tree.getType();
		
		if(tag.isA(CategoryTag.CONTAINS_PHRASE))
			for(WordTree<StructureTag> child : tree.getChildren())
				phrases.addAll(splitPhrases(child));
		else if(tag.isA(CategoryTag.PHRASE))
			phrases.add(tree);
		
		return phrases;
	}
	
	private String randomPhrase(String[] list) {
		return list[(int)Math.floor(Math.random() * list.length)];
	}
	
	private String format(String format, Object... args) {
		if(formatStringBuilder.length() > 0)
			formatStringBuilder.delete(0, formatStringBuilder.length());
		formatter.format(format, args);
		return formatStringBuilder.toString();
	}

	@Override
	public String close(boolean generateResponse) {
		if(generateResponse)
			return CLOSING_MESSAGE;
		else
			return null;
	}
	
}
