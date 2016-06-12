package betsy.vocab;

import net.sf.extjwnl.data.IndexWord;

/**
 * A WordInfo describing a verb.
 * @author jacob
 *
 */
public class VerbInfo extends GenericWordInfo {
	
	/**
	 * Describes the tense of a verb, including its time and frame. Some frames
	 * have their times combined into one tag, since its difficult to
	 * distinguish between them based on just one word.
	 * @author jacob
	 *
	 */
	public enum VerbType {
		BASE, MODAL, PRESENT_SIMPLE, PAST_SIMPLE, //influences tense of phrase
		CONTINUOUS,
		PERFECT
	}
	
	private final VerbType type;
	
	public VerbInfo(String word, IndexWord index,
			VerbType type) {
		super(word, index);
		this.type = type;
	}
	
	/**
	 * Get the type of the verb. See VerbType for a description.
	 * @return the type of the verb
	 */
	public VerbType getType() {
		return type;
	}

}
