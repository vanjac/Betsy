package betsy.vocab;

import net.sf.extjwnl.data.IndexWord;

/**
 * A WordInfo describing an adjective.
 * @author jacob
 *
 */
public class AdjectiveInfo extends GenericWordInfo {
	
	/**
	 * An adjective can be:
	 * NORMAL: "fast"
	 * COMPARATIVE: "faster"
	 * SUPERLATIVE: "fastest"
	 * @author jacob
	 *
	 */
	public enum AdjectiveType {
		NORMAL, COMPARATIVE, SUPERLATIVE;
	}
	private final AdjectiveType type;
	
	public AdjectiveInfo(String word, IndexWord index, AdjectiveType type) {
		super(word, index);
		this.type = type;
	}
	
	/**
	 * Get the type of the adjective. See AdjectiveType for a description of
	 * the three types.
	 * @return the word's AdjectiveType
	 */
	public AdjectiveType getType() {
		return type;
	}

}
