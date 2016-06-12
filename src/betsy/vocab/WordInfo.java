package betsy.vocab;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.IndexWord;

/**
 * The WordInfo info interface describes a single word.
 * @author jacob
 *
 */
public interface WordInfo {
	/**
	 * Get the word described by this WordInfo
	 * @return the word
	 */
	public String getWord();
	
	/**
	 * Get the base form of the word. If the word is a noun, this is the
	 * singular form. If the word is a verb, this is the present, simple tense.
	 * If the word is an adjective or adverb, comparative/superlative endings
	 * are removed.
	 * @return the word's base form
	 */
	public String getBaseForm();
	/**
	 * Get the word's (or its base form's) representation in WordNet.
	 * @return the IndexWord representing this word or its base form
	 */
	public IndexWord getIndex();
	/**
	 * Get the part of speech of this word, if applicable. Can be a NOUN,
	 * VERB, ADJECTIVE, or ADVERB
	 * @return the part of speech of the word.
	 */
	public POS getPOS();
}
