package betsy.vocab;

import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;

/**
 * A generic implementation of WordInfo. Keeps track of the word and its
 * IndexWord, which it uses for other word properties.
 * @author jacob
 *
 */
public abstract class GenericWordInfo implements WordInfo{
	
	private String word;
	private IndexWord index;
	
	public GenericWordInfo(String word, IndexWord index) {
		this.word = word;
		this.index = index;
	}
	
	@Override
	public String getWord() {
		return word;
	}
	
	@Override
	public String getBaseForm() {
		if(index != null)
			return index.getLemma();
		else
			return word;
	}
	
	@Override
	public IndexWord getIndex() {
		return index;
	}
	
	@Override
	public POS getPOS() {
		return index.getPOS();
	}
	
	@Override
	public String toString() {
		if(index != null)
			return index.toString();
		else
			return word;
	}
	
}
