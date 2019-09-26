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

	static Boolean isJoke = true;
	static int primaryPort = 5050;			// Primary server port to connect to server
	static int secondaryPort = 5051;		// Secondary server port to connect to server

	public static void main (String args[])
	{
		String serverName;
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];

		System.out.println("Elijah Caluya's Joke Client Admin, 1.8.\n");
		System.out.println("using server: " + serverName + ", Port: " + Integer.toString(primaryPort));
		
		// Set up input stream to read user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		// Set default mode to joke mode
		Boolean jokeMode = true;
	
		try {
			String input;
			do {
				if (jokeMode){	//Declare to client admin that the current mode is Joke Mode and prompt to change to Proverb mode
					System.out.println("Current Mode is JOKE MODE");
					System.out.println("Press Enter to change to PROVERB mode or 'quit' to stop admin client");
				}	
				else{	//Declare to client admin that the current mode is Proverb Mode and prompt to change to Joke mode
					System.out.println("Current Mode is PROVERB MODE");
					System.out.println("Press Enter to change to JOKE mode or 'quit' to stop admin client");
				}

				// Every time the user presses enter, the mode is changed on the server
				input = in.readLine();	// Get input from user
				if (input.indexOf("quit") < 0)	// Check if string entered is not "quit"
					jokeMode = changeMode(serverName,jokeMode);	// If not quit, call changeMode() method to change mode of server
			// While the input is not quit, keep reading in line and sending info to server
			} while (input.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}


	/*******************************************************************************************************************/
	// 		Main method used to change the server mode and receive update from server
	/*******************************************************************************************************************/
	static Boolean changeMode(String serverName, Boolean isJoke)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader fromServer;		// Buffer variable to store output from server
		DataOutputStream toServer;		// PrintStrem variable to store output to the server
		String textFromServer;			// String variable to store output from server after converted to String
		

		try {
			sock = new Socket(serverName, primaryPort);	// Connect to client at given server name and port

			// Set the input stream to read in from the socket
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up output stream to send boolean value to server
			toServer = new DataOutputStream(sock.getOutputStream());

			// Send boolean value to server of the mode we want to change the server to
			toServer.writeBoolean(!isJoke);
			toServer.flush();	// Flush output stream

			// Update the saved mode on the client admin
			if (isJoke)
				isJoke = false;
			else
				isJoke = true;

			
			textFromServer = fromServer.readLine();		// Read in update from server that server mode has changed
			// If the output from server is not null, print the output on a new line.
			if (textFromServer != null) System.out.println(textFromServer);
			System.out.println();
			
			// Close connection with server
			sock.close();
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}

		return isJoke;
	}
}