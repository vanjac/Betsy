package betsy.memory;

import java.util.List;
import java.io.PrintStream;
import java.util.ArrayList;
import betsy.grammar.StructureTag;
import betsy.grammar.WordTree;
import static betsy.grammar.StructureTag.CategoryTag.*;

/**
 * An implementation of QuestionMemory. Compares the search phrase to
 * statements in memory, and assigns each a score recursively based on how
 * closely they match each other. The highest score wins, unless that score is
 * 0, in which case nobody wins.
 * @author jacob
 *
 */
public class ScoredQuestionMemory implements QuestionMemory {

	private List<WordTree<StructureTag>> statements;
	private final PrintStream logOut;
	
	public ScoredQuestionMemory(PrintStream logOut) {
		statements = new ArrayList<>();
		this.logOut = logOut;
	}
	
	@Override
	public void storeStatement(WordTree<StructureTag> statement) {
		statements.add(statement);
	}
	
	@Override
	public WordTree<StructureTag> filterQuestion(
			WordTree<StructureTag> question) {
		return filterStatement(question);
	}

	@Override
	public WordTree<StructureTag> filterStatement(
			WordTree<StructureTag> statement) {
		float highestScore = 0;
		WordTree<StructureTag> bestMatch = null;
		for(WordTree<StructureTag> test : statements) {
			System.out.println("Testing: " + test.wordListToString());
			float score = getScore(statement, test);
			if(score >= highestScore && score != 0) {
				highestScore = score;
				bestMatch = test;
			}
			logOut.println("Score: " + score);
		}
		
		return bestMatch;
	}
	
	private float getScore(WordTree<StructureTag> question,
			WordTree<StructureTag> answer) {
		int score = 0;
		int total = 0;
		
		// including IGNOREDs but not including ANSWERs
		List<String> qLeafWords = new ArrayList<>();
		List<String> aLeafWords = new ArrayList<>();
		List<WordTree<StructureTag>> qTrees = new ArrayList<>();
		List<WordTree<StructureTag>> aTrees = new ArrayList<>();
		
		for(WordTree<StructureTag> child : question.getChildren()) {
			if(child.isLeaf()) {
				if(!child.getType().isA(ANSWER))
					qLeafWords.add(child.getWord());
			} else {
				qTrees.add(child);
			}
		}
		
		for(WordTree<StructureTag> child : answer.getChildren()) {
			if(child.isLeaf()) {
				if(!child.getType().isA(ANSWER))
					aLeafWords.add(child.getWord());
			} else {
				aTrees.add(child);
			}
		}
		
		//find matching leaves:
		if(qLeafWords.size() > aLeafWords.size())
			total += qLeafWords.size();
		else
			total += aLeafWords.size();
		for(String w : qLeafWords) {
			if(aLeafWords.contains(w)) {
				score++;
				aLeafWords.remove(w);
			}
		}
		
		//find the best combinations of trees
		//TODO: order should not matter
		if(qTrees.size() > aTrees.size())
			total += qTrees.size();
		else
			total += aTrees.size();
		for(WordTree<StructureTag> qTree : qTrees) {
			float highestScore = 0;
			WordTree<StructureTag> bestMatch = null;
			for(WordTree<StructureTag> aTree : aTrees) {
				float treeScore = getScore(qTree, aTree);
				if(treeScore > highestScore) {
					highestScore = treeScore;
					bestMatch = aTree;
				}
			}
			if(bestMatch != null) {
				score += highestScore;
				aTrees.remove(bestMatch);
			}
		}
		
		if(total == 0)
			return 0;
		else
			return (float)score / (float)total;
	}
	
}
