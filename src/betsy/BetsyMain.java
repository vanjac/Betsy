package betsy;

// number of uncommented lines of code: \n[\s]*[^/*\s]

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import betsy.log.*;

/**
 * Betsy's main class. Creates a LogFrame, an instance of Betsy, and lets the
 * user talk to it. Has a collection of commands, starting with a '\', that
 * the user can use to control the bot.
 * @author jacob
 *
 */
public class BetsyMain {
	
	public static LogFrame logger;
	
	// input and output streams:
	public static PrintStream out; // for non-bot messages to the user
	public static BufferedReader in; // for user input
	public static PrintStream logOut; // for debugging and demonstration
	public static PrintStream botOut; // for messages from the bot
	
	private static Bot bot;
	
	// information for commands to access
	private static String lastUserMessage;
	private static String lastBotMessage;
	private static boolean conversationComplete;
	
	public static List<String> getResourceLines(String s) {
		InputStream stream = ClassLoader.getSystemResourceAsStream(s);
		return new BufferedReader(new InputStreamReader(stream,
				StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
	}
	
	
	/**
	 * Create the chatbot.
	 * @return a new Bot
	 */
	private static Bot makeBot() {
		return new BetsyBot(logOut);
	}
	
	public static void main(String[] args) throws IOException {
		logOut = System.out;
		logOut.println("Setting up user interface...");
		logger = new LogFrame();
		out = new PrintStream(logger.outputStream());
		in = new BufferedReader(new InputStreamReader(logger.inputStream()));
		botOut = out;
		
		while(!logger.isReady());
		out.println("Please wait...");
		
		logOut.println("Creating bot...");
		bot = makeBot();
		logOut.println("  Done.");
		
		out.println();
		
		// initialize the bot and print a welcome message
		botOut.println(bot.init(true));
		
		conversationComplete = false;
		while(!conversationComplete) {
			if(logger.resetClicked()) {
				bot.close(false);
				out.println("Please wait...");
				out.println();
				botOut.println(bot.init(true));
			}
			
			out.print("> "); // prompt
			logger.enable(); // will only have an effect the first time
			String input = in.readLine();
			logger.disable();
			input = input.trim();
			
			
			if(input.isEmpty())
				continue;
			
			if(input.charAt(0) == '\\') { // input is a command
				processCommand(input);
			} else { // input is a message to the bot
				botInterpret(input);
				lastUserMessage = input;
			}
		}
		
		botOut.println(bot.close(true));
		
		logger.close();
	}
	
	/**
	 * Send a message to the bot, print its response to botOut, and save the
	 * response.
	 * @param input the message
	 */
	private static void botInterpret(String input) {
		try {
			String botResponse = bot.interpret(input, true);
			botOut.println(botResponse);
			lastBotMessage = botResponse;
		} catch (Exception e) {
			e.printStackTrace();
			botOut.println("ERROR!");
		}
	}
	
	/**
	 * Process a command given by the user
	 * @param command the command string. Commands start with a '\' followed by
	 * the command name, and any arguments separated by spaces.
	 */
	private static void processCommand(String command) {
		command = command.substring(1).toLowerCase();
		String[] words = command.split(" ", 0);
		
		if(words[0].isEmpty()) {
			return;
		}
		if(words[0].charAt(0) == '\\') {
			botInterpret(command);
			lastUserMessage = command;
			return;
		}
		if(words[0].equals("exit")) {
			conversationComplete = true;
			return;
		}
		if(words[0].equals("repeat")) {
			if(lastUserMessage != null)
				botInterpret(lastUserMessage);
			return;
		}
		if(words[0].equals("botrepeat")) {
			int timesRepeat = 1;
			if(words.length > 1)
				timesRepeat = Integer.parseInt(words[1]);
			if(lastBotMessage != null) {
				for(int i = 0; i < timesRepeat; i++)
					botInterpret(lastBotMessage);
			}
			return;
		}
		if(words[0].equals("nothing")) {
			botInterpret("");
			return;
		}
		if(words[0].equals("init")) {
			botOut.println(bot.init(true));
			return;
		}
		if(words[0].equals("close")) {
			botOut.println(bot.close(true));
			return;
		}
		if(words[0].equals("newbot")) {
			bot = makeBot();
			return;
		}
		if(words[0].equals("restart")) {
			botOut.println(bot.close(true));
			botOut.println(bot.init(true));
			return;
		}
		if(words[0].equals("reset")) {
			botOut.println(bot.close(true));
			bot = makeBot();
			botOut.println(bot.init(true));
			return;
		}
		
		out.println("Unrecognized command!");
		return;
	}
}
