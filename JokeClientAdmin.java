/*--------------------------------------------------------

1. Elijah Caluya / Date: 9/29/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeClientAdmin.java
> java JokeClientAdmin


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

e.g.:

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

e.g.:

I faked the random number generator. I have a bug that comes up once every
ten runs or so. If the server hangs, just kill it and restart it. You do not
have to restart the clients, they will find the server again when a request
is made.

----------------------------------------------------------*/


import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


/*******************************************************************************************************************/
// 		JokeClientAdmin class used to connect with JokeAdmin to change the mode of the server
/*******************************************************************************************************************/
public class JokeClientAdmin 
{

	
	static int primaryPort = 5050;			// Primary server port to connect to server
	static int secondaryPort = 5051;		// Secondary server port to connect to server
	static int currentPort = primaryPort;

	static String primaryServer = "";				// String to hold primary server for client admin
	static String secondaryServer = "";				// String to hold secondary server for client admin
	static String currentServer = primaryServer;	// By default set current server to primary server

	static Boolean primaryJokeMode = true;			// By default, primary server starts out in Joke Mode
	static Boolean secondaryJokeMode = true;		// By default, secondary server starts out in Proverb mode
	static Boolean currentJokeMode = primaryJokeMode;

	static Boolean isSecondaryEnabled = false;		// Secondary server access is not enabled at default
	static Boolean s2Mode = false;					// Value to determine if sending info to secondary or primary server

	public static void main (String args[])
	{
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) primaryServer = "localhost";
		else if (args.length == 1) primaryServer = args[0];
		else if (args.length == 2) {
			primaryServer = args[0];
			secondaryServer = args[1];
			isSecondaryEnabled = true;
		} else {
			System.out.println("Too many Arguments! JokeClientAdmin takes 1, 2, or no arguments.");
			System.exit(0);
		}

		System.out.println("Server one: " + primaryServer + ", port: " + Integer.toString(primaryPort));
		if (isSecondaryEnabled)
			System.out.println("Server one: " + secondaryServer + ", port: " + Integer.toString(secondaryPort));

		
		// Set up input stream to read user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	
		try {
			String input;
			do {
				if (!s2Mode){
					if (primaryJokeMode){	//Declare to client admin that the current mode is Joke Mode and prompt to change to Proverb mode
						System.out.println("Current PRIMARY Server Mode is JOKE MODE");
						System.out.println("Press <Enter> to change to PROVERB mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}	
					else{	//Declare to client admin that the current mode is Proverb Mode and prompt to change to Joke mode
						System.out.println("Current PRIMARY Server Mode is PROVERB MODE");
						System.out.println("Press <Enter> to change to JOKE mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}
				} else {
					if (secondaryJokeMode){	//Declare to client admin that the current mode is Joke Mode and prompt to change to Proverb mode
						System.out.println("<S2> Current SECONDARY Server Mode is JOKE MODE");
						System.out.println("<S2> Press <Enter> to change to PROVERB mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}	
					else{	//Declare to client admin that the current mode is Proverb Mode and prompt to change to Joke mode
						System.out.println("<S2> Current SECONDARY Server Mode is PROVERB MODE");
						System.out.println("<S2> Press <Enter> to change to JOKE mode, 's' to switch between primary/secondary server, or 'quit' to stop admin client");
					}
				}
				

				// Every time the user presses enter, the mode is changed on the server
				input = in.readLine();	// Get input from user
				if (input.equals("s"))
					switchSecondary();
				else if (input.indexOf("quit") < 0)	// Check if string entered is not "quit"
					changeMode(s2Mode);	// If not quit, call changeMode() method to change mode of server
			// While the input is not quit, keep reading in line and sending info to server
			} while (input.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}

	static void switchSecondary()
	{
		if (isSecondaryEnabled){
			if (s2Mode){
				s2Mode = false;
				currentServer = primaryServer;
				currentPort = primaryPort;
			}
			else{
				s2Mode = true;
				currentServer = secondaryServer;
				currentPort = secondaryPort;
			}
			System.out.println("Now communicating with " + currentServer 
								+ " port " + Integer.toString(currentPort));

		} else 
			System.out.println("No secondary server being used\n");
	}


	/*******************************************************************************************************************/
	// 		Main method used to change the server mode and receive update from server
	/*******************************************************************************************************************/
	static void changeMode(Boolean isS2)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader fromServer;		// Buffer variable to store output from server
		DataOutputStream toServer;		// PrintStrem variable to store output to the server
		

		try {
			sock = new Socket(currentServer, currentPort);	// Connect to client at given server name and port

			// Set the input stream to read in from the socket
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up output stream to send boolean value to server
			toServer = new DataOutputStream(sock.getOutputStream());


			if (!isS2){
				toServer.writeBoolean(isS2);
				toServer.writeBoolean(!primaryJokeMode);	// Send boolean value to server of the mode we want to change the server to
				toServer.flush();	// Flush output stream
				// Update the saved mode on the client admin
				if (primaryJokeMode)
					primaryJokeMode = false;
				else
					primaryJokeMode = true;

				String textFromPrimaryServer = fromServer.readLine();		// Read in update from server that server mode has changed
				// If the output from server is not null, print the output on a new line.
				if (textFromPrimaryServer != null) System.out.println(textFromPrimaryServer);
			}
			else{
				toServer.writeBoolean(isS2);
				toServer.writeBoolean(!secondaryJokeMode);	// Send boolean value to server of the mode we want to change the server to
				toServer.flush();	// Flush output stream
				// Update the saved mode on the client admin
				if (secondaryJokeMode)
					secondaryJokeMode = false;
				else
					secondaryJokeMode = true;

				String textFromSecondaryServer = fromServer.readLine();		// Read in update from server that server mode has changed
				// If the output from server is not null, print the output on a new line.
				if (textFromSecondaryServer != null) System.out.println("<S2> " + textFromSecondaryServer);
			}
			
			
			System.out.println();
			
			// Close connection with server
			sock.close();
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}
	}
}