package betsy.grammar;

import edu.stanford.nlp.trees.Tree;
import java.util.List;
import java.util.ArrayList;

/**
 * Tags used by Penn Treebank to categorize words and phrases in a sentence.
 * @author jacob
 *
 */
public enum Tag {
	
	// sources:
	// catalog.ldc.upenn.edu/docs/LDC99T42/tagguid1.pdf
	// web.mit.edu/6.863/www/PennTreebankTags.html
	// www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html
	
	ROOT("ROOT", StructuralType.OTHER),
	UNKNOWN("", StructuralType.OTHER),
	
	// words
	//! means done
	ADJECTIVE("JJ", StructuralType.WORD, WordType.ADJECTIVE), //!
	ADJECTIVE_COMPARATIVE("JJR", StructuralType.WORD, WordType.ADJECTIVE), //!
	ADJECTIVE_SUPERLATIVE("JJS", StructuralType.WORD, WordType.ADJECTIVE), //!
	ADVERB("RB", StructuralType.WORD, WordType.ADVERB), //!
	ADVERB_COMPARATIVE("RBR", StructuralType.WORD, WordType.ADVERB), //!
	ADVERB_SUPERLATIVE("RBS", StructuralType.WORD, WordType.ADVERB), //!
	CARDINAL_NUMBER("CD", StructuralType.WORD),
	COORDINATING_CONJUNCTION("CC", StructuralType.WORD),
	DETERMINER("DT", StructuralType.WORD), //!
	EXISTENTIAL_THERE("EX", StructuralType.WORD),
	FOREIGN_WORD("FW", StructuralType.WORD), //!
	INTERJECTION("UH", StructuralType.WORD), //!
	LIST_ITEM_MARKER("LS", StructuralType.WORD), //!
	MODAL_VERB("MD", StructuralType.WORD, WordType.VERB), //!
	NOUN_PLURAL("NNS", StructuralType.WORD, WordType.NOUN), //!
	NOUN_SINGULAR_OR_MASS("NN", StructuralType.WORD, WordType.NOUN), //!
	PARTICLE("RP", StructuralType.WORD),
	PERSONAL_PRONOUN("PRP", StructuralType.WORD, WordType.NOUN), //!
	POSSESSIVE_ENDING("POS", StructuralType.WORD), //!
	POSSESSIVE_PRONOUN("PRP$", StructuralType.WORD),  //!
	POSSESSIVE_WH_PRONOUN("WP$", StructuralType.WORD),
	PREDETERMINER("PDT", StructuralType.WORD),
	PREPOSITION_OR_SUBORDINATING_CONJUNCTION("IN", StructuralType.WORD),
	PROPER_NOUN_PLURAL("NNPS", StructuralType.WORD, WordType.NOUN), //!
	PROPER_NOUN_SINGULAR("NNP", StructuralType.WORD, WordType.NOUN), //!
	SYMBOL("SYM", StructuralType.WORD), //!
	TO("TO", StructuralType.WORD),
	VERB_BASE_FORM("VB", StructuralType.WORD, WordType.VERB), //!
	VERB_PAST_TENSE("VBD", StructuralType.WORD, WordType.VERB), //!
	VERB_GERUND_OR_PRESENT_PARTICIPLE("VBG", StructuralType.WORD,
			WordType.VERB), //!
	VERB_PAST_PARTICIPLE("VBN", StructuralType.WORD, WordType.VERB), //!
	VERB_PRESENT_TENSE("VBP", StructuralType.WORD, WordType.VERB), //!
	VERB_PRESENT_TENSE_3RD_PERSON_SINGULAR("VBZ", StructuralType.WORD,
			WordType.VERB), //!
	WH_DETERMINER("WDT", StructuralType.WORD),
	WH_PRONOUN("WP", StructuralType.WORD, WordType.NOUN),
	WH_ADVERB("WRB", StructuralType.WORD, WordType.ADVERB),
	
	// phrases
	ADJECTIVE_PHRASE("ADJP", StructuralType.PHRASE),
	ADVERB_PHRASE("ADVP", StructuralType.PHRASE),
	CONJUNCTION_PHRASE("CONJP", StructuralType.PHRASE),
	FRAGMENT("FRAG", StructuralType.PHRASE),
	INTERJECTION_PHRASE("INTJ", StructuralType.PHRASE),
	LIST_MARKER("LST", StructuralType.PHRASE),
	NOT_A_CONSTITUENT("NAC", StructuralType.PHRASE),
	NOUN_PHRASE("NP", StructuralType.PHRASE),
	HEAD_OF_NOUN_PHRASE("NX", StructuralType.PHRASE),
	PREPOSITIONAL_PHRASE("PP", StructuralType.PHRASE),
	PARENTHETICAL("PRN", StructuralType.PHRASE),
	PARTICLE_PHRASE("PRT", StructuralType.PHRASE),
	QUANTIFIER_PHRASE("QP", StructuralType.PHRASE),
	REDUCED_RELATIVE_CLAUSE("RRC", StructuralType.PHRASE),
	UNLIKE_COORDINATED_PHRASE("UCP", StructuralType.PHRASE),
	VERB_PHRASE("VP", StructuralType.PHRASE),
	WH_ADJECTIVE_PHRASE("WHADJP", StructuralType.PHRASE),
	WH_ADVERB_PHRASE("WHADVP", StructuralType.PHRASE),
	WH_NOUN_PHRASE("WHNP", StructuralType.PHRASE),
	WH_PREPOSITIONAL_PHRASE("WHPP", StructuralType.PHRASE),
	X("X", StructuralType.PHRASE),
	
	// clauses
	SIMPLE_DECLARATIVE_CLAUSE("S", StructuralType.CLAUSE),
	SUBORDINATING_CONJUNCTION_CLAUSE("SBAR", StructuralType.CLAUSE),
	WH_DIRECT_QUESTION("SBARQ", StructuralType.CLAUSE),
	INVERTED_DECLARATIVE_SENTENCE("SINV", StructuralType.CLAUSE),
	// when used top-level, is a yes/no question
	// can also be used within a WH_DIRECT_QUESTION
	INVERTED_YES_NO_OR_WH_QUESTION_CLAUSE("SQ", StructuralType.CLAUSE),
	
	// punctuation
	DOLLAR("S", StructuralType.PUNCTUATION),
	OPENING_QUOTATION_MARK("``", StructuralType.PUNCTUATION),
	CLOSING_QUOTATION_MARK("\"", StructuralType.PUNCTUATION),
	OPENING_PARENTHESIS("(", StructuralType.PUNCTUATION),
	CLOSING_PARENTHESIS(")", StructuralType.PUNCTUATION),
	COMMA(",", StructuralType.PUNCTUATION),
	DASH("--", StructuralType.PUNCTUATION),
	SENTENCE_TERMINATOR(".", StructuralType.PUNCTUATION),
	COLON_OR_ELLIPSIS(":", StructuralType.PUNCTUATION),
	
	// other (undocumented)
	APOSTROPHE("''", StructuralType.PUNCTUATION);
	
	;
	
	
	public enum StructuralType {
		WORD, PHRASE, CLAUSE, PUNCTUATION, OTHER;
	}
	
	public enum WordType {
		ADJECTIVE, ADVERB, VERB, NOUN, OTHER;
	}
	
	private final String acronym;
	private final StructuralType structuralType;
	private final WordType wordType;
	
	Tag(String acronym, StructuralType structuralType) {
		this.acronym = acronym;
		this.structuralType = structuralType;
		wordType = WordType.OTHER;
	}
	
	Tag(String acronym, StructuralType structuralType, WordType wordType) {
		this.acronym = acronym;
		this.structuralType = structuralType;
		this.wordType = wordType;
	}
	
	/**
	 * Get the acronym used by Penn Treebank for this tag
	 * @return the acronym, in all caps
	 */
	public String getAcronym() {
		return acronym;
	}
	
	public StructuralType getStructuralType() {
		return structuralType;
	}
	
	public WordType getWordType() {
		return wordType;
	}
	
	/**
	 * Get the tag with the specified acronym
	 * @param s the acronym to check for, in all caps
	 * @return the matching tag, or UNKNOWN if not found.
	 */
	public static Tag fromString(String s) {
		for(Tag p : Tag.values()) {
			if(s.equals(p.getAcronym()))
				return p;
		}
		
		return UNKNOWN;
	}
	
	/**
	 * From an edu.stanford.nlp.trees.Tree, create a WordTree of Tags,
	 * converting the Penn Treebank acronyms used in the Tree to tags
	 * @param t the tree to convert
	 * @return a WordTree of tags, ideally identical to the Tree
	 */
	public static WordTree<Tag> fromTree(Tree t) {
		boolean isLeaf = t.depth() == 1;
		
		Tag type = Tag.fromString(t.label().toString());
		
		if(isLeaf) {
			String word = t.getChild(0).label().toString().toLowerCase();
			return new WordTree<Tag>(type, word);
		} else {
			int numChildren = t.numChildren();
			List<WordTree<Tag>> children = new ArrayList<>(numChildren);
			for(Tree tree : t.children()) {
				children.add(fromTree(tree));
			}
			return new WordTree<Tag>(type, children);
		}
	}
}
