package betsy.vocab;

import java.util.List;

/**
 * The names class has static methods and variables that keep track of lists of
 * common male/female first names.
 * @author jacob
 *
 */
public class Names {
	private static final String NAMES_MALE_FILE = "namesMale.txt";
	private static final String NAMES_FEMALE_FILE = "namesFemale.txt";
	
	/**
	 * A list of common male names. Call loadNames() before using.
	 */
	public static List<String> maleNames;
	/**
	 * A list of common female names. Call loadNames() before using.
	 */
	public static List<String> femaleNames;
	
	/**
	 * Load the names from files, and store them in maleNames and femaleNames.
	 */
	public static void loadNames() {
		maleNames = betsy.BetsyMain.getResourceLines(NAMES_MALE_FILE);
		femaleNames = betsy.BetsyMain.getResourceLines(NAMES_FEMALE_FILE);
		for(int i = 0; i < maleNames.size(); i++) {
			maleNames.set(i, maleNames.get(i).toLowerCase());
		}
		for(int i = 0; i < femaleNames.size(); i++) {
			femaleNames.set(i, femaleNames.get(i).toLowerCase());
		}
	}
}
