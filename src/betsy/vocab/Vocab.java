package betsy.vocab;

import betsy.grammar.Tag;
import betsy.vocab.AdjectiveInfo.AdjectiveType;
import betsy.vocab.VerbInfo.*;
import net.sf.extjwnl.dictionary.Dictionary;
import static net.sf.extjwnl.data.POS.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.extjwnl.data.IndexWord;

/**
 * A collection of methods used for analyzing single words.
 * @author jacob
 *
 */
public class Vocab {
	/**
	 * A WordNet dictionary.
	 */
	public static Dictionary d;
	
	private static final String CONTRACTIONS_FILE = "contractions.txt";
	private static final String PAST_TENSE_VERBS_FILE = "pastTense.txt";
	
	/**
	 * A Map, associating contraction words like "don't" with their expanded
	 * forms ("do not") in the form of an array of words.
	 */
	public static Map<String, String[]> contractions = null;
	/**
	 * A Map, associating the present, simple tense of verbs with their past
	 * tense.
	 */
	public static Map<String, String> pastTenseVerbs = null;
	
	/**
	 * Load important data from files.
	 */
	public static void init() {
		try {
			d = Dictionary.getDefaultResourceInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		loadContractions();
		loadPastTenseVerbs();
	}
	
	private static void loadContractions() {
		List<String> lines =
				betsy.BetsyMain.getResourceLines(CONTRACTIONS_FILE);
		
		contractions = new HashMap<>();
		for(String s : lines) {
			int tabIndex = s.indexOf("\t");
			String contraction = s.substring(0, tabIndex);
			String words = s.substring(tabIndex + 1);
			String[] wordsArray = words.split(" ");
			contractions.put(contraction, wordsArray);
		}
	}
	
	private static void loadPastTenseVerbs() {
		pastTenseVerbs = new HashMap<>();
		List<String> lines =
				betsy.BetsyMain.getResourceLines(PAST_TENSE_VERBS_FILE);
		
		for(String s : lines) {
			int spaceIndex = s.indexOf(' ');
			String present = s.substring(0, spaceIndex);
			String past = s.substring(spaceIndex + 1);
			pastTenseVerbs.put(present, past);
		}
	}
	
	/**
	 * Given an array of words, produce a new array with the contraction words
	 * (like "don't") replaced with their expanded form ("do not").
	 * @param words the words with potential contractions to replace
	 * @return a new array of words with contractions replaced with multiple
	 * words.
	 */
	public static String[] replaceContractions(String[] words) {
		if(contractions == null)
			loadContractions();
		
		List<String> newWordsList = new ArrayList<>();
		
		for(String s : words) {
			String key1 = s.toLowerCase();
			String key2 = key1.replace("'", "");
			if(contractions.containsKey(key1)) {
				for(String word : contractions.get(key1))
					newWordsList.add(word);
			} else if(contractions.containsKey(key2)) {
				for(String word : contractions.get(key2))
					newWordsList.add(word);
			} else if(s.toLowerCase().endsWith("'s")) {
				newWordsList.add(s.substring(0, s.length()-2));
				newWordsList.add("'s");
			} else {
				newWordsList.add(s);
			}
		}
		
		return newWordsList.toArray(new String[0]);
	}
	
	/**
	 * Get the WordInfo for a noun word.
	 * @param s the word
	 * @param tag the tag given by the parser -- helps with determining the
	 * context of the word
	 * @return the WordInfo for the word
	 */
	public static NounInfo getNoun(String s, Tag tag) {
		try {
			IndexWord index = d.lookupIndexWord(NOUN, s);
			boolean plural, pronoun;
			boolean question = false;
			switch(tag) {
			case NOUN_PLURAL:
				plural = true;
				pronoun = false;
				break;
			case NOUN_SINGULAR_OR_MASS:
				plural = false;
				pronoun = false;
				break;
			case PERSONAL_PRONOUN:
				plural = false;
				pronoun = true;
				break;
			case PROPER_NOUN_PLURAL:
				plural = true;
				pronoun = false;
				break;
			case PROPER_NOUN_SINGULAR:
				plural = false;
				pronoun = false;
				break;
			case WH_PRONOUN:
				plural = false;
				pronoun = true;
				question = true;
				break;
			default:
				return null;
			}
			return new NounInfo(s, index, plural, pronoun, question);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the WordInfo for a verb word.
	 * @param s the word
	 * @param tag the tag given by the parser -- helps with determining the
	 * context of the word
	 * @return the WordInfo for the word
	 */
	public static VerbInfo getVerb(String s, Tag tag) {
		try {
			IndexWord index = d.lookupIndexWord(VERB, s);
			VerbType type;
			switch(tag) {
			case MODAL_VERB: // can, could, (dare), may, might, must, ought
				             // ought, shall, should, will, would
				type = VerbType.MODAL;
				break;
			case VERB_BASE_FORM: // to speak
				type = VerbType.BASE;
				break;
			case VERB_PAST_TENSE: // spoke
				type = VerbType.PAST_SIMPLE;
				break;
			case VERB_GERUND_OR_PRESENT_PARTICIPLE: //speaking
				type = VerbType.CONTINUOUS;
				break;
			case VERB_PAST_PARTICIPLE: //spoken
				type = VerbType.PERFECT;
				break;
			case VERB_PRESENT_TENSE: //speak
			case VERB_PRESENT_TENSE_3RD_PERSON_SINGULAR: //speaks
				type = VerbType.PRESENT_SIMPLE;
				break;
			default:
				return null;
			}
			return new VerbInfo(s, index, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the WordInfo for an adjective word.
	 * @param s the word
	 * @param tag the tag given by the parser -- helps with determining the
	 * context of the word
	 * @return the WordInfo for the word
	 */
	public static AdjectiveInfo getAdjective(String s, Tag tag) {
		try {
			IndexWord index = d.lookupIndexWord(ADJECTIVE, s);
			AdjectiveType type;
			switch(tag) {
			case ADJECTIVE:
				type = AdjectiveType.NORMAL;
				break;
			case ADJECTIVE_COMPARATIVE:
				type = AdjectiveType.COMPARATIVE;
				break;
			case ADJECTIVE_SUPERLATIVE:
				type = AdjectiveType.SUPERLATIVE;
				break;
			default:
				return null;
			}
			return new AdjectiveInfo(s, index, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the WordInfo for an adverb word.
	 * @param s the word
	 * @param tag the tag given by the parser -- helps with determining the
	 * context of the word
	 * @return the WordInfo for the word
	 */
	public static AdverbInfo getAdverb(String s, Tag tag) {
		try {
			IndexWord index = d.lookupIndexWord(ADVERB, s);
			AdjectiveType type;
			switch(tag) {
			case ADVERB:
				type = AdjectiveType.NORMAL;
				break;
			case ADVERB_COMPARATIVE:
				type = AdjectiveType.COMPARATIVE;
				break;
			case ADVERB_SUPERLATIVE:
				type = AdjectiveType.SUPERLATIVE;
				break;
			default:
				return null;
			}
			return new AdverbInfo(s, index, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * If the given word is a pronoun like "you" or "yourself", swap it with
	 * "me" or "myself". Pronouns like "she" or "i" become their canonical
	 * forms, "her" or "me".
	 * @param pronoun the word to check
	 * @return the word, possibly swapped with other pronouns
	 */
	public static String swapPronoun(String pronoun) {
		pronoun = pronoun.toLowerCase();
		
		// canonical pronouns
		if(pronoun.equals("she"))
			return "her";
		if(pronoun.equals("he"))
			return "him";
		
		if(pronoun.equals("i"))
			return "you";
		if(pronoun.equals("me"))
			return "you";
		if(pronoun.equals("you"))
			return "me";
		
		if(pronoun.equals("myself"))
			return "yourself";
		if(pronoun.equals("yourself"))
			return "myself";
		if(pronoun.equals("yourselves"))
			return "ourselves";
		
		// are these pronouns?
		if(pronoun.equals("mine"))
			return "yours";
		if(pronoun.equals("yours"))
			return "mine";
		
		return pronoun;
	}
	
	/**
	 * If the given word is a possessive pronoun like "my", swap it as in
	 * swapPronoun() and replace it with the possessor moun of the word ("me").
	 * @param pronoun the word to check
	 * @return the word, possibly swapped with other pronouns
	 */
	public static String pronounPossessor(String pronoun) {
		pronoun = pronoun.toLowerCase();
		
		if(pronoun.equals("my"))
			return "you";
		if(pronoun.equals("your"))
			return "me";
		if(pronoun.equals("his"))
			return "him";
		if(pronoun.equals("her"))
			return "her"; //...
		if(pronoun.equals("its"))
			return "it";
		if(pronoun.equals("our"))
			return "us";
		if(pronoun.equals("their"))
			return "them";
		if(pronoun.equals("whose"))
			return "who";
		
		return pronoun;
	}
	
	/**
	 * Check if the word is a "WH" question word: who, what, when, where, why,
	 * how, which, whom, whose.
	 * @param word the word to check
	 * @return true if it is a question word
	 */
	public static boolean isAWhWord(String word) {
		word = word.toLowerCase();
		
		if(word.equals("who"))
			return true;
		if(word.equals("what"))
			return true;
		if(word.equals("when"))
			return true;
		if(word.equals("where"))
			return true;
		if(word.equals("why"))
			return true;
		if(word.equals("how"))
			return true;
		if(word.equals("which"))
			return true;
		if(word.equals("whom"))
			return true;
		if(word.equals("whose"))
			return true;
		
		return false;
	}
	
	/**
	 * Make a singular noun plural, using a set of rules that works for most
	 * nouns, but not all.
	 * @param noun the singular form of the noun
	 * @return the noun's plural form
	 */
	public static String makePlural(String noun) {
		// http://www.edufind.com/english-grammar/plural-nouns/
		int len = noun.length();
		if(noun.endsWith("s") || noun.endsWith("x") || noun.endsWith("z")
				|| noun.endsWith("ch") || noun.endsWith("sh"))
			return noun + "es";
		else if(noun.charAt(len - 1) == 'y'
				&& isConsonant(noun.charAt(len - 2)))
			return noun.substring(0, len - 1) + "ies";
		else
			return noun + "s";
	}
	
	/**
	 * Check if the character is a consonant: a, e, i, o, or u.
	 * @param c the character to check
	 * @return true if it is a consonant
	 */
	public static boolean isConsonant(char c) {
		return c != 'a' && c != 'e' && c != 'i' && c != 'o' && c != 'u';
	}
	
	/**
	 * Make a noun possessive (without swapping it). For example, "me"
	 * becomes "my," "him" becomes "her," and "Joe" becomes "Joe's".
	 * @param noun the pronoun to make possessive
	 * @return the possessive form of the noun
	 */
	public static String makePossessive(String noun) {
		noun = noun.toLowerCase();
		
		if(noun.equals("me"))
			return "my";
		if(noun.equals("I"))
			return "my";
		if(noun.equals("you"))
			return "your";
		if(noun.equals("him"))
			return "his";
		if(noun.equals("he"))
			return "his";
		if(noun.equals("her"))
			return "her";
		if(noun.equals("she"))
			return "her";
		if(noun.equals("it"))
			return "its";
		if(noun.equals("us"))
			return "our";
		if(noun.equals("they"))
			return "their";
		if(noun.equals("them"))
			return "their";
		if(noun.equals("who"))
			return "whose";
		
		return noun + "'s";
	}
	
	/**
	 * Some pronouns change when they are the subject of the sentence. "Me"
	 * becomes "I," "him" becomes "he," etc. This changes a pronoun to make it
	 * grammatically correct in the subject of a sentence.
	 * @param pronoun the word to check
	 * @return a version of the word that is more grammatically correct when
	 * used in the subject of a sentence.
	 */
	public static String makePronounSubject(String pronoun) {
		pronoun = pronoun.toLowerCase();
		
		if(pronoun.equals("me"))
			return "I";
		if(pronoun.equals("him"))
			return "he";
		if(pronoun.equals("her"))
			return "she";
		if(pronoun.equals("them"))
			return "they";
		
		return pronoun;
	}
	
	/**
	 * the past, the present, and the future walked into a bar.
	 * it was tense.
	 */
	private enum TenseTime {
		PAST, PRESENT, FUTURE, GENERIC
	}
	
	private enum TenseFrame {
		SIMPLE, PERFECT, CONTINUOUS, PERFECT_CONTINUOUS
	}
	
	/**
	 * Change a verb to be in a given tense. This is done using a set of rules
	 * or lists of common verbs and their tenses.
	 * @param verb the verb in its base form (present, simple tense)
	 * @param tenseTime the time of the verb tense: "PAST", "PRESENT",
	 * "FUTURE", or "GENERIC"
	 * generic
	 * @param tenseFrame the frame of the verb tense: "SIMPLE", "PERFECT",
	 * "CONTINUOUS", or "PERFECT_CONTINUOUS"
	 * @return the verb word, in the given tense
	 */
	@SuppressWarnings("incomplete-switch")
	public static String conjugateVerb(String verb,
			String tenseTime, String tenseFrame) {
		//TODO: recognize different pronouns, fix conjugations
		
		TenseTime time = TenseTime.GENERIC;
		TenseFrame frame = TenseFrame.SIMPLE;
		
		if(tenseTime != null && !tenseTime.isEmpty())
			time = TenseTime.valueOf(tenseTime);
		if(tenseFrame != null && !tenseFrame.isEmpty())
			frame = TenseFrame.valueOf(tenseFrame);
		
		boolean isBe = verb.equals("be");
		
		
		switch(frame) {
		case SIMPLE:
			if(isBe) {
				switch(time) {
				case PAST:
					verb = "was";
					break;
				case PRESENT:
					verb = "is";
					break;
				case FUTURE:
					verb = "will be";
					break;
				}
			} else {
				switch(time) {
				case PAST:
					verb = verbAddEd(verb);
					break;
				case PRESENT:
					verb = verb + "s";
					break;
				case FUTURE:
					verb = "will " + verb;
					break;
				}
			}
			break;
		case CONTINUOUS:
			verb = verb + "ing";
			switch(time) {
			case PAST:
				verb = "was " + verb;
				break;
			case PRESENT:
				verb = "is " + verb;
				break;
			case FUTURE:
				verb = "will be " + verb;
				break;
			}
			break;
		case PERFECT:
			if(isBe)
				verb = "been";
			else
				verb = verbAddEd(verb);
			switch(time) {
			case PAST:
				verb = "had " + verb;
				break;
			case PRESENT:
				verb = "has " + verb;
				break;
			case FUTURE:
				verb = "will have " + verb;
				break;
			}
			break;
		case PERFECT_CONTINUOUS:
			verb = verb + "ing";
			switch(time) {
			case PAST:
				verb = "had been " + verb;
				break;
			case PRESENT:
				verb = "has been " + verb;
				break;
			case FUTURE:
				verb = "will have been " + verb;
				break;
			}
			break;
		} // end of frame switch
		
		return verb;
	}
	
	private static String verbAddEd(String verb) {
		// special cases are common with past tense verbs
		// http://www.linguasorb.com/english/verbs/most-common-verbs/
		if(pastTenseVerbs.containsKey(verb))
			return pastTenseVerbs.get(verb);
		
		if(verb.endsWith("e"))
			return verb + "d";
		if(verb.endsWith("y"))
			return verb.substring(0, verb.length() - 1) + "ied";
		return verb + "ed";
	}
	
	/**
	 * Make an adjective or adverb comparative, like "fast" -&gt; "faster".
	 * This method cheats and just returns things like "more fast."
	 * @param adjective the adjective or adverb to make comparative
	 * @return the comparative form of the word
	 */
	public static String makeComparative(String adjective) {
		//TODO: more intelligent conjugations
		//see: http://grammar.yourdictionary.com/parts-of-speech/adjectives/what-is-a-superlative-adjective.html
		return "more " + adjective;
	}
	
	/**
	 * Make an adjective or adverb superlative, like "fast" -&gt; "fastest".
	 * This method cheats and just returns things like "most fast."
	 * @param adjective the adjective or adverb to make superlative
	 * @return the superlative form of the word
	 */
	public static String makeSuperlative(String adjective) {
		//TODO: more intelligent conjugations
		return "most " + adjective;
	}
}
