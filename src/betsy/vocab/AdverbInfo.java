package betsy.vocab;

import net.sf.extjwnl.data.IndexWord;
import betsy.vocab.AdjectiveInfo.AdjectiveType;

/**
 * A WordInfo describing an adverb.
 * @author jacob
 *
 */
public class AdverbInfo extends GenericWordInfo {
	
	private final AdjectiveType type;
	
	public AdverbInfo(String word, IndexWord index, AdjectiveType type) {
		super(word, index);
		this.type = type;
	}
	
	/**
	 * Get the type of the adverb. These are the same types used for
	 * adjectives.
	 * @return the adverb's AdjectiveType.
	 */
	public AdjectiveType getType() {
		return type;
	}

}
