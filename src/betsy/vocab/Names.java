package betsy.vocab;

import java.util.List;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
		Path maleFile = Paths.get(NAMES_MALE_FILE);
		Path femaleFile = Paths.get(NAMES_FEMALE_FILE);
		
		try {
			maleNames = Files.readAllLines(maleFile);
			femaleNames = Files.readAllLines(femaleFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		for(int i = 0; i < maleNames.size(); i++) {
			maleNames.set(i, maleNames.get(i).toLowerCase());
		}
		for(int i = 0; i < femaleNames.size(); i++) {
			femaleNames.set(i, femaleNames.get(i).toLowerCase());
		}
	}
}
