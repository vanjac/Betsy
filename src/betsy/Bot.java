package betsy;

/**
 * An interface for a Chatbot.
 * @author jacob
 *
 */
public interface Bot {
	
	/**
	 * Get the name of the bot
	 * @return the Bot's name
	 */
	String getName();
	
	/**
	 * Initiate a new conversation. Can be called multiple times during the
	 * lifetime of the bot.
	 * @param generateResponse Whether to say a welcome message to the user
	 * @return a welcome message if generateResponse is true
	 */
	String init(boolean generateResponse);
	
	/**
	 * Interpret user's input, and optionally respond
	 * @param text the user's message
	 * @param generateResponse whether to respond to the user
	 * @return a response if generateResponse is true
	 */
	String interpret(String text, boolean generateResponse);
	
	/**
	 * End the conversation. Can be called multiple times during the lifetime
	 * of the bot.
	 * @param generateResponse Whether to say a closing message to the user
	 * @return a closing message if generateResponse is true
	 */
	String close(boolean generateResponse);
	
}
