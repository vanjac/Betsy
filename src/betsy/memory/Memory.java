package betsy.memory;

import java.util.List;
import java.util.ArrayList;
import betsy.grammar.*;

@Deprecated
public interface Memory {
	
	void storeStatement(WordTree<StructureTag> statement);
	
	/**
	 * Filter all statements for statements containing the WordTree
	 * Extra tags in the statement's WordTree will be ignored
	 * @param filters the trees to search for
	 * @return a list of trees that contain the given filters
	 */
	public List<WordTree<StructureTag>> filter(
			List<WordTree<StructureTag>> filters);
	
	public default List<WordTree<StructureTag>> filter(
			WordTree<StructureTag> filter) {
		List<WordTree<StructureTag>> filterList = new ArrayList<>();
		filterList.add(filter);
		return filter(filterList);
	}
}
