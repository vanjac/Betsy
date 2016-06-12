package betsy.memory;

import java.util.List;
import java.util.ArrayList;
import betsy.grammar.*;
import betsy.vocab.Names;
import static betsy.grammar.StructureTag.*;

/**
 * The Context class is used to keep track of what generic pronouns like "him,"
 * "her," "it," "them," etc. refer to, based on nouns that the user has talked
 * about in previous sentences. It keeps track of four types of prounouns:
 * "him," for male people (based on a list of common male names), "her," for
 * female people (based on a list of common female names), "it" for generic
 * singular objects, and "them" for generic plural objects or people.
 * @author jacob
 *
 */
public class Context {
	
	private WordTree<StructureTag> him = null;
	private WordTree<StructureTag> her = null;
	private WordTree<StructureTag> it = null;
	private WordTree<StructureTag> them = null;
	
	/**
	 * Get the phrase that the word "him" most likely refers to.
	 * @return WordTree whose root is a NOUN_PHRASE
	 */
	public WordTree<StructureTag> getHim() {
		if(him != null)
			return him;
		else
			return it;
	}
	
	/**
	 * Get the phrase that the word "her" most likely refers to.
	 * @return WordTree whose root is a NOUN_PHRASE
	 */
	public WordTree<StructureTag> getHer() {
		if(her != null)
			return her;
		else
			return it;
	}
	
	/**
	 * Get the phrase that the word "it" most likely refers to.
	 * @return WordTree whose root is a NOUN_PHRASE
	 */
	public WordTree<StructureTag> getIt() {
		return it;
	}
	
	/**
	 * Get the phrase that the word "them" most likely refers to.
	 * @return WordTree whose root is a NOUN_PHRASE
	 */
	public WordTree<StructureTag> getThem() {
		return them;
	}
	
	/**
	 * Interpret a user's sentence, pick out the nouns and store them as the
	 * pronouns that could be used to refer to them.
	 * @param phrase the phrase, as a WordTree, to interpret and search for
	 * nouns
	 */
	public void interpretContext(WordTree<StructureTag> phrase) {
		List<WordTree<StructureTag>> nounPhrases = new ArrayList<>();
		findNounPhrases(phrase, nounPhrases);
		
		// start at end of list
		for(WordTree<StructureTag> tree : nounPhrases) {
			
			if(tree.hasType(PLURAL)) {
				them = tree;
				continue;
			}
			
			WordTree<StructureTag> noun = tree.getType(NOUN);
			
			if(noun != null) {
				// check if noun is name of person
				if(Names.maleNames.contains(noun.getWord())) {
					him = tree;
					continue;
				}
				if(Names.femaleNames.contains(noun.getWord())) {
					her = tree;
					continue;
				}
			}
			
			// all unmatched nouns are "it"
			it = tree;
		}
	}
	
	/**
	 * Replace pronouns in the phrase like "him," "her," etc. with their
	 * most likely meaning. Not all pronouns will be replaced -- some may not
	 * have a match.
	 * @param phrase the phrase to scan and replace. A new WordTree isn't
	 * created -- the existing one is modified in place.
	 */
	public void replaceContext(WordTree<StructureTag> phrase) {
		List<WordTree<StructureTag>> nounPhrases = new ArrayList<>();
		findNounPhrases(phrase, nounPhrases);
		
		for(WordTree<StructureTag> tree : nounPhrases) {
			if(tree.hasType(PRONOUN)) {
				String pronounWord = tree.getType(PRONOUN).getWord();
				WordTree<StructureTag> context = getContextFromPronoun(
						pronounWord);
				if(context == null)
					continue;
				tree.removeChild(tree.getType(PRONOUN));
				for(WordTree<StructureTag> child : context.getChildren()) {
					tree.addChild(child.clone());
				}
			}
		}
	}
	
	/**
	 * Given a pronoun word (he, she, they, it, etc.), get the noun-phrase,
	 * based on the user's recent sentences, most likely meant by it.
	 * @param pronoun the pronoun word
	 * @return a noun-phrase, in the form of a WordTree with root NOUN_PHRASE
	 */
	public WordTree<StructureTag> getContextFromPronoun(String pronoun) {
		if(pronoun.equals("she") || pronoun.equals("her")
				|| pronoun.equals("herself"))
			return getHer();
		if(pronoun.equals("he") || pronoun.equals("him")
				|| pronoun.equals("himself"))
			return getHim();
		if(pronoun.equals("they") || pronoun.equals("them")
				|| pronoun.equals("those") || pronoun.equals("themselves"))
			return getThem();
		if(pronoun.equals("that") || pronoun.equals("it")
				|| pronoun.equals("itself"))
			return getIt();
		// TODO: all
		
		return null;
	}
	
	/**
	 * Get the current state of the context as a human-readable string.
	 * @param constructor the SentenceConstructor used to turn phrase trees
	 * into human-readable strings.
	 * @return a String describing the current context
	 */
	public String getContextDescription(SentenceConstructor constructor) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("\tHim: ");
		sb.append(phraseToString(him, constructor));
		sb.append("\n\tHer: ");
		sb.append(phraseToString(her, constructor));
		sb.append("\n\tIt: ");
		sb.append(phraseToString(it, constructor));
		sb.append("\n\tThem: ");
		sb.append(phraseToString(them, constructor));
		
		return sb.toString();
	}
	
	private String phraseToString(WordTree<StructureTag> phrase,
			SentenceConstructor constructor) {
		if(phrase == null)
			return "None.";
		else
			return constructor.constructSentence(phrase, false);
	}
	
	private void findNounPhrases(WordTree<StructureTag> tree,
			List<WordTree<StructureTag>> nounPhrases) {
		if(tree.getType().equals(NOUN_PHRASE))
			nounPhrases.add(tree);
		if(!tree.isLeaf())
			for(WordTree<StructureTag> child : tree.getChildren())
				findNounPhrases(child, nounPhrases);
		return;
	}
	
}
