package betsy.memory;

import betsy.grammar.StructureTag;
import betsy.grammar.WordTree;

/**
 * QuestionMemory is used to answer a user's questions, or find info related to
 * a user's statements.
 * @author jacob
 *
 */
public interface QuestionMemory {
	
	/**
	 * Store a phrase from the user in memory, for use in future memory queries.
	 * @param statement the phrase to store.
	 */
	void storeStatement(WordTree<StructureTag> statement);
	
	/**
	 * Get a statement most related to the provided one
	 * @param statement the statement to search for
	 * @return the closest match to the statement, or null
	 */
	public WordTree<StructureTag> filterStatement(
			WordTree<StructureTag> statement);
	
	/**
	 * Get the phrase most likely to answer the question. A full statement is
	 * not returned, only a phrase to fill in the missing info in the question.
	 * @param question The question phrase
	 * @return the most likely answer to the question, or null
	 */
	public WordTree<StructureTag> filterQuestion(
			WordTree<StructureTag> question);
}
