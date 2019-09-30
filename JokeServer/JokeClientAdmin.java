/*--------------------------------------------------------

1. Elijah Caluya / Date: 9/29/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeClientAdmin
or 
> java JokeClientAdmin 140.192.1.22
	- Will run the Joke Client Admin on localhost and will try to connect with server at this address if no argument is given
	- Otherwise will try to connect to server on address given in first argument i.e. 140.192.1.22
	- will connect to only the primary server on port 5050.

> java JokeClientAdmin localhost 140.192.1.22
	- Will run the joke client and try to connect to primary server on first argument on port 5050 i.e. localhost
	- Will run the joke client and try to connect to secondary server on second argument on port 5051 i.e. 140.192.1.22
	- Can connect to primary server on port 5050 and secondary server on 5051

5. List of files needed for running the program.

e.g.:

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:

e.g.:

The Joke Client Admin will connect to only the primary server at localhost on port 5050 if no argument is given.
If there is one argument given, then the Joke Client Admin will try to connect with the primary server at the argument given on port 5050.
If two arguments are entered, then the joke client admin will try to connect to the primary server at that first argument given on port 5050
and to the secondary server at the second argument given on port 5051. After starting the program, the user can change the state of the current
server the client admin is connected to by pressing enter. This will change the server mode to proverb or joke mode. The user can 
switch between the primary and secondary server by entering 's' and quit the program at any time by entering 'quit'.

----------------------------------------------------------*/


import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


/*******************************************************************************************************************/
// 		JokeClientAdmin class used to connect with JokeAdmin to change the mode of the server
/*******************************************************************************************************************/
public class JokeClientAdmin 
{
	// Have the ports and server names easily accessible by making them static.
	// If I had to improve this program I would try and create less static variables in the future.
	static int primaryPort = 5050;			// Primary server port to connect to server
	static int secondaryPort = 5051;		// Secondary server port to connect to server
	static int currentPort = primaryPort;

	static String primaryServer = "";					// String to hold primary server for client admin
	static String secondaryServer = "";					// String to hold secondary server for client admin
	static String currentServer = primaryServer;		// By default set current server to primary server

	// Store the states on the client so we can just send a boolean value over to the server.
	// By default the server starts out in Joke mode and will change to proverb mode.
	// However, if the mode is changed and the JokeClientAdmin is stopped and restarted, the client admin will want to change the
	// server mode to proverb mode by default. It will let the user know what the mode will be changed to and what the current mode is
	static Boolean primaryJokeMode = true;				// By default, primary server starts out in Joke Mode
	static Boolean secondaryJokeMode = true;			// By default, secondary server starts out in Proverb mode
	static Boolean currentJokeMode = primaryJokeMode;	// By default the current server is the primary server

	// Boolean values for secondary server functionality
	static Boolean isSecondaryEnabled = false;			// Secondary server access is not enabled at default
	static Boolean s2Mode = false;						// Value to determine if sending info to secondary or primary server


	public static void main (String args[]) throws FileNotFoundException 
	{
		// If User doesn't provide a server name in arguments, then set the primary server to localhost
		if (args.length < 1) primaryServer = "localhost";
		else if (args.length == 1) primaryServer = args[0];	// If one argument given, set the primary server to this address
		else if (args.length == 2) {		// If a second argument is given then set the primary and secondary servers
			primaryServer = args[0];		// Set the primary server to the first argument
			secondaryServer = args[1];		// Set the secondary server to the second argument
			isSecondaryEnabled = true;		// Enable secondary server functionality
		} else {
			// If there are too many arguments then alert the user and stop the program
			System.out.println("Too many Arguments! JokeClientAdmin takes 1, 2, or no arguments.");
			System.exit(0);
		}

		// Display the servers and ports being used
		System.out.println("Server one: " + primaryServer + ", port: " + Integer.toString(primaryPort));
		if (isSecondaryEnabled)		// If secondary functionality is enabled then display the second server and port
			System.out.println("Server one: " + secondaryServer + ", port: " + Integer.toString(secondaryPort));

		
		// Set up input stream to read user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	
		try {
			String input;
			do {
				if (!s2Mode){
					if (primaryJokeMode){	//Declare to client admin that the current primary server mode is Joke Mode and prompt to change to Proverb mode
						System.out.println("Current PRIMARY Server Mode is JOKE MODE");
						System.out.println("Press <Enter> to change to PROVERB mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}	
					else{	//Declare to client admin that the current primary server mode is Proverb Mode and prompt to change to Joke mode
						System.out.println("Current PRIMARY Server Mode is PROVERB MODE");
						System.out.println("Press <Enter> to change to JOKE mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}
				} else {
					if (secondaryJokeMode){	//Declare to client admin that the secondary server current mode is Joke Mode and prompt to change to Proverb mode
						System.out.println("<S2> Current SECONDARY Server Mode is JOKE MODE");
						System.out.println("<S2> Press <Enter> to change to PROVERB mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}	
					else{	//Declare to client admin that the current secondary server mode is Proverb Mode and prompt to change to Joke mode
						System.out.println("<S2> Current SECONDARY Server Mode is PROVERB MODE");
						System.out.println("<S2> Press <Enter> to change to JOKE mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}
				}
				System.out.flush();

				// Every time the user presses enter, the mode is changed on the server
				input = in.readLine();	// Get input from user
				if (input.equals("s"))	// if the user wants to switch between secondary and primary server
					switchSecondary();	// Inititate the switching of which server to communicate with
				else if (input.indexOf("quit") < 0)	// Check if string entered is not "quit"
					changeMode(s2Mode);	// If not quit, call changeMode() method to change mode of server
			// While the input is not quit, keep reading in line and sending info to server
			} while (input.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}

	// Method to switch between communicating to secondary and primary server
	static void switchSecondary()
	{
		if (isSecondaryEnabled){	// Check if the secondary functionality is enabled
			if (s2Mode){			// If it is in Secondary mode, then switch everything to the primary server
				s2Mode = false;
				currentServer = primaryServer;
				currentPort = primaryPort;
			}
			else{					// If it is in primary mode, then switch everything to the secondary server
				s2Mode = true;
				currentServer = secondaryServer;
				currentPort = secondaryPort;
			}
			// Let user know of the current server and port that they are communicating with
			System.out.println("Now communicating with " + currentServer 		
								+ " port " + Integer.toString(currentPort));

		} else 	// If secondary functionality is not enabled then let the user know
			System.out.println("No secondary server being used\n");
	}


	/*******************************************************************************************************************/
	// 		Main method used to change the server mode and receive update from server
	/*******************************************************************************************************************/
	static void changeMode(Boolean isS2)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader fromServer;		// Buffer variable to store output from server
		// Usage for DataOutputStream obtained through https://docs.oracle.com/javase/7/docs/api/java/io/DataOutputStream.html
		DataOutputStream toServer;		// PrintStrem variable to store output to the server
		

		try {
			sock = new Socket(currentServer, currentPort);	// Connect to server at the current server name and port

			// Set the input stream to read in String from the socket
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up output stream to send boolean value to server
			toServer = new DataOutputStream(sock.getOutputStream());


			if (!isS2){		// Check if communicating to secondary or primary server
				toServer.writeBoolean(isS2);				// Send to server if communicated through secondary or primary server
				toServer.writeBoolean(!primaryJokeMode);	// Send boolean value to server of the mode we want to change the server to
				toServer.flush();	// Flush output stream
				// Update the saved primary mode on the client admin
				if (primaryJokeMode)
					primaryJokeMode = false;
				else
					primaryJokeMode = true;

				String textFromPrimaryServer = fromServer.readLine();		// Read in update from server that server mode has changed
				// If the output from server is not null, print the output on a new line.
				if (textFromPrimaryServer != null) System.out.println(textFromPrimaryServer);
			}
			else{
				toServer.writeBoolean(isS2);				// Send to server if communicated through secondary or primary server
				toServer.writeBoolean(!secondaryJokeMode);	// Send boolean value to server of the mode we want to change the server to
				toServer.flush();	// Flush output stream
				// Update the saved secondary mode on the client admin
				if (secondaryJokeMode)
					secondaryJokeMode = false;
				else
					secondaryJokeMode = true;

				String textFromSecondaryServer = fromServer.readLine();		// Read in update from server that server mode has changed
				// If the output from server is not null, print the output on a new line with secondary server indicator.
				if (textFromSecondaryServer != null) System.out.println("<S2> " + textFromSecondaryServer);
			}
			
			System.out.println();	// For easier to read output
			
			// Close connection with server
			sock.close();
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}
	}
}