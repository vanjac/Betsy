package betsy.grammar;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * Utilities for working with Tokens. A token is a single word or punctuation
 * mark in a sentence. It can be represented by a CoreLabel (used by the
 * Stanford Parser) or a normal String (used everywhere else in this program).
 * @author jacob
 *
 */
public class TokenUtils {
	
	/**
	 * Given a list of tokens, split at sentence terminators (defined by
	 * isSentenceTerminator) into sentences. Include the sentence terminator
	 * in the sentence that it terminates.
	 * @param tokens a list of edu.stanford.nlp.ling.CoreLabel's. Their values
	 * are the tokens in the sentence(s).
	 * @return a list of CoreLabel lists. Each list is a sentence.
	 */
	public static List<List<CoreLabel>> splitSentences(
			List<CoreLabel> tokens) {
		List<List<CoreLabel>> sentences = new ArrayList<>();
		List<CoreLabel> currentSentence = new ArrayList<>();
		for(CoreLabel token : tokens) {
			if(isSentenceTerminator(token)) {
				if(!currentSentence.isEmpty()) {
					currentSentence.add(token);
					sentences.add(currentSentence);
					currentSentence = new ArrayList<>();
				}
			} else {
				currentSentence.add(token);
			}
		}
		if(!currentSentence.isEmpty())
			sentences.add(currentSentence);
		return sentences;
	}
	
	/**
	 * Determine if the token is a sentence terminator -- a punctuation mark
	 * that ends a sentence. This includes '.' (period), '?' (question mark),
	 * '!' (exclamation point), and ';' (semicolon). Semicolons aren't really
	 * sentence terminators, but Betsy treats a sentence with a semicolon as
	 * two separate sentences.
	 * @param token the token to test
	 * @return true if the token is a sentence terminator or semicolon, false
	 * otherwise.
	 */
	public static boolean isSentenceTerminator(CoreLabel token) {
		String s = token.word();
		return s.equals(".") || s.equals("?") || s.equals("!")
				|| s.equals(";");
	}
	
	/**
	 * Split a string into tokens. The string is split at whitespace characters
	 * into words, with punctuation marks treated as separate words. Characters
	 * that aren't alphanumeric or apostrophes are treated as whitespace.
	 * @param s the string to split into tokens
	 * @return an array of tokens. None of the strings should have whitespace,
	 * and punctuation marks should have their own tokens.
	 */
	public static String[] tokenize(String s) {
		List<String> strings = new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		
		boolean whitespace = true;
		for(char c : s.toCharArray()) {
			if(isPunctuation(c)) {
				if(sb.length() != 0) {
					strings.add(sb.toString());
					sb = new StringBuilder();
				}
				strings.add(Character.toString(c));
			} else if(isWhitespace(c)) {
				whitespace = true;
			} else {
				if(whitespace) {
					whitespace = false;
					if(sb.length() != 0) {
						strings.add(sb.toString());
						sb = new StringBuilder();
					}
				}
				sb.append(c);
			}
		}
		if(sb.length() != 0) {
			strings.add(sb.toString());
		}
		
		//convert to array
		String[] stringArray = new String[strings.size()];
		int i = 0;
		for(String string : strings) {
			stringArray[i++] = string;
		}
		
		return stringArray;
	}
	
	/**
	 * Check if the character is treated as whitespace by the tokenizer.
	 * Every character that isn't alphanumeric or an apostrophe is treated as
	 * whitespace.
	 * @param c the character to test
	 * @return true if the character is a whitespace character
	 */
	public static boolean isWhitespace(char c) {
		if(c >= '0' && c <= '9')
			return false;
		if(c >= 'a' && c <= 'z')
			return false;
		if(c >= 'A' && c <= 'Z')
			return false;
		if(c == '\'')
			return false;
		
		return true;
	}
	
	/**
	 * Check if the character is a punctuation mark, worthy of its own token.
	 * @param c the character to test
	 * @return true if the character is a punctuation mark
	 */
	public static boolean isPunctuation(char c) {
		if(c == '.' || c == '?' || c == '!' || c == ',' || c == ';')
			return true;
		if(c == '`' || c == '\"' || c == '(' || c == ')')
			return true;
		return false;
	}
	
	/**
	 * Given a list of tokens, reconstruct a phrase. This follows the same
	 * rules as the tokenizer, but in reverse. Words have single spaces in
	 * between. Punctuation marks are given no space before them and a single
	 * space after them.
	 * @param tokens the tokens, in the form of CoreLabels
	 * @return the final, reconstructed sentence(s)
	 */
	public static String detokenize(Collection<CoreLabel> tokens) {
		StringBuilder sb = new StringBuilder();
		for(CoreLabel l : tokens) {
			String s = l.toString();
			if(s.length() == 0) {
				continue;
			} else if(s.length() == 1 && isPunctuation(s.charAt(0))) {
				sb.append(s);
			} else {
				if(sb.length() != 0)
					sb.append(' ');
				sb.append(s);
			}
		}
		return sb.toString();
	}
	
}
