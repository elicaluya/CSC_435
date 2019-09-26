/*--------------------------------------------------------

1. Elijah Caluya / Date: 9/29/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeClient.java
> java JokeClient


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
// 					Client class for user to connect with server and get joke or proverb
/*******************************************************************************************************************/
public class JokeClient 
{
	static int primaryPort = 4545;			// Primary server port to connect with
	static int secondaryPort = 4546; 		// Secondary server port to connect with

	static String jokeState = "";		// Start out with empty Joke state for new client
	static String proverbState = "";	// Start out with empty Proverb state for new client

	public static void main (String args[])
	{
		String serverName;
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];

		System.out.println("Elijah Caluya's Joke Client, 1.8.\n");
		System.out.println("using server: " + serverName + ", Port: " + Integer.toString(primaryPort));
		// Initialize the buffer to read in user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String name = "";	// String variable to store name the user enters

		// Get name in from user and store in variable "name"
		try {
			System.out.println("Enter your name, or 'quit' to stop the program: ");
			name = in.readLine();
		} catch (IOException i){
			System.out.println(i);
		}

		// If the user wants to quit the program before entering their name
		if (name.equals("quit")){
			System.out.println("Cancelled by user request");
			System.exit(0);
		}

		// Read in user input to send request to server to get joke or proverb
		try {
			String request;
			do {
				System.out.print("Press enter to send request to server or 'quit' to stop program: ");	
				System.out.flush();				// Make sure everything written to Standard out is sent
				request = in.readLine();			// read in anything input from user as long not 'quit'
				if (request.indexOf("quit") < 0)	// Check if string entered is not "quit"
					sendRequest(name, serverName);		// Send request to server to get joke or proverb with current state
			// While the input is not quit, keep reading in line and sending info to server
			} while (request.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}


	/*******************************************************************************************************************/
	// 				Main method used to send request to server with user name, joke state, and proverb state
	/*******************************************************************************************************************/	
	static void sendRequest(String name, String serverName)
	{
		Socket sock;					// Socket variable for connection to server
		PrintStream toServer;			// PrintStrem variable to store output to the server

		try {
			sock = new Socket(serverName, primaryPort);	// Connect to client at given server name and port

			// Set up output stream to go to the server from the socket
			toServer = new PrintStream(sock.getOutputStream());


			toServer.println(name);			// Send user name to server
			toServer.println(jokeState);	// Send joke state to server
			toServer.println(proverbState);	// Send proverb state to server
			toServer.flush();				// flush output stream

			getMessage(sock);	// Receive the info sent back from the server

		} catch (IOException i) {System.out.println(i);}
	}


	/*******************************************************************************************************************/
	// 		Main method used to get info back from server including joke or proverb, joke state, and proverb state
	/*******************************************************************************************************************/	
	static void getMessage(Socket sock)
	{
		BufferedReader messageFromServer;		// Buffer variable to store output from server
		String textmessageFromServer;			// String variable to store output from server after converted to String

		try {
			// Read the output from the server through the socket and store the data into the buffer
			messageFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			textmessageFromServer = messageFromServer.readLine();	// Store the joke or proverb from server
			jokeState = messageFromServer.readLine();				// Store the updated joke state from server
			proverbState = messageFromServer.readLine();			// Store the updated proverb state from server
			

			// If the output from server is not null, print the joke or proverb on a new line.
			if (textmessageFromServer != null) System.out.println(textmessageFromServer);
			
			// If the client finished the joke cycle, let client know
			if (jokeState.length() == 8)
				System.out.println("JOKE CYCLE COMPLETED! Will get new joke cycle on next request");
			// If the client finished the proverb cycle, let client know
			if (proverbState.length() == 8)
				System.out.println("PROVERB CYCLE COMPLETED! Will get new proverb cycle on next request");
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