package betsy.grammar;

/**
 * SentenceConstructors take a tree of StructureTags and turn them back into
 * a human-readable sentence.
 * @author jacob
 *
 */
public interface SentenceConstructor {
	/**
	 * Create a phrase or sentence from the WordTree
	 * @param tree the tree of tags. Expected to have a layout as defined in
	 * StructureTag.
	 * @param addPunctuation if true, punctuation will be added to the end of
	 * the sentence and the first letter will be capitalized.
	 * @return a phrase/sentence string created from the given tree
	 */
	public String constructSentence(WordTree<StructureTag> tree,
			boolean addPunctuation);
}
