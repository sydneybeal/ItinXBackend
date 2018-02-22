package proposalcreator;

import java.util.List;

/** A general interface for a user interface
 * @author vjm
 */
public interface UserInterface {
	/** presents set of commands for user to choose one of
        @param commands the commands to choose from
        @return the index of the command in the array
	 */
	int getCommand (String[] commands);
	int getCommand (String[] commands, String prompt);
	/** tell the user something
	@param message string to print out to the user
	 */
	void sendMessage (String message);
        String selectItem(String[] items, String message);
	/** prompts the user for a string
	@param prompt the request
	@return what the user enters, null if nothing
	 */
	String getInfo (String prompt);    
}
