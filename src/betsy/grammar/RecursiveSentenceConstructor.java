package betsy.grammar;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;
import static betsy.grammar.StructureTag.*;
import static betsy.grammar.StructureTag.CategoryTag.*;
import betsy.vocab.Vocab;

/**
 * An implementation of a SentenceConstructor. Interprets the tree recursively,
 * with rules for each StructureTag for constructing the phrase.
 * @author jacob
 *
 */
public class RecursiveSentenceConstructor implements SentenceConstructor {
	
	private PrintStream logOut;
	private boolean addPunctuation;
	
	public RecursiveSentenceConstructor(PrintStream logOut) {
		this.logOut = logOut;
	}
	
	@Override
	public String constructSentence(WordTree<StructureTag> tree,
			boolean addPunctuation) {
		this.addPunctuation = addPunctuation;
		String sentence = construct(tree);
		if(sentence.isEmpty())
			return "";
		
		if(addPunctuation) {
			// capitalize first letter
			sentence = Character.toUpperCase(sentence.charAt(0))
					+ sentence.substring(1);
			// add period if there isn't already a ?
			if(sentence.charAt(sentence.length() - 1) != '?')
				sentence += ".";
		}
		return sentence;
	}
	
	private String construct(WordTree<StructureTag> tree) {
		if(tree.getType().isA(WORD)) {
			return tree.getWord();
		}
		if(!tree.isLeaf() && tree.numChildren() == 0)
			return "";
		if(tree.getType().isA(IGNORED)) {
			return "";
		}
		if(tree.getType().equals(POSSESSOR)) {
			return Vocab.makePossessive(construct(tree.getChild(0)));
		}
		if(tree.getType().isA(SINGLE_CHILD)) {
			return construct(tree.getChild(0));
		}
		
		
		switch(tree.getType()) {
		case CONJUNCTION_PHRASE:
			return constructConjunctionPhrase(tree);
		case STATEMENT:
			return constructStatement(tree);
		case QUESTION:
			return constructQuestion(tree);
		case YES_NO:
			String statement = constructStatement(tree);
			if(addPunctuation)
				statement += "?";
			return statement; //TODO: YES_NO!
		// INTERJECTION_PHRASE: fallback
		case NOUN_PHRASE:
			return constructNounPhrase(tree);
		case VERB_PHRASE:
			return constructVerbPhrase(tree);
		case ADJECTIVE_PHRASE:
			return constructAdjectivePhrase(tree);
		case ADVERB_PHRASE:
			return constructAdverbPhrase(tree);
		case PREPOSITION_PHRASE:
			return constructPrepositionPhrase(tree);
		case SUBORDINATING_CONJUNCTION_PHRASE:
			return constructSubordinatingConjunctionPhrase(tree);
		case PARTICLE_PHRASE:
			return constructParticlePhrase(tree);
		default:
			return constructFallback(tree);
		}
	}
	
	private String constructConjunctionPhrase(WordTree<StructureTag> tree) {
		List<String> conjunctions = new ArrayList<>();
		List<String> phrases = new ArrayList<>();
		
		for(WordTree<StructureTag> child : tree.getChildren())
			if(child.getType().equals(CONJUNCTION))
				conjunctions.add(construct(child));
			else 
				phrases.add(construct(child));
		
		int numConjunctions = conjunctions.size();
		int numPhrases = phrases.size();
		int maxNum = numConjunctions > numPhrases ?
				numConjunctions : numPhrases;
		
		//interleave phrases and conjunctions
		List<String> wordList = new ArrayList<>();
		for(int i = 0; i < maxNum; i++) {
			if(i < numPhrases)
				wordList.add(phrases.get(i));
			if(i < numConjunctions)
				wordList.add(conjunctions.get(i));
		}
		return wordList(wordList);
	}
	
	private String constructStatement(WordTree<StructureTag> tree) {
		List<String> wordList = new ArrayList<>();
		if(tree.hasType(SUBJECT))
			wordList.add(construct(tree.getType(SUBJECT)));
		if(tree.hasType(ACTION))
			wordList.add(construct(tree.getType(ACTION)));
		return wordList(wordList);
	}
	
	private String constructQuestion(WordTree<StructureTag> tree) {
		List<String> wordList = new ArrayList<>();
		if(tree.hasType(QUESTION_TYPE))
			wordList.add(construct(tree.getType(QUESTION_TYPE)));
//		if(tree.hasType(AUXILIARY_VERB))
//			wordList.add(construct(tree.getType(AUXILIARY_VERB)));
		if(tree.hasType(SUBJECT))
			wordList.add(construct(tree.getType(SUBJECT)));
		if(tree.hasType(ACTION))
			wordList.add(construct(tree.getType(ACTION)));
		String response = wordList(wordList);
		if(addPunctuation)
			response += "?";
		return response;
	}
	
	private String constructNounPhrase(WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> nouns = tree.getAllType(NOUN);
		nouns.addAll(tree.getAllType(PRONOUN));
		nouns.addAll(tree.getAllType(QUESTION_PRONOUN));
		nouns.addAll(tree.getAllType(REFERRING_PRONOUN));
		List<WordTree<StructureTag>> adjectivePhrases =
				tree.getAllType(ADJECTIVE_PHRASE);
		List<WordTree<StructureTag>> determiners =
				tree.getAllType(DETERMINER);
		determiners.addAll(tree.getAllType(QUESTION_DETERMINER));
		List<WordTree<StructureTag>> possessors =
				tree.getAllType(POSSESSOR);
		List<WordTree<StructureTag>> prepositionPhrases =
				tree.getAllType(PREPOSITION_PHRASE);
		List<WordTree<StructureTag>> subordinatingConjunctionPhrases =
				tree.getAllType(SUBORDINATING_CONJUNCTION_PHRASE);
		boolean isPlural = tree.hasType(PLURAL);
		
		boolean isSubject = false;
		if(tree.getParent() != null)
			isSubject = tree.getParent().getType().equals(SUBJECT);
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : determiners)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : possessors)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : adjectivePhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : nouns) {
			String noun = t.getWord();
			if(isSubject)
				noun = Vocab.makePronounSubject(noun);
			if(isPlural)
				noun = Vocab.makePlural(noun);
			wordList.add(noun);
		}
		for(WordTree<StructureTag> t : prepositionPhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : subordinatingConjunctionPhrases)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	private String constructVerbPhrase(WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> verbs = tree.getAllType(VERB);
		List<WordTree<StructureTag>> adverbPhrases =
				tree.getAllType(ADVERB_PHRASE);
		List<WordTree<StructureTag>> objects =
				tree.getAllType(OBJECT);
		List<WordTree<StructureTag>> indirectObjects =
				tree.getAllType(INDIRECT_OBJECT);
		List<WordTree<StructureTag>> prepositionPhrases =
				tree.getAllType(PREPOSITION_PHRASE);
		List<WordTree<StructureTag>> subordinatingConjunctionPhrases =
				tree.getAllType(SUBORDINATING_CONJUNCTION_PHRASE);
		List<WordTree<StructureTag>> particlePhrases =
				tree.getAllType(PARTICLE_PHRASE);
		List<WordTree<StructureTag>> questionAdverbs =
				tree.getAllType(QUESTION_ADVERB);
		
		String tenseTime = null;
		String tenseFrame = null;
		if(tree.hasType(TENSE_TIME))
			tenseTime = tree.getType(TENSE_TIME).getWord();
		if(tree.hasType(TENSE_FRAME))
			tenseFrame = tree.getType(TENSE_FRAME).getWord();
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : adverbPhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : verbs)
			wordList.add(Vocab.conjugateVerb(t.getWord(),
					tenseTime, tenseFrame));
		for(WordTree<StructureTag> t : indirectObjects)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : objects)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : particlePhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : prepositionPhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : subordinatingConjunctionPhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : questionAdverbs)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	private String constructAdjectivePhrase(WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> adjectives = tree.getAllType(ADJECTIVE);
		List<WordTree<StructureTag>> adverbPhrases =
				tree.getAllType(ADVERB_PHRASE);
		List<WordTree<StructureTag>> prepositionPhrases =
				tree.getAllType(PREPOSITION_PHRASE);
		
		boolean isComparative = tree.hasType(COMPARATIVE);
		boolean isSuperlative = tree.hasType(SUPERLATIVE);
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : adverbPhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : adjectives)
			if(isSuperlative)
				wordList.add(Vocab.makeSuperlative(t.getWord()));
			else if(isComparative)
				wordList.add(Vocab.makeComparative(t.getWord()));
			else
				wordList.add(t.getWord());
		for(WordTree<StructureTag> t : prepositionPhrases)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	private String constructAdverbPhrase(WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> adverbs = tree.getAllType(ADVERB);
		List<WordTree<StructureTag>> adverbPhrases =
				tree.getAllType(ADVERB_PHRASE);
		List<WordTree<StructureTag>> prepositionPhrases =
				tree.getAllType(PREPOSITION_PHRASE);
		
		boolean isComparative = tree.hasType(COMPARATIVE);
		boolean isSuperlative = tree.hasType(SUPERLATIVE);
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : adverbPhrases)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : adverbs)
			if(isSuperlative)
				wordList.add(Vocab.makeSuperlative(t.getWord()));
			else if(isComparative)
				wordList.add(Vocab.makeComparative(t.getWord()));
			else
				wordList.add(t.getWord());
		for(WordTree<StructureTag> t : prepositionPhrases)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	private String constructPrepositionPhrase(WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> prepositions =
				tree.getAllType(PREPOSITION);
		List<WordTree<StructureTag>> objects =
				tree.getAllType(OBJECT);
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : prepositions)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : objects)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	private String constructSubordinatingConjunctionPhrase(
			WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> conjunctions =
				tree.getAllType(CONJUNCTION);
		List<WordTree<StructureTag>> statements =
				tree.getAllType(STATEMENT);
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : conjunctions)
			wordList.add(construct(t));
		for(WordTree<StructureTag> t : statements)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	private String constructParticlePhrase(WordTree<StructureTag> tree) {
		List<WordTree<StructureTag>> particles =
				tree.getAllType(PARTICLE);
		
		List<String> wordList = new ArrayList<>();
		
		for(WordTree<StructureTag> t : particles)
			wordList.add(construct(t));
		
		return wordList(wordList);
	}
	
	//fallback for trees with no matches found
	private String constructFallback(WordTree<StructureTag> tree) {
		error("Fallback for tag " + tree.getType());
		if(tree.isLeaf()) {
			return tree.getWord();
		} else {
			List<String> strList = new ArrayList<>();
			for(WordTree<StructureTag> child : tree.getChildren()) {
				strList.add(construct(child));
			}
			return wordList(strList);
		}
	}
	
	private String wordList(List<String> words) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < words.size(); i++) {
			String s = words.get(i);
			sb.append(s);
			if(i != words.size() - 1 && !s.isEmpty())
				sb.append(" ");
		}
		return sb.toString();
	}
	
	private void error(String text) {
		logOut.println("WARNING: " + text);
	}

}
