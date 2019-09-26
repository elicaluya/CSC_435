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
	static int currentPort = primaryPort;

	static String primaryServer = "";
	static String s2ServerName = "";
	static String currentServer = primaryServer;

	static String jokeState = "";		// Start out with empty Joke state for new client
	static String proverbState = "";	// Start out with empty Proverb state for new client

	static String s2jokeState = "";		// Start out with empty secondary joke state
	static String s2proverbState = "";	// Start out with empty secondary proverb state

	static Boolean isSecondaryEnabled = false;	// Value to check if user wants to connect to second server
	static Boolean s2Mode = false;				// Switches between primary and secondary mode to send request to primary or secondary server

	public static void main (String args[])
	{
		
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) primaryServer = "localhost";
		else if (args.length == 1) primaryServer = args[0];
		else if (args.length == 2){
			primaryServer = args[0];
			s2ServerName = args[1];
			isSecondaryEnabled = true;
		}
		else {
			System.out.println("Too many Arguments! JokeClient takes 1, 2, or no arguments.");
			System.exit(0);
		}

		System.out.println("Server one: " + primaryServer + ", Port: " + Integer.toString(primaryPort));
		if (isSecondaryEnabled)
			System.out.println("Server two: " + s2ServerName + ", Port: " + Integer.toString(secondaryPort));


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
				if(!s2Mode){
					System.out.println("Current server: PRIMARY");
					System.out.print("Press <Enter> to get message, 's' to switch between primary/secondary server, or 'quit' to stop program: ");	
					System.out.flush();				// Make sure everything written to Standard out is sent
				}
				else {
					System.out.println("<S2> Current server: SECONDARY");
					System.out.print("<S2> Press <Enter> to get message, 's' to switch between primary/secondary server, or 'quit' to stop program: ");	
					System.out.flush();				// Make sure everything written to Standard out is sent
				}
				request = in.readLine();
				if (request.equals("s"))
					switchSecondary(isSecondaryEnabled);
				
				else if (request.indexOf("quit") < 0)	// Check if string entered is not "quit"
					sendRequest(name, s2Mode);		// Send request to server to get joke or proverb with current state
			// While the input is not quit, keep reading in line and sending info to server
			} while (request.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}

	// Method to switch to secondary mode
	static void switchSecondary(Boolean isS2)
	{
		if (isS2){
			if (currentServer.equals(primaryServer))
				currentServer = primaryServer;
			else
				currentServer = s2ServerName;

			if (currentPort == primaryPort)
				currentPort = secondaryPort;
			else
				currentPort = primaryPort;
			System.out.println("Now communicating with " + currentServer 
								+ " port " + Integer.toString(currentPort));
			if (s2Mode)
				s2Mode = false;
			else
				s2Mode = true;
		} else 
			System.out.println("No secondary server being used\n");
	}


	/*******************************************************************************************************************/
	// 				Main method used to send request to server with user name, joke state, and proverb state
	/*******************************************************************************************************************/	
	static void sendRequest(String name, Boolean isS2)
	{
		Socket sock;					// Socket variable for connection to server
		PrintStream toServer;			// PrintStrem variable to store output to the server
		DataOutputStream s2out;

		try {
			sock = new Socket(currentServer, currentPort);	// Connect to client at given server name and port

			// Set up output stream to go to the server from the socket
			toServer = new PrintStream(sock.getOutputStream());
			s2out = new DataOutputStream(sock.getOutputStream());

			s2out.writeBoolean(isS2);
			s2out.flush();

			toServer.println(name);			// Send user name to server
			if (!isS2){
				toServer.println(jokeState);	// Send joke state to server
				toServer.println(proverbState);	// Send proverb state to server
			} else {
				toServer.println(s2jokeState);	// Send joke state to server
				toServer.println(s2proverbState);	// Send proverb state to server
			}
			
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
			if (!s2Mode){
				jokeState = messageFromServer.readLine();				// Store the updated joke state from server
				proverbState = messageFromServer.readLine();			// Store the updated proverb state from server
			}
			else {
				s2jokeState = messageFromServer.readLine();				// Store the updated joke state from server
				s2proverbState = messageFromServer.readLine();			// Store the updated proverb state from server
			}

			// If the output from server is not null, print the joke or proverb on a new line.
			if (textmessageFromServer != null) System.out.println(textmessageFromServer);
			
			// If the client finished the primary joke cycle, let client know
			if (jokeState.length() == 8)
				System.out.println("PRIMARY JOKE CYCLE COMPLETED! Will get new joke cycle on next request");
			// If the client finished the primary proverb cycle, let client know
			if (proverbState.length() == 8)
				System.out.println("PRIMARY PROVERB CYCLE COMPLETED! Will get new proverb cycle on next request");
			// If the client finished the secondary joke cycle, let client know
			if (s2jokeState.length() == 8)
				System.out.println("<S2> SECONDARY JOKE CYCLE COMPLETED! Will get new joke cycle on next request");
			// If the client finished the secondary proverb cycle, let client know
			if (s2proverbState.length() == 8)
				System.out.println("<S2> SECONDARY PROVERB CYCLE COMPLETED! Will get new proverb cycle on next request");
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