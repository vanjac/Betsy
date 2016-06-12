package betsy.vocab;

import net.sf.extjwnl.data.IndexWord;

/**
 * A WordInfo describing a noun.
 * @author jacob
 *
 */
public class NounInfo extends GenericWordInfo {
	
	private final boolean plural;
	private final boolean pronoun;
	private final boolean question;
	
	public NounInfo(String word, IndexWord index,
			boolean plural, boolean pronoun, boolean question) {
		super(word, index);
		this.plural = plural;
		this.pronoun = pronoun;
		this.question = question;
	}
	
	/**
	 * Check whether the noun is plural. If it is, the base form should be its
	 * singular form.
	 * @return true if the noun is plural
	 */
	public boolean isPlural() {
		return plural;
	}
	
	/**
	 * Check if the noun is a pronoun like "he," "she," "it," "who," etc.
	 * @return true if the noun is a pronoun.
	 */
	public boolean isPronoun() {
		return pronoun;
	}
	
	/**
	 * Check if the noun is a question pronoun: who, what, when, where, why,
	 * how, whom, whose, which. For these words isPronoun() should also be
	 * true.
	 * @return true if the noun is a question pronoun
	 */
	public boolean isQuestion() {
		return question;
	}
}
