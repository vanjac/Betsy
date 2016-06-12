package betsy.grammar;

import static betsy.grammar.StructureTag.CategoryTag.*;

/**
 * Trees of StructureTags are used to describe a sentence. StructureTags are
 * used to "tag" either individual words or phrases in a sentence. The tag
 * describes the word or phrase's role in the sentence. There are clearly
 * defined rules for what tags mean and what other tags they are allowed to
 * contain.
 * @author jacob
 *
 */
public enum StructureTag {
	ROOT(CONTAINS_PHRASE),
	
	// Top-level phrases, contained by ROOT
	CONJUNCTION_PHRASE(PHRASE, CONTAINS_PHRASE),
	STATEMENT(PHRASE),
	COMMAND(PHRASE, SINGLE_CHILD), // contains an ACTION
	QUESTION(PHRASE),
	YES_NO(PHRASE),
	INTERJECTION_PHRASE(PHRASE),
	FRAGMENT_NOUN(PHRASE, SINGLE_CHILD), // contains a NOUN_PHRASE
	FRAGMENT_ADJECTIVE(PHRASE, SINGLE_CHILD), // contains ADJECTIVE_PHRASE
	FRAGMENT_ADVERB(PHRASE, SINGLE_CHILD), // contains an ADVERB_PHRASE
	QUESTION_FRAGMENT(PHRASE, SINGLE_CHILD), // contains a QUESTION_TYPE
	
	// subtags of conjunction phrase
	// can contain other phrases
	CONJUNCTION(WORD), // leaf
	
	// subtags of statements
	SUBJECT(SINGLE_CHILD), // contains a NOUN_PHRASE.
	ACTION(SINGLE_CHILD), // contains a VERB_PHRASE
	
	// subtags of questions
	// SUBJECT
	// ACTION
	@Deprecated
	QUESTION_TYPE(WORD), // leaf: who, what, when, where, why, how, etc.
//	@Deprecated
//	AUXILIARY_VERB(SINGLE_CHILD), // contains one child, a VERB_PHRASE
	
	// subtags of yes-no's
	// SUBJECT
	// ACTION
	
	// subtags of interjections
	INTERJECTION_WORD(WORD), // leaf
	
	NOUN_PHRASE(),
	VERB_PHRASE(),
	ADJECTIVE_PHRASE(),
	ADVERB_PHRASE(),
	
	// subtags of noun-phrases
	NOUN(WORD), // leaf
	PRONOUN(WORD), // leaf, similar usage to NOUN
	QUESTION_PRONOUN(WORD, ANSWER), // leaf, a wh-pronoun
	REFERRING_PRONOUN(WORD), // refers to a previous noun, like which / that
	// ADJECTIVE_PHRASE
	PLURAL(IGNORED), // leaf: present if noun is plural
	                 // contains either the plural form of the word or nothing
	DETERMINER(WORD), // leaf
	QUESTION_DETERMINER(WORD, ANSWER), // "which," sometimes "what"
	POSSESSOR(SINGLE_CHILD), // contains one child, a NOUN_PHRASE
	// PREPOSITION_PHRASE
	// SUBORDINATING_CONJUNCTION_PHRASE // e.g. a dog that is hot
	
	// subtags of verb-phrases
	VERB(WORD), // leaf
	// ADVERB_PHRASE
	TENSE_TIME(IGNORED), // leaf: PAST, PRESENT, or FUTURE
	TENSE_FRAME(IGNORED), // leaf: SIMPLE/PERFECT/CONTINUOUS/PERFECT_CONTINUOUS
	OBJECT(SINGLE_CHILD), // contains one child, either a NOUN_PHRASE, an
	                      // ADJECTIVE_PHRASE (as in "I am happy"), a
	                      // VERB_PHRASE ("I like to run"), or a
	                      // POSSESSOR ("That is joe's.")
	INDIRECT_OBJECT(SINGLE_CHILD), // in phrases like "tell me your name",
	                               // "me" is the indirect object
	PREPOSITION_PHRASE(),
	SUBORDINATING_CONJUNCTION_PHRASE(),
	PARTICLE_PHRASE(), // on, off, away, etc.
	QUESTION_ADVERB(WORD, ANSWER), // not part of an adverb phrase
	
	// subtags of adjective-phrases
	ADJECTIVE(WORD), // leaf
	COMPARATIVE(IGNORED), // leaf: comparative form or empty string
	SUPERLATIVE(IGNORED), // leaf: superlative form or empty string
	// ADVERB_PHRASE // can describe an adjective, like "very"
	// PREPOSITION_PHRASE // e.g, happier than you
	
	// subtags of adverb-phrases
	ADVERB(WORD), // leaf
	// COMPARATIVE
	// SUPERLATIVE
	// PREPOSITION_PHRASE
	// ADVERB_PHRASE // adverbs describing the adverb, like very
	
	// subtags of preposition-phrases
	PREPOSITION(WORD), // leaf
	// OBJECT
	
	// subtags of subordinating-conjunction-phrases
	// phrases like "which is hot" have no conjunction
	// instead, which is the subject of the statement
	// CONJUNCTION
	// STATEMENT
	
	// subtags of particle-phrases
	PARTICLE(WORD), // leaf
	;
	
	/**
	 * CategoryTags are used to categorize StructureTags. Each StructureTag
	 * can have any number of CategoryTags associated with it.
	 * @author jacob
	 *
	 */
	public enum CategoryTag {
		PHRASE,
		/**
		 * Could contain a Phrase
		 */
		CONTAINS_PHRASE,
		/**
		 * A leaf with a real word used in the sentence.
		 */
		WORD,
		/**
		 * A tree that has only one child.
		 */
		SINGLE_CHILD,
		/**
		 * A leaf with a word that should be ignored when constructing a
		 * sentence.
		 */
		IGNORED,
		/**
		 * A place where an answer to a question could be filled in. A leaf,
		 * where the word is the question word like "what" or "how."
		 */
		ANSWER,
	}
	
	private CategoryTag[] categories;
	
	StructureTag(CategoryTag... categories) {
		this.categories = categories;
	}
	
	/**
	 * Check if the StructureTag falls under the category described by the
	 * CategoryTag
	 * @param t the category to check for
	 * @return true if the tag falls under that category, false otherwise.
	 */
	public boolean isA(CategoryTag t) {
		for(CategoryTag test : categories) {
			if(test == t)
				return true;
		}
		
		return false;
	}
}
