package betsy.grammar;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * A WordTree is used to group and arrange strings in a tree structure.
 * There are two types of WordTree: a leaf has a single String -- the "word",
 * and a tree has a list of other WordTrees -- its children. Both have an
 * object -- the "type" -- used to identify and describe them. WordTree is a
 * generic class so this object could potentially be anything. For Betsy, it is
 * usually a Tag or a StructureTag.
 * The WordTree's children can change, but everything else is immutable.
 * @author jacob
 *
 * @param <T> the type of object used to tag each tree or leaf
 */
public class WordTree<T> {
	private final T type;
	private final boolean isLeaf;
	private final String word;
	private final List<WordTree<T>> children;
	private WordTree<T> parent;
	
	/**
	 * Construct a leaf WordTree, with a word
	 * @param type the WordTree's identifying type
	 * @param word the leaf's word
	 */
	public WordTree(T type, String word) {
		this.type = type;
		this.isLeaf = true;
		this.word = word;
		children = Collections.unmodifiableList(new ArrayList<>(0));
		parent = null;
	}
	
	/**
	 * Construct a tree WordTree, with a list of children. This list could
	 * change later.
	 * @param type the WordTree's identifying type 
	 * @param children the initial children of the tree
	 */
	public WordTree(T type, List<WordTree<T>> children) {
		this.type = type;
		isLeaf = false;
		this.word = null;
		this.children = children;
		for(WordTree<T> child : children) {
			initChild(child);
		}
	}
	
	/**
	 * Construct a tree WordTree. It initially has no children.
	 * @param type the WordTree's identifying type
	 */
	public WordTree(T type) {
		this.type = type;
		isLeaf = false;
		this.word = null;
		this.children = new ArrayList<>();
	}
	
	/**
	 * Set the parent of the tree. This is only called by other word trees.
	 * @param p the new parent of the tree
	 */
	private void setParent(WordTree<T> p) {
		parent = p;
	}
	
	/**
	 * Get the identifying "type" object of the tree
	 * @return the tree's type, of type T
	 */
	public T getType() {
		return type;
	}
	
	/**
	 * Check if the tree is a leaf. Leaves have a word and no children; trees
	 * may have children but do not have a word.
	 * @return true if the tree is a leaf
	 */
	public boolean isLeaf() {
		return isLeaf;
	}
	
	/**
	 * Get the WordTree's word, if it is a leaf.
	 * @return if the WordTree is a leaf, returns the leaf's word. If it is a
	 * tree, returns null.
	 */
	public String getWord() {
		return word;
	}
	
	/**
	 * Get the WordTree's children, if it is a tree
	 * @return an unmodifiable list. If the WordTree is a tree, the list will
	 * have its children; otherwise it will be empty.
	 */
	public List<WordTree<T>> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	/**
	 * Collapses all of the leaves in this tree, its children, their children,
	 * etc. using a depth-first search. The leaves are put together in order,
	 * in a list.
	 * @return a list of WordTree's, all of which are leaves. If this WordTree
	 * is a leaf, the list will have one item -- this tree. If it is a tree, it
	 * will call getAllLeaves() for each of its children and collect their
	 * responses, in order, in a list.
	 */
	public List<WordTree<T>> getAllLeaves() {
		List<WordTree<T>> leaves = new ArrayList<>();
		if(isLeaf()) {
			leaves.add(this);
		} else {
			for(WordTree<T> child : getChildren())
				leaves.addAll(child.getAllLeaves());
		}
		return leaves;
	}
	
	/**
	 * Get the number of children this WordTree has
	 * @return the number of children returned by getChildren(). If this is a
	 * leaf, returns 0.
	 */
	public int numChildren() {
		return children.size();
	}
	
	/**
	 * Get the child at the specific index.
	 * @param i the index of the child. Must be between 0 (inclusive) and
	 * numChildren() (exclusive)
	 * @return the child WordTree at that index
	 */
	public WordTree<T> getChild(int i) {
		return children.get(i);
	}
	
	/**
	 * Check if one of the tree's direct children has the specified type.
	 * @param type the type to search for
	 * @return true if this WordTree has a direct child with this type. False
	 * if this is a leaf.
	 */
	public boolean hasType(T type) {
		for(WordTree<T> child : children) {
			if(child.getType().equals(type))
				return true;
		}
		return false;
	}
	
	/**
	 * Get the first direct child of this tree with the specified type.
	 * @param type the type to search for
	 * @return the first one of this tree's direct children with that type.
	 * Null if none are found, or if this is a leaf.
	 */
	public WordTree<T> getType(T type) {
		for(WordTree<T> child : children) {
			if(child.getType().equals(type))
				return child;
		}
		return null;
	}
	
	/**
	 * Get the index of the first direct child of this tree with the
	 * specified type.
	 * @param type the type to search for
	 * @return the index of the first one of this tree's direct children with
	 * that type. -1 if none are found, or if this is a leaf.
	 */
	public int getTypeIndex(T type) {
		int i = 0;
		for(WordTree<T> child : children) {
			if(child.getType().equals(type))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Get all direct children of this tree with the specified type.
	 * @param type the type to search for
	 * @return a list of each of this tree's direct children with that type.
	 * Return an empty list if none are found, or if this is a leaf.
	 */
	public List<WordTree<T>> getAllType(T type) {
		List<WordTree<T>> all = new ArrayList<>();
		for(WordTree<T> child : children) {
			if(child.getType().equals(type))
				all.add(child);
		}
		return all;
	}
	
	/**
	 * Check if one of the tree's direct children 1) is a leaf, 2) has the
	 * specified type, and 3) has the exact value of the specified word.
	 * @param type the type to search for
	 * @param word the word to search for
	 * @return true if this WordTree has a direct child meeting these criteria.
	 * False if this is a leaf.
	 */
	public boolean hasLeaf(T type, String word) {
		for(WordTree<T> child : children) {
			if(child.isLeaf() && child.getType().equals(type)
					&& child.getWord().equals(word))
				return true;
		}
		return false;
	}
	
	/**
	 * Find the first direct child of this tree meeting the criteria described
	 * in hasLeaf().
	 * @param type the type to search for
	 * @param word the word to search for
	 * @return the first of this tree's children meeting the criteria. Null if
	 * none are found or if this is a leaf.
	 */
	public WordTree<T> getLeaf(T type, String word) {
		for(WordTree<T> child : children) {
			if(child.isLeaf() && child.getType().equals(type)
					&& child.getWord().equals(word))
				return child;
		}
		return null;
	}
	
	/**
	 * Find the index of the first direct child of this tree meeting the
	 * criteria described in hasLeaf().
	 * @param type the type to search for
	 * @param word the word to search for
	 * @return the index of the first of this tree's children meeting the
	 * criteria. -1 if none are found or if this is a leaf.
	 */
	public int getLeafIndex(T type, String word) {
		int i = 0;
		for(WordTree<T> child : children) {
			if(child.isLeaf() && child.getType().equals(type)
					&& child.getWord().equals(word))
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Get the "parent" of this WordTree - the tree that contains this tree as
	 * a child.
	 * @return this tree's parent, or null if none exists
	 */
	public WordTree<T> getParent() {
		return parent;
	}
	
	/**
	 * Add the given tree to this tree's children. Its parent will be
	 * automatically set. Behavior is undefined if this is a leaf.
	 * @param tree the new child
	 * @return the newly added child -- exactly the same as the input
	 */
	public WordTree<T> addChild(WordTree<T> tree) {
		children.add(tree);
		initChild(tree);
		return tree;
	}
	
	/**
	 * Remove the given tree from this tree's children. Its parent will be set
	 * to null. If the tree isn't a child of this tree, nothing happens. If
	 * this is a leaf, nothing happens.
	 * @param tree the child to remove
	 */
	public void removeChild(WordTree<T> tree) {
		if(children.contains(tree)) {
			children.remove(tree);
			tree.setParent(null);
		}
	}
	
	/**
	 * Remove the child at the specified index. Behavior is undefined if the
	 * index is out of range, or if this is a leaf.
	 * @param i the index of the child to remove
	 */
	public void removeChild(int i) {
		removeChild(getChild(i));
	}
	
	/**
	 * Add the specified tree to this tree's parent, and move this tree into
	 * the specified tree.
	 * @param tree the tree to replace this one with
	 * @return the specified tree
	 */
	public WordTree<T> insertTree(WordTree<T> tree) {
		parent.addChild(tree);
		tree.addChild(this);
		return tree;
	}
	
	private void initChild(WordTree<T> tree) {
		if(tree.getParent() != null)
			tree.getParent().removeChild(tree);
		tree.setParent(this);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(getType());
		
		if(isLeaf()) {
			sb.append(" ");
			sb.append(getWord());
		} else {
			for(WordTree<T> t : getChildren()) {
				sb.append("\n");
				sb.append(indentString(t.toString()));
			}
			//sb.append("\n");
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	/**
	 * Collapse all the leaves in this tree using the same method as
	 * getAllLeaves(). Then put spaces between each of the leaf's words, and
	 * combine them into a single string.
	 * @return the combined words of the leaves in the entire word tree
	 */
	public String wordListToString() {
		if(isLeaf())
			return getWord();
		else {
			StringBuilder sb = new StringBuilder();
			for(WordTree<T> t : getChildren()) {
				if(sb.length() > 0)
					sb.append(" ");
				sb.append(t.wordListToString());
			}
			
			return sb.toString();
		}
	}
	
	private String indentString(String s) {
		return "  " + s.replace("\n", "\n  ");
	}
	
	/**
	 * Create a full copy of this tree, its children, their children, etc.
	 * @return a new tree with the same structure as this one, but new object
	 * instances.
	 */
	public WordTree<T> clone() {
		if(isLeaf()) {
			return new WordTree<T>(getType(), getWord());
		} else {
			WordTree<T> t = new WordTree<T>(getType());
			for(WordTree<T> child : getChildren()) {
				t.addChild(child.clone());
			}
			return t;
		}
	}
	
	/**
	 * Check if this tree matches another. Children, or children's children,
	 * etc., can be in different orders, as long as they are the same values.
	 * @param tree2 the tree to compare this tree to
	 * @param ignoreExtraChildren if true, any extra children in tree2 or its
	 * children that aren't in this tree will be ignored
	 * @return true if the trees match, as specified above
	 */
	public boolean exactMatch(WordTree<T> tree2, boolean ignoreExtraChildren) {
		if(!getType().equals(tree2.getType()))
			return false;
		if(isLeaf() != tree2.isLeaf())
			return false;
		if(isLeaf()) {
			return getWord().equals(tree2.getWord());
		} else {
			if(!ignoreExtraChildren
					&& getChildren().size() != tree2.getChildren().size())
				return false;
			for(WordTree<T> child : getChildren()) {
				boolean tree2ChildFound = false;
				for(WordTree<T> child2 : tree2.getChildren()) {
					if(child.exactMatch(child2, ignoreExtraChildren)) {
						tree2ChildFound = true;
						break;
					}
				}
				if(!tree2ChildFound)
					return false;
			}
			
			return true;
		}
	}
}
