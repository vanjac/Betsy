package betsy.memory;

import java.util.*;
import betsy.grammar.StructureTag;
import betsy.grammar.WordTree;

@Deprecated
public class SimpleMemory implements Memory {
	
	private List<WordTree<StructureTag>> statements;
	
	public SimpleMemory() {
		statements = new ArrayList<>();
	}
	
	@Override
	public void storeStatement(WordTree<StructureTag> statement) {
		statements.add(statement);
	}
	
	@Override
	public List<WordTree<StructureTag>> filter(
			List<WordTree<StructureTag>> filters) {
		List<WordTree<StructureTag>> filtered = new ArrayList<>();
		for(WordTree<StructureTag> test : statements) {
			for(WordTree<StructureTag> filter : filters) {
				if(containsTree(test, filter)) {
					filtered.add(test);
					break;
				}
			}
		}
		return filtered;
	}
	
	private boolean containsTree(WordTree<StructureTag> test,
			WordTree<StructureTag> search) {
		if(search.exactMatch(test, true))
			return true;
		if(test.isLeaf()) {
			return false; //no more testing can be done -- trees don't match
		} else {
			for(WordTree<StructureTag> child : test.getChildren()) {
				if(containsTree(child, search))
					return true;
			}
			return false;
		}
	}
}
