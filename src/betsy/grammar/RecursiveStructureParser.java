package betsy.grammar;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static betsy.grammar.StructureTag.*;
import static betsy.grammar.StructureTag.CategoryTag.*;
import betsy.grammar.Tag.WordType;
import betsy.grammar.Tag.StructuralType;
import betsy.vocab.*;
import betsy.vocab.VerbInfo.VerbType;

/**
 * An implementation of SentenceStructureParser. It's recursive, as suggested
 * by the title. It's also complicated and ugly. It sometimes works.
 * @author jacob
 *
 */
public class RecursiveStructureParser implements SentenceStructureParser {
	
	private PrintStream logOut;
	
	private WordTree<StructureTag> structureTree;
	private int depth;
	
	public RecursiveStructureParser(PrintStream logOut) {
		this.logOut = logOut;
	}
	
	private static boolean isIgnoredTag(Tag t) {
		StructuralType type = t.getStructuralType();
		return type.equals(StructuralType.OTHER)
				|| type.equals(StructuralType.PUNCTUATION);
	}
	
	private static boolean isIncorrectTag(Tag tag) {
		return tag.equals(Tag.UNKNOWN) || tag.equals(Tag.X)
				|| tag.equals(Tag.FOREIGN_WORD) || tag.equals(Tag.SYMBOL)
				|| tag.equals(Tag.LIST_ITEM_MARKER);
	}
	
	@Override
	public WordTree<StructureTag> parseTree(WordTree<Tag> tree) {
		depth = 0;
		structureTree = new WordTree<StructureTag>(ROOT);
		parseTreeRecursive(tree);
		fixTreeRecursive(structureTree);
		return structureTree;
	}
	
	private void parseTreeRecursive(WordTree<Tag> tree) {
		int initialDepth = depth;
		boolean goDeeper = interpretTree(tree);
		if(!tree.isLeaf() && goDeeper) {
			for(WordTree<Tag> child : tree.getChildren()) {
				parseTreeRecursive(child);
			}
		}
		
		while(depth > initialDepth) {
			structureTree = structureTree.getParent();
			depth--;
		}
	}
	
	
	// fix any special-case parsing mistakes
	private void fixTreeRecursive(WordTree<StructureTag> tree) {
		fixTree(tree);
		if(!tree.isLeaf()) {
			for(WordTree<StructureTag> child : tree.getChildren()) {
				fixTreeRecursive(child);
			}
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	private void fixTree(WordTree<StructureTag> tree) {
		StructureTag tag = tree.getType();
		
		switch(tag) {
		case QUESTION:
			if(!tree.hasType(SUBJECT)) {
				// check first for indirect objects in the action,
				// then for normal objects
				if(tree.hasType(ACTION)) {
					WordTree<StructureTag> verbPhrase = tree.getType(ACTION)
							.getType(VERB_PHRASE);
					if(verbPhrase.hasType(INDIRECT_OBJECT)) {
						WordTree<StructureTag> noun =
								verbPhrase.getType(INDIRECT_OBJECT)
								.getType(NOUN_PHRASE);
						verbPhrase.removeChild(
								verbPhrase.getType(INDIRECT_OBJECT));
						WordTree<StructureTag> subject =
								new WordTree<>(SUBJECT);
						subject.addChild(noun);
						tree.addChild(subject);
					} else if(verbPhrase.hasType(OBJECT)) {
						WordTree<StructureTag> noun =
								verbPhrase.getType(OBJECT)
								.getType(NOUN_PHRASE);
						verbPhrase.removeChild(
								verbPhrase.getType(OBJECT));
						WordTree<StructureTag> subject =
								new WordTree<>(SUBJECT);
						if(noun != null)
							subject.addChild(noun);
						tree.addChild(subject);
					}
				}
			}
		}
	}
	
	// return whether subtrees should be interpreted
	private boolean interpretTree(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		StructureTag sTag = structureTree.getType();
		
		if(isIgnoredTag(tag)) {
			return true;
		}
		else if(isIncorrectTag(tag)) {
			error("Incorrect tag " + tag);
			return true;
		}
		else {
			
			if(sTag.isA(CONTAINS_PHRASE))
				return interpretTopLevel(tree);
			else if(sTag.equals(QUESTION))
				return interpretQuestion(tree);
			else if(sTag.equals(YES_NO))
				return interpretYesNo(tree);
				//return interpretVerbPhrase(tree);
			else if(sTag.equals(COMMAND))
				return interpretCommand(tree);
			else if(sTag.equals(STATEMENT))
				return interpretStatement(tree);
			else if(sTag.equals(INTERJECTION_PHRASE))
				return interpretInterjection(tree);
			else if(sTag.equals(VERB_PHRASE))
				return interpretVerbPhrase(tree);
			else if(sTag.equals(NOUN_PHRASE))
				return interpretNounPhrase(tree);
			else if(sTag.equals(ADJECTIVE_PHRASE))
				return interpretAdjectivePhrase(tree);
			else if(sTag.equals(ADVERB_PHRASE))
				return interpretAdverbPhrase(tree);
			else if(sTag.equals(PREPOSITION_PHRASE))
				return interpretPrepositionPhrase(tree);
			else if(sTag.equals(SUBORDINATING_CONJUNCTION_PHRASE))
				return interpretSubordinatingConjuctionPhrase(tree);
			else if(sTag.equals(PARTICLE_PHRASE))
				return interpretParticlePhrase(tree);
			else
				return interpretOther(tree);
			
		}
	}
	
	private boolean interpretTopLevel(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		switch(tag) {
		case WH_DIRECT_QUESTION:
			addChild(QUESTION);
			return true;
		case INVERTED_YES_NO_OR_WH_QUESTION_CLAUSE:
			addChild(YES_NO);
			return true;
		case SIMPLE_DECLARATIVE_CLAUSE:
			Tag subTag = tree.getChildren().get(0).getType();
			if(subTag.equals(Tag.VERB_PHRASE)
					|| subTag.equals(Tag.ADVERB_PHRASE))
				addChild(COMMAND);
			else
				addChild(STATEMENT);
			return true;
		
		case FRAGMENT:
			return true;
		case NOUN_PHRASE:
			addChild(FRAGMENT_NOUN);
			addChild(NOUN_PHRASE);
			return true;
		case ADJECTIVE_PHRASE:
			addChild(FRAGMENT_ADJECTIVE);
			addChild(ADJECTIVE_PHRASE);
			return true;
		case ADVERB_PHRASE:
			addChild(FRAGMENT_ADVERB);
			addChild(ADVERB_PHRASE);
			return true;
		case WH_DETERMINER:
		case WH_PRONOUN:
		case WH_ADVERB:
		case WH_ADJECTIVE_PHRASE:
		case WH_ADVERB_PHRASE:
		case WH_NOUN_PHRASE:
		case WH_PREPOSITIONAL_PHRASE:
			addChild(QUESTION_FRAGMENT);
			addChild(QUESTION_TYPE, tree);
			return false;
		
		case INTERJECTION_PHRASE:
			addChild(INTERJECTION_PHRASE);
			return true;
		
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	
//	private boolean interpretQuestion(WordTree<Tag> tree) {
//		Tag tag = tree.getType();
//		
//		if(tag.getWordType() == WordType.VERB) {
//			addOrGetChild(AUXILIARY_VERB);
//			addOrGetChild(VERB_PHRASE);
//			return interpretVerbPhrase(tree);
//		}
//		
//		switch(tag) {
//		case WH_ADVERB_PHRASE:
//		case WH_NOUN_PHRASE:
//			addChild(QUESTION_TYPE, tree);
//			return false;
//		case INVERTED_YES_NO_OR_WH_QUESTION_CLAUSE:
//			// do nothing -- question body
//			return true;
//		case NOUN_PHRASE:
//			addOrGetChild(SUBJECT);
//			addOrGetChild(NOUN_PHRASE);
//			return true;
//		case VERB_PHRASE:
//			addOrGetChild(ACTION);
//			addOrGetChild(VERB_PHRASE);
//			return true;
//		
//		default:
//			unknownTagError(tag);
//			return true;
//		}
//	}
	
	private boolean interpretQuestion(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		if(tag.getWordType().equals(WordType.VERB)) {
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			return interpretVerbPhrase(tree);
		}
		
		switch(tag) {
		case WH_ADVERB_PHRASE:
		case WH_NOUN_PHRASE:
			WordTree<Tag> nounTree = null;
			String questionWord = null;
			for(WordTree<Tag> child : tree.getChildren()) {
				switch(child.getType()) {
				case WH_PRONOUN:
				case POSSESSIVE_WH_PRONOUN:
				case WH_DETERMINER:
				case WH_ADVERB:
					if(questionWord == null)
						questionWord = child.getWord();
					else
						error("Multiple question words found.");
					break;
				default:
					if(child.getType().getWordType().equals(WordType.NOUN)
							|| child.getType().equals(Tag.NOUN_PHRASE)) {
						if(nounTree == null)
							nounTree = child;
						else
							error("Multiple question nouns found.");
					} else {
						unknownTagError(tag);
					}
					break;
				}
			}
			
			if(questionWord == null)
				error("No question word found.");
			else if(questionWord.equals("who")
					|| questionWord.equals("whom")) {
				addOrGetChild(ACTION);
				addOrGetChild(VERB_PHRASE);
				addChild(OBJECT);
				addChild(NOUN_PHRASE);
				addChild(QUESTION_PRONOUN, "who");
			}
			else if(questionWord.equals("what")) {
				addOrGetChild(ACTION);
				addOrGetChild(VERB_PHRASE);
				addChild(OBJECT);
				addChild(NOUN_PHRASE);
				if(nounTree == null) {
					addChild(QUESTION_PRONOUN, questionWord);
				} else { // what is acting as a determiner
					if(nounTree != null) {
						int initialDepth = depth;
						if(nounTree.isLeaf()) {
							interpretNounPhrase(nounTree);
						} else {
							for(WordTree<Tag> child : nounTree.getChildren())
								parseTreeRecursive(child);
						}
						while(depth > initialDepth) {
							structureTree = structureTree.getParent();
							depth--;
						}
					}
					addChild(QUESTION_DETERMINER, questionWord);
				}
			}
			else if(questionWord.equals("when")
					|| questionWord.equals("where")
					|| questionWord.equals("why")
					|| questionWord.equals("how")) {
				addOrGetChild(ACTION);
				addOrGetChild(VERB_PHRASE);
				addChild(QUESTION_ADVERB, questionWord);
			}
			else if(questionWord.equals("which")) {
				addOrGetChild(ACTION);
				addOrGetChild(VERB_PHRASE);
				addChild(OBJECT);
				addChild(NOUN_PHRASE);
				if(nounTree != null) {
					int initialDepth = depth;
					if(nounTree.isLeaf()) {
						interpretNounPhrase(nounTree);
					} else {
						for(WordTree<Tag> child : nounTree.getChildren())
							parseTreeRecursive(child);
					}
					while(depth > initialDepth) {
						structureTree = structureTree.getParent();
						depth--;
					}
				}
				addChild(QUESTION_DETERMINER, questionWord);
			}
			else if(questionWord.equals("whose")) {
				addOrGetChild(ACTION);
				addOrGetChild(VERB_PHRASE);
				addChild(OBJECT);
				addChild(NOUN_PHRASE);
				if(nounTree != null) {
					int initialDepth = depth;
					if(nounTree.isLeaf()) {
						interpretNounPhrase(nounTree);
					} else {
						for(WordTree<Tag> child : nounTree.getChildren())
							parseTreeRecursive(child);
					}
					while(depth > initialDepth) {
						structureTree = structureTree.getParent();
						depth--;
					}
				}
				addChild(POSSESSOR);
				addChild(NOUN_PHRASE);
				addChild(QUESTION_PRONOUN, "who");
			}
			else
				error("Unknown question word " + questionWord);
			
			return false;
		case INVERTED_YES_NO_OR_WH_QUESTION_CLAUSE:
			// do nothing -- question body
			return true;
		case NOUN_PHRASE:
			addOrGetChild(SUBJECT);
			addOrGetChild(NOUN_PHRASE);
			return true;
		case VERB_PHRASE:
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			return interpretVerbPhrase(tree);
		
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
//	private boolean interpretYesNo(WordTree<Tag> tree) {
//		Tag tag = tree.getType();
//		
//		if(tag.getWordType() == WordType.VERB) {
//			addOrGetChild(AUXILIARY_VERB);
//			addOrGetChild(VERB_PHRASE);
//			return interpretVerbPhrase(tree);
//		}
//		
//		switch(tag) {
//		case NOUN_PHRASE:
//			addOrGetChild(SUBJECT);
//			addOrGetChild(NOUN_PHRASE);
//			return true;
//		case VERB_PHRASE:
//			addOrGetChild(ACTION);
//			addOrGetChild(VERB_PHRASE);
//			return true;
//		
//		default:
//			unknownTagError(tag);
//			return true;
//		}
//	}
	
	private boolean interpretYesNo(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		if(tag.equals(Tag.NOUN_PHRASE) && !structureTree.hasType(SUBJECT)) {
			addChild(SUBJECT);
			addChild(NOUN_PHRASE);
			return true;
		} else {
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			return interpretVerbPhrase(tree);
		}
	}
	
	private boolean interpretCommand(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		switch(tag) {
		case VERB_PHRASE:
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			return true;
		case ADVERB_PHRASE:
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			addChild(ADVERB_PHRASE);
			return true;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretStatement(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		switch(tag) {
		case NOUN_PHRASE:
			addOrGetChild(SUBJECT);
			addOrGetChild(NOUN_PHRASE);
			return true;
		case VERB_PHRASE:
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			return true;
		case ADVERB_PHRASE:
			addOrGetChild(ACTION);
			addOrGetChild(VERB_PHRASE);
			addChild(ADVERB_PHRASE);
			return true;
		case SIMPLE_DECLARATIVE_CLAUSE:
			return true;
		case COORDINATING_CONJUNCTION:
			makeConjunction(tree.getWord());
			return false;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretInterjection(WordTree<Tag> tree) {
		if(tree.isLeaf()) {
			addChild(INTERJECTION_WORD, tree.getWord());
			return false;
		} else {
			return true;
		}
	}
	
	private boolean interpretVerbPhrase(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		if(tag.getWordType().equals(WordType.VERB)) {
			VerbInfo info = Vocab.getVerb(tree.getWord(), tag);
			String verbWord = info.getBaseForm();
			VerbType type = info.getType();
			if(type != VerbType.MODAL) {
				addChild(VERB, verbWord);
				upDepth();
			}
			
			String currentFrame = "";
			if(structureTree.hasType(TENSE_FRAME))
				currentFrame = structureTree.getType(TENSE_FRAME).getWord();
			
			switch(type) {
			case BASE:
				if(structureTree.hasType(TENSE_FRAME))
					structureTree.removeChild(
							structureTree.getType(TENSE_FRAME));
				addChild(TENSE_FRAME, "SIMPLE");
				break;
			case CONTINUOUS:
				if(structureTree.hasType(TENSE_FRAME))
					structureTree.removeChild(
							structureTree.getType(TENSE_FRAME));
				if(currentFrame.equals("PERFECT"))
					addChild(TENSE_FRAME, "PERFECT_CONTINUOUS");
				else
					addChild(TENSE_FRAME, "CONTINUOUS");
				upDepth();
				break;
			case MODAL:
				if(structureTree.hasType(TENSE_TIME))
					structureTree.removeChild(
							structureTree.getType(TENSE_TIME));
				addChild(TENSE_TIME, "FUTURE");
				upDepth();
				break;
			case PAST_SIMPLE:
				if(structureTree.hasType(TENSE_TIME))
					structureTree.removeChild(
							structureTree.getType(TENSE_TIME));
				if(structureTree.hasType(TENSE_FRAME))
					structureTree.removeChild(
							structureTree.getType(TENSE_FRAME));
				addChild(TENSE_TIME, "PAST");
				upDepth();
				addChild(TENSE_FRAME, "SIMPLE");
				upDepth();
				break;
			case PERFECT:
				if(structureTree.hasType(TENSE_FRAME))
					structureTree.removeChild(
							structureTree.getType(TENSE_FRAME));
				if(currentFrame.equals("CONTINUOUS"))
					addChild(TENSE_FRAME, "PERFECT_CONTINUOUS");
				else
					addChild(TENSE_FRAME, "PERFECT");
				upDepth();
				break;
			case PRESENT_SIMPLE:
				if(structureTree.hasType(TENSE_TIME))
					structureTree.removeChild(
							structureTree.getType(TENSE_TIME));
				if(structureTree.hasType(TENSE_FRAME))
					structureTree.removeChild(
							structureTree.getType(TENSE_FRAME));
				addChild(TENSE_TIME, "PRESENT");
				upDepth();
				addChild(TENSE_FRAME, "SIMPLE");
				upDepth();
				break;
			default:
				break;
			}
			
			return false;
		}
		
		if(tag.getWordType().equals(WordType.ADVERB)) {
			addChild(ADVERB_PHRASE);
			return interpretAdverbPhrase(tree);
		}
		
		switch(tag) {
		case ADVERB_PHRASE:
			addChild(ADVERB_PHRASE);
			return true;
		//if this verb-phrase has another verb-phrase inside it, that's the
		//REAL verb-phrase
		case VERB_PHRASE:
			List<WordTree<StructureTag>> remove = new ArrayList<>();
			for(WordTree<StructureTag> child : structureTree.getChildren()) {
				if(child.getType().equals(VERB))
					remove.add(child);
			}
			for(WordTree<StructureTag> child : remove)
				structureTree.removeChild(child);
			return true;
		case TO:
			return false;
		case NOUN_PHRASE:
			addVerbObject();
			addChild(NOUN_PHRASE);
			return true;
		case ADJECTIVE_PHRASE:
			addVerbObject();
			addChild(ADJECTIVE_PHRASE);
			return true;
		case SIMPLE_DECLARATIVE_CLAUSE:
			addVerbObject();
			addChild(VERB_PHRASE);
			return true;
		case PREPOSITIONAL_PHRASE:
			addChild(PREPOSITION_PHRASE);
			return true;
		case SUBORDINATING_CONJUNCTION_CLAUSE:
			addChild(SUBORDINATING_CONJUNCTION_PHRASE);
			return true;
		case PARTICLE_PHRASE:
			addChild(PARTICLE_PHRASE);
			return true;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private void addVerbObject() {
		if(structureTree.hasType(OBJECT)) {
			WordTree<StructureTag> indirectObject =
					new WordTree<>(INDIRECT_OBJECT);
			WordTree<StructureTag> object = structureTree.getType(OBJECT);
			structureTree.addChild(indirectObject);
			List<WordTree<StructureTag>> children =
					new ArrayList<>(object.getChildren());
			for(WordTree<StructureTag> child : children) {
				object.removeChild(child);
				indirectObject.addChild(child);
			}
			addOrGetChild(OBJECT);
		} else {
			addChild(OBJECT);
		}
	}
	
	private boolean interpretNounPhrase(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		if(tag.getWordType().equals(WordType.NOUN)) {
			NounInfo info = Vocab.getNoun(tree.getWord(), tag);
			String nounWord = info.getBaseForm();
			if(info.isPronoun()) {
				if(info.isQuestion())
					addChild(QUESTION_PRONOUN, nounWord);
				else
					addChild(PRONOUN, Vocab.swapPronoun(nounWord));
			} else {
				// mine is tagged as a noun
				addChild(NOUN, Vocab.swapPronoun(nounWord));
			}
			upDepth();
			if(info.isPlural()) {
				addChild(PLURAL, info.getWord());
			}
			return false;
		}
		
		if(tag.getWordType().equals(WordType.ADJECTIVE)) {
			addChild(ADJECTIVE_PHRASE);
			return interpretAdjectivePhrase(tree);
		}
		
		switch(tag) {
		case ADVERB:
			if(tree.getWord().equals("not")) {
				// "not" is sometimes put in the object of the verb
				WordTree<StructureTag> adverbPhrase =
						new WordTree<>(ADVERB_PHRASE);
				structureTree.getParent().getParent().addChild(adverbPhrase);
				adverbPhrase.addChild(new WordTree<>(ADVERB, tree.getWord()));
				return false;
			} else {
				// "there" and possibly others are categorized as adverbs
				addChild(NOUN, tree.getWord());
				return false;
			}
		case DETERMINER:
			String determiner = tree.getWord();
			if(determiner.equals("an"))
				determiner = "a";
			addChild(DETERMINER, determiner);
			return false;
		case ADJECTIVE_PHRASE:
			addChild(ADJECTIVE_PHRASE);
			return true;
		case COORDINATING_CONJUNCTION: //used for, eg. quick and brown
			return false;              //can probably be safely ignored
		case NOUN_PHRASE:
			if(tree.hasType(Tag.POSSESSIVE_ENDING)) {
				addChild(POSSESSOR);
				addChild(NOUN_PHRASE);
				return true;
			} else {
				// sub-phrases are sometimes used with prepositions
				return true;
			}
		case PREPOSITIONAL_PHRASE:
			addChild(PREPOSITION_PHRASE);
			return true;
		case POSSESSIVE_ENDING:
			if(!structureTree.getParent().getType().equals(POSSESSOR)) {
				structureTree.insertTree(
						new WordTree<>(POSSESSOR));
				depth++;
			}
			return false;
		case POSSESSIVE_PRONOUN:
			addChild(POSSESSOR);
			addChild(NOUN_PHRASE);
			
			String possessor = Vocab.pronounPossessor(tree.getWord());
			if(Vocab.isAWhWord(possessor))
				addChild(QUESTION_PRONOUN, possessor);
			else
				addChild(PRONOUN, possessor);
			return false;
		case SUBORDINATING_CONJUNCTION_CLAUSE:
			addChild(SUBORDINATING_CONJUNCTION_PHRASE);
			return true;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretAdjectivePhrase(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		//stanford parser classifies adjectives in yes-no's as verbs for some
		//reason
		if(tag.getWordType().equals(WordType.VERB)) {
			tag = Tag.ADJECTIVE;
		}
		
		if(tag.getWordType().equals(WordType.ADJECTIVE)) {
			if(structureTree.hasType(ADJECTIVE)) {
				//already has an adjective, so we need to create a new tree
				upDepth();
				addChild(ADJECTIVE_PHRASE);
			}
			
			AdjectiveInfo info = Vocab.getAdjective(tree.getWord(), tag);
			String adjectiveWord = info.getBaseForm();
			// yours is tagged as an adjective
			addChild(ADJECTIVE, Vocab.swapPronoun(adjectiveWord));
			upDepth();
			
			switch(info.getType()) {
			case NORMAL:
				break;
			case COMPARATIVE:
				addChild(COMPARATIVE, info.getWord());
				break;
			case SUPERLATIVE:
				addChild(SUPERLATIVE, info.getWord());
				break;
			}
			
			return false;
		}
		
		if(tag.getWordType().equals(WordType.ADVERB)) {
			addChild(ADVERB_PHRASE);
			return interpretAdverbPhrase(tree);
		}
		
		switch(tag) {
		case ADVERB_PHRASE:
			addChild(ADVERB_PHRASE);
			return true;
		case ADJECTIVE_PHRASE:
			// sub-phrases occur in some prepositions
			return true;
		case PREPOSITIONAL_PHRASE:
			addChild(PREPOSITION_PHRASE);
			return true;
		default:
			unknownTagError(tag);
			return true;
		}
		
		
	}
	
	private boolean interpretAdverbPhrase(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		if(tag.equals(Tag.WH_ADVERB)) {
			structureTree.getParent().addChild(
					new WordTree<>(QUESTION_ADVERB, tree.getWord()));
			return false;
		}
		if(tag.getWordType().equals(WordType.ADVERB)) {
			if(structureTree.hasType(ADVERB)) { // like "very"
				structureTree = structureTree.insertTree(
						new WordTree<>(ADVERB_PHRASE));
			}
			
			AdverbInfo info = Vocab.getAdverb(tree.getWord(), tag);
			String adverbWord = info.getBaseForm();
			addChild(ADVERB, adverbWord);
			upDepth();
			
			switch(info.getType()) {
			case NORMAL:
				break;
			case COMPARATIVE:
				addChild(COMPARATIVE, info.getWord());
				break;
			case SUPERLATIVE:
				addChild(SUPERLATIVE, info.getWord());
				break;
			}
			
			return false;
		}
		
		switch(tag) {
		//often used for and's that combine multiple adverbs
		//can probably be safely ignored
		case COORDINATING_CONJUNCTION:
			return false;
		case ADVERB_PHRASE:
			return true;
		case PREPOSITIONAL_PHRASE: // TODO: not sure if these actually appear
			addChild(PREPOSITION_PHRASE);
			return true;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretPrepositionPhrase(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		switch(tag) {
		case TO:
		case PREPOSITION_OR_SUBORDINATING_CONJUNCTION:
			addChild(PREPOSITION, tree.getWord());
			return false;
		case NOUN_PHRASE:
			addChild(OBJECT);
			addChild(NOUN_PHRASE);
			return true;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretSubordinatingConjuctionPhrase(
			WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		switch(tag) {
		case PREPOSITION_OR_SUBORDINATING_CONJUNCTION:
			addChild(CONJUNCTION, tree.getWord());
			return false;
		case SIMPLE_DECLARATIVE_CLAUSE:
			addOrGetChild(STATEMENT);
			return true;
		case WH_NOUN_PHRASE:
			addOrGetChild(STATEMENT);
			addOrGetChild(SUBJECT);
			addOrGetChild(NOUN_PHRASE);
			addChild(REFERRING_PRONOUN, tree);
			return false;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretParticlePhrase(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		
		switch(tag) {
		case PARTICLE:
			addChild(PARTICLE, tree.getWord());
			return false;
		default:
			unknownTagError(tag);
			return true;
		}
	}
	
	private boolean interpretOther(WordTree<Tag> tree) {
		Tag tag = tree.getType();
		StructureTag sTag = structureTree.getType();
		error("Unrecognized parent tag! " + sTag);
		unknownTagError(tag);
		return true;
	}
	
	private void error(String text) {
		logOut.println("WARNING: " + text);
	}
	
	private void unknownTagError(Object tag) {
		error("Unknown tag " + tag);
	}
	
	private void makeConjunction(String conjunctionWord) {
		structureTree = structureTree.insertTree(
				new WordTree<>(CONJUNCTION_PHRASE));
		addChild(CONJUNCTION, conjunctionWord);
		upDepth();
	}
	
	private void addChild(StructureTag childTag) {
		structureTree = structureTree.addChild(new WordTree<>(childTag));
		depth++;
	}
	
	private void addOrGetChild(StructureTag childTag) {
		if(structureTree.hasType(childTag))
			structureTree = structureTree.getType(childTag);
		else
			structureTree = structureTree.addChild(new WordTree<>(childTag));
		depth++;
	}
	
	private void addChild(StructureTag childTag, String leafText) {
		structureTree = structureTree.addChild(
				new WordTree<>(childTag, leafText));
		depth++;
	}
	
	private void addChild(StructureTag childTag, WordTree<Tag> leafTree) {
		logOut.println("WARNING: Compressing tag tree!");
		structureTree = structureTree.addChild(
				new WordTree<>(childTag, leafTree.wordListToString()));
		depth++;
	}
	
	private void upDepth() {
		depth--;
		structureTree = structureTree.getParent();
	}
}
