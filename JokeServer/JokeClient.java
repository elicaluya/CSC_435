/*--------------------------------------------------------

1. Elijah Caluya / Date: 9/29/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeClient.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows:

> java JokeClient
or 
> java JokeClient 140.192.1.22
	- Will run the Joke Client on localhost and will try to connect with server at this address if no argument is given
	- Otherwise will try to connect to server on address given in first argument i.e. 140.192.1.22
	- will connect to only the primary server on port 4545.

> java JokeClient localhost 140.192.1.22
	- Will run the joke client and try to connect to primary server on first argument on port 4545 i.e. localhost
	- Will run the joke client and try to connect to secondary server on second argument on port 4546 i.e. 140.192.1.22
	- Can connect to primary server on port 4545 and secondary server on 4546


5. List of files needed for running the program.

e.g.:

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java


5. Notes:

e.g.:

The Joke client will connect to the primary server at localhost on port 4545 if no argument given.
If one argument given, the joke client will connect to primary server at the address of the argument entered on port 4545.
If a second argument is given, the joke client will be able to connect to the secondary server at the address of the
second address entered on port 4546. After the user enters their name, they will be able to send requests to the primary or
secondary server by pressing the <Enter> button and sending over the name the user entered, and the current joke and proverb state.
The client receives the proper joke or proverb from the joke server and the updated joke and proverb state.
The client maintains a primary and secondary joke and proverb state that are independent of each other. The client is able to
switch who to send requests to by entering 's'. This will switch the address and port that the client sends a request to.
The client can stop the program at anything by entering 'quit'.

----------------------------------------------------------*/


import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


/******************************************************************************************************************************************************************/
// 											Client class for user to connect with server and get joke or proverb
/******************************************************************************************************************************************************************/
public class JokeClient 
{
	// Have the ports and server names easily accessible by making them static.
	// If I had to improve this program I would try and create less static variables in the future.
	static int primaryPort = 4545;			// Primary server port to connect with
	static int secondaryPort = 4546; 		// Secondary server port to connect with
	static int currentPort = primaryPort;	// Set default current port to the primary port

	static String primaryServer = "";				// Variable to store the primary server to communicate to primary joke server with
	static String secondaryServer = "";				// Variable to store the secondary server to communicate to secondary joke server with
	static String currentServer = primaryServer;	// Default current server to primary server

	// Have each client's state for primary and secondary jokes and proverbs stored on the client.
	// This way, it eliminates the need for a UUID and the states can be easily adjusted and kept track of by checking against the strings
	static String jokeState = "";		// Start out with empty Joke state for new client
	static String proverbState = "";	// Start out with empty Proverb state for new client
	static String s2jokeState = "";		// Start out with empty secondary joke state
	static String s2proverbState = "";	// Start out with empty secondary proverb state

	// Boolean values for secondary server functionality
	static Boolean isSecondaryEnabled = false;	// Value to check if user wants to connect to second server
	static Boolean s2Mode = false;				// Switches between primary and secondary mode to send request to primary or secondary server

	public static void main (String args[])
	{
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) primaryServer = "localhost";
		else if (args.length == 1) primaryServer = args[0];		// If one argument entered, set primary server to the argument
		else if (args.length == 2){			// If the user entered 2 arguments for secondary server functionality
			primaryServer = args[0];		// set primary server to the first argument
			secondaryServer = args[1];		// set secondary server to the second argument
			isSecondaryEnabled = true;		// Enable secondary server functionality
		}
		else {		// If user entered too many arguments, alert user then exit the program
			System.out.println("Too many Arguments! JokeClient takes 1, 2, or no arguments.");
			System.exit(0);
		}

		// Display all of the servers being used
		System.out.println("Server one: " + primaryServer + ", Port: " + Integer.toString(primaryPort));
		if (isSecondaryEnabled)		// If secondary server functionality is enabled, display the second server
			System.out.println("Server two: " + secondaryServer + ", Port: " + Integer.toString(secondaryPort));


		// Initialize the buffer to read in user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String name = "";	// String variable to store name the user enters

		// First read and store in name before entering loop to connect with server
		try {
			System.out.println("Enter your name, or 'quit' to stop the program: ");
			name = in.readLine();
			if (name.equals("")){	// Keep asking user for a name if the user did not enter anything
				do {
					System.out.println("You must enter a name");
					name = in.readLine();
				} while (name.equals(""));
			}
			
		} catch (IOException i){
			System.out.println(i);	// Print out exception 
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
				// Display which server is being used and the prompt for the user
				if(!s2Mode){	// Check if the secondary server is being used or not
					System.out.println("Current server: PRIMARY");
					System.out.print("Press <Enter> to get message, 's' to switch between primary/secondary server, or 'quit' to stop program: ");					
				}
				else {
					// Display with secondary server indicator
					System.out.println("<S2> Current server: SECONDARY");
					System.out.print("<S2> Press <Enter> to get message, 's' to switch between primary/secondary server, or 'quit' to stop program: ");	
				}
				System.out.flush();				// Make sure everything written to Standard out is sent

				request = in.readLine();		// Get request in from the user
				if (request.equals("s"))		// If the user wants to switch between primary and secondary server
					switchSecondary();			// Initiate method to switch to primary or secondary server
				
				else if (request.indexOf("quit") < 0)	// Check if string entered is not "quit"
					sendRequest(name, s2Mode);		// Send request to server to get joke or proverb with current state
			// While the input is not quit, keep reading in line and sending info to server
			} while (request.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}

	// Method to switch to secondary mode
	static void switchSecondary()
	{
		if (isSecondaryEnabled){	// Check if secondary server functionality is enabled
			if (s2Mode){				// if in secondary mode, change the current server and port to primary mode
				s2Mode = false;
				currentServer = primaryServer;
				currentPort = primaryPort;
			}						
			else{						// if in primary mode, change the current server and port to secondary mode
				s2Mode = true;
				currentServer = secondaryServer;
				currentPort = secondaryPort;
			}
				
			// Display that the client is communicating with a different server and port
			System.out.println("Now communicating with " + currentServer 
								+ " port " + Integer.toString(currentPort));
			
		} else 	// if secondary server functionality is not available then let the user know
			System.out.println("No secondary server being used");
	}


	/******************************************************************************************************************************************************************/
	// 											Main method used to send request to server with user name, joke state, and proverb state
	/******************************************************************************************************************************************************************/	
	static void sendRequest(String name, Boolean isS2)
	{
		Socket sock;					// Socket variable for connection to server
		PrintStream toServer;			// PrintStrem variable to store output to the server

		try {
			sock = new Socket(currentServer, currentPort);	// Connect to client at given server name and port

			// Set up output stream to go to the server from the socket
			toServer = new PrintStream(sock.getOutputStream());
	

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


	/******************************************************************************************************************************************************************/
	// 									Main method used to get info back from server including joke or proverb, joke state, and proverb state
	/******************************************************************************************************************************************************************/	
	static void getMessage(Socket sock)
	{
		BufferedReader messageFromServer;		// Buffer variable to store output from server
		String textmessageFromServer;			// String variable to store output from server after converted to String

		try {
			// Read the output from the server through the socket and store the data into the buffer
			messageFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			textmessageFromServer = messageFromServer.readLine();	// Store the joke or proverb from server

			

			if (!s2Mode){	// Check whether we are updating primary or secondary joke and proverb states
				// If the output from server is not null, print the joke or proverb on a new line.
				if (textmessageFromServer != null) System.out.println(textmessageFromServer);

				jokeState = messageFromServer.readLine();				// Store the updated joke state from server
				proverbState = messageFromServer.readLine();			// Store the updated proverb state from server
				// If the client finished the primary joke cycle, let client know
				if (jokeState.length() == 8)
					System.out.println("PRIMARY JOKE CYCLE COMPLETED! Will get new joke cycle on next JOKE request");
				// If the client finished the primary proverb cycle, let client know
				if (proverbState.length() == 8)
					System.out.println("PRIMARY PROVERB CYCLE COMPLETED! Will get new proverb cycle on next PROVERB request");
			}
			else {
				// If the output from server is not null, print the joke or proverb on a new line.
				if (textmessageFromServer != null) System.out.println("<S2> " + textmessageFromServer);

				s2jokeState = messageFromServer.readLine();				// Store the updated joke state from server
				s2proverbState = messageFromServer.readLine();			// Store the updated proverb state from server
				// If the client finished the secondary joke cycle, let client know
				if (s2jokeState.length() == 8)
					System.out.println("<S2> SECONDARY JOKE CYCLE COMPLETED! Will get new joke cycle on next JOKE request");
				// If the client finished the secondary proverb cycle, let client know
				if (s2proverbState.length() == 8)
					System.out.println("<S2> SECONDARY PROVERB CYCLE COMPLETED! Will get new proverb cycle on next PROVERB request");
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