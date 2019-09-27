/*--------------------------------------------------------

1. Elijah Caluya / Date: 9/29/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac JokeServer.java


4. Precise examples / instructions to run this program:

e.g.:

In separate shell windows, can be started in any order:

> java JokeServer
	- This command will run the primary Joke Server on localhost
	- This primary server will accept connections to this address and at port 4545
	- Will run a new thread for each new connection from client to primary server

> java JokeServer secondary
	- This command will run the secondary Joke server on localhost
	- This secondary server will accept connection to this address and at port 4546
	- Will run a new thread for each new connection from client to secondary server


5. List of files needed for running the program.

e.g.:

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

5. Notes:

The Joke server will accept connections from a joke client and receive a name, joke state, and proverb state of the client.
Depending on the current mode (Joke/Proverb), the server will send back a joke or proverb back to the client that the client has not received.
This is kept track of in the Joke state and Proverb state sent by the client. The server reads in these states and sends a random joke
or proverb that the client has not seen. Once the cycle of the 4 jokes or 4 proverbs have been sent, the cycle will reset in a different order
and the client will receive a different order of jokes or proverbs on their next request.

The mode of the joke server is changed by the JokeClientAdmin.java file. This file will connect to the Primary Admin server on port 5050 
and the secondary server on port 5051. Requests from the JokeClientAdmin will toggle the current mode of the server switching from
Joke to Proverb mode or Proverb to Joke Mode. A request to the primary server will change the mode of the primary server only and 
a request to the secondary channel will only change the secondary server mode.


----------------------------------------------------------*/


import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries
import java.util.*;	// For getting random Integer


/******************************************************************************************************************************************************************/
// 													Worker Class is a subclass of built in Thread class to use Thread functionality
/******************************************************************************************************************************************************************/
class Worker extends Thread 
{

	Socket sock;		// Create local variable for socket object
	static Boolean isJoke;	// Variable to store if proverb or joke request
	static Boolean s2Mode;	// Variable to store if from secondary or primary server 
	Worker (Socket s, Boolean j, Boolean s2) {sock = s; isJoke = j; s2Mode = s2;}		// Worker class constructor makes the local variable sock to Socket argument given

	// Override Thread.run() method for our own implementation 
	public void run()
	{
		PrintStream out = null;		// PrintStream variable so we can write data to Output Stream back to the client
		BufferedReader in = null;	// Create a buffer to read text from character-input stream from the client

		String jokeState = "";		// Variable to store joke state from the client
		String proverbState = "";	// Variable to store proverb state from the client

		try {
			// Initialize buffer variable to read the input stream from the client connection
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			// Initialize our PrintStream variable to write to the OutputStream of the socket (back to the client)
			out = new PrintStream(sock.getOutputStream());
			

			try {

				String name;	// The name we get in from the client
				// Read in the name sent in from the client and store it
				name = in.readLine();

				// Read in the state of the jokes and proverbs for client
				jokeState = in.readLine();
				proverbState = in.readLine();
				
				// Send back to client the joke or proverb as well as the updated joke and proverb state
				sendMessage(name, out, jokeState, proverbState, s2Mode);

			} catch (IOException x) {
				// Throw error if there is a problem reading from the buffer from the socket
				System.out.println("Server read error");
				x.printStackTrace();		// Print out IOException data to console log
			}

			// Close the connection with the socket
			sock.close();
		// Print error if there is problem initializing variables for our input and output streams
		} catch (IOException ioe) {System.out.println(ioe);}
	}



	/******************************************************************************************************************************************************************/
	//									Main method for sending a random joke or proverb back to client along with their respective updated state
	/******************************************************************************************************************************************************************/
	static void sendMessage (String name, PrintStream toClient, String jState, String pState, Boolean isS2)
	{
		String message = "";	// String to store message to client
		
		if (isJoke){	// If server is set to Joke Mode
			if (jState.length() == 8){	// Check if the state has already seen all 4 jokes
				jState = "";			// if so create a new empty state to send back to client
				if (!isS2){
					// Print out joke cycle completed for the user and that the joke state will be reset
					System.out.println("JOKE CYCLE COMPLETED! Resetting jokes for " + name);
				} 
				else {
					// Print out joke cycle completed for the user and that the joke state will be reset
					System.out.println("<S2> JOKE CYCLE COMPLETED! Resetting jokes for " + name);
				}
			}
			
			// Set the message back to the client to the joke given the current joke state of the client
			message = getJoke(jState,name);
			
			// Update the joke state for the client
			jState = jState + message.substring(0,2);
			
			
		}
		else{			// If the server is set to proverb mode
			if (pState.length() == 8){	// Check if the state has already seen all 4 jokes
				pState = "";			// If so create a new empty state to send back to client
				if (!isS2){
					// Print out Proverb cycle completed for the user and that the proverb state will reset
					System.out.println("PROVERB CYCLE COMPLETED! Resetting proverbs for " + name);
				}
				else {
					// Print out Proverb cycle completed for the user and that the proverb state will reset
					System.out.println("<S2> PROVERB CYCLE COMPLETED! Resetting proverbs for " + name);
				}
			}

			// Set the message back to the client to the proverb given the current proverb state of the client
			message = getProverb(pState, name);
			
			// Update the proverb state for the client
			pState = pState + message.substring(0,2);
		}

		toClient.println(message);	// Send Joke or proverb back to client
		toClient.println(jState);	// Send back joke state back to client
		toClient.println(pState);	// Send back proverb state back to client
		toClient.flush();	// flush output stream

		if (!isS2){
			// If on Primary Server print out standard message
			System.out.println("Sent " + message.substring(0,2) + " to: " + name);
		} else {
			// If on Secondary Server print out standard secondary message
			System.out.println("<S2> Sent " + message.substring(0,2) + " to: " + name);
		}
	}



	/******************************************************************************************************************************************************************/
	//											Methods for determining the right random joke or proverb to send according to joke state
	/******************************************************************************************************************************************************************/

	// Method to get random joke index according to state of joke state from client
	static int randomJokeIndex(String state)
	{
		String[] jokeIDs = {"JA","JB","JC","JD"};	// array of Joke IDs
		Random random = new Random();				
		int randIndex = random.nextInt(4);			// Get random number either 0, 1, 2, or 3

		// If the ID of the joke is already in the state, keep getting a new random number until its the index of a new ID
		while (state.contains(jokeIDs[randIndex]))
			randIndex = random.nextInt(4);
		
		return randIndex;	// Return the random joke index back
	}

	// Method to get random proverb index according to state of proverb state from client
	static int randomProverbIndex(String state)
	{
		String[] proverbIDs = {"PA","PB","PC","PD"};	// array of proverb IDs
		Random random = new Random();
		int randIndex = random.nextInt(4);				// Get random number either 0, 1, 2, or 3

		// If the ID of the proverb is already in the state, keep getting a new random number until itsthe index of a new ID
		while (state.contains(proverbIDs[randIndex]))
			randIndex = random.nextInt(4);

		return randIndex;	// Return the random Proverb index back
	}


	// Method to get joke string according to state of client
	static String getJoke(String state, String name)
	{
		// All the jokes we will use with IDs included in the front
		String[] jokes = {	"JA : What's brown and sticky? A stick",
							"JB : A blind person was eating seafood. It didn't help",
							"JC : I invented a new word! Plagiarism!",
							"JD : Why do we tell actors to 'break a leg'? Because every play has a cast"
						};
		
		// Get the index of the jokes array to get the joke we will send back to client
		int index = randomJokeIndex(state);

		// Return the joke string with the name of the user inserted after the Joke ID
		return jokes[index].substring(0,2) + " " + name + jokes[index].substring(2,jokes[index].length());	
	}


	// Method to get proverb string according to state of client
	static String getProverb(String state, String name)
	{
		// All the proverbs we will use with IDs included in the front
		String[] proverbs = {	"PA : Every now and then a blind pig snorts up a truffle",
								"PB : A bad workman always blames his tools",
								"PC : An idle brain is the devil's workshop",
								"PD : Don't bite the hand that feeds you"
							};

		// Get the index of the proverbs array to get the proverb we will send back to client
		int index = randomProverbIndex(state);

		// Return the proverb string with the name of the user inserted after the Proverb ID
		return proverbs[index].substring(0,2) + " " + name + proverbs[index].substring(2,proverbs[index].length());
	}	
}



/******************************************************************************************************************************************************************/
// 							Class for JokeServer that is the main server that handles JokeClient connections and starts new JokeAdmin thread
// 												to get connections from JokeClientAdmin to change the server mode.
/******************************************************************************************************************************************************************/
public class JokeServer 
{
	// By default both of the servers start out in Joke mode
	static Boolean primaryJokeMode = true;
	static Boolean secondaryJokeMode = true;
	// By default the server is using only primary server
	static Boolean secondaryMode = false;

	public static void main(String args[]) throws IOException 
	{
		int q_len = 6;				// Number of requests for OpSys to queue
		int primaryPort = 4545;		// Primary port number to connect to client
		int secondaryPort= 4546;	// Secondary port number to connect to client
		Socket sock;				// Socket variable to connect with client
		
		// If the user enters more than one argument or an invalid argument then alert user and exit program
		if (args.length > 1 && !args[0].equals("secondary")){
			System.out.println("Invalid Arugments! JokeServer takes 'secondary' as argument or no arguments.");
			System.exit(0);
		}

		// Check if Secondary server needs to be run by checking argument from user
		if (args.length == 1 && args[0].equals("secondary")){
			// Need to create a new thread for the Secondary Admin server to change state of Secondary Server
			JokeAdminSecondary adminSecondary = new JokeAdminSecondary();		// Create new instance of JokeAdminSecondary class
			Thread secondAdminThread = new Thread(adminSecondary);			// Create new thread to run JokeAdminSecondary class
			secondAdminThread.start();	// start thread

			// Construct server socket on set port and with max queue length for incoming connection requests
			ServerSocket secondServSock = new ServerSocket(secondaryPort, q_len);

			// Display that the secondary server is up and running on secondary port
			System.out.println("Elijah Caluya's Secondary Joke server 1.8 starting up, listening at port " 
									+ Integer.toString(secondaryPort) + "\n");

			while (true) {						// Infinite loop:
				sock = secondServSock.accept();		// Wait for client connection
				// Create new Worker thread once client connection is accepted and do code in run() method with start() call
				new Worker(sock, secondaryJokeMode,!secondaryMode).start();		
			}
		}

		// If Primary needs to be run
		else {
			JokeAdmin admin = new JokeAdmin();		// Create new instance of JokeAdmin class
			Thread adminThread = new Thread(admin);			// Create new thread to run JokeAdmin class
			adminThread.start();								// start thread

			// Construct server socket on set port and with max queue length for incoming connection requests
			ServerSocket primaryServSock = new ServerSocket(primaryPort, q_len);

			// Display that the primary server is up and running on primary port
			System.out.println("Elijah Caluya's Primary Joke server 1.8 starting up, listening at port " 
							+ Integer.toString(primaryPort) + "\n");


			while (true) {						// Infinite loop:
				sock = primaryServSock.accept();		// Wait for client connection
				// Create new Worker thread once client connection is accepted and do code in run() method with start() call
				new Worker(sock, primaryJokeMode,secondaryMode).start();		
			}
		}

		
	}
}



/******************************************************************************************************************************************************************/
//											Admin classes for handling JokeClientAdmin connections
/******************************************************************************************************************************************************************/

// AdminWorker class thread that will set the mode of the server depending on what the JokeClientAdmin sends over
class AdminWorker extends Thread
{
	Socket sock;

	AdminWorker(Socket s) {sock = s;}

	public void run()
	{
		// Input and output streams for getting info from client
		DataInputStream fromClient;
		PrintStream toClient;

		try {
			// Data Input stream to read in boolean value sent in by the client
			fromClient = new DataInputStream(sock.getInputStream());
			// Output stream to send back to client a message of the current mode of the server
			toClient = new PrintStream(sock.getOutputStream());

			// Read in from client if communicating with primary or secondary server
			Boolean isS2 = fromClient.readBoolean();	

			// Read in the Boolean value sent in from the JokeClientAdmin to change server mode to
			Boolean jokeMode = fromClient.readBoolean();

	
			if (jokeMode){				// If the client wants to set the mode to Joke Mode
				if (!isS2){		// Check if communicating from primary or secondary server
					JokeServer.primaryJokeMode = true;		// Set the server mode to Joke Mode
					System.out.println("Primary Server set to Joke Mode");	// Print out on server that the server is set to Joke Mode
					toClient.println("Primary Server set to Joke Mode");	// Send back to client message that Joke Mode is set	
				}
				else {
					JokeServer.secondaryJokeMode = true;		// Set the server mode to Joke Mode
					System.out.println("<S2> Secondary Server set to Joke Mode");	// Print out on server that the server is set to Joke Mode
					toClient.println("Secondary Server set to Joke Mode");	// Send back to client message that Joke Mode is set	
				}
			} else {				// If the client wants to set the mode to Proverb mode
				if (!isS2){
					JokeServer.primaryJokeMode = false;		// Set the server mode to Proverb Mode
					System.out.println("Primary Server set to Proverb Mode");	// Print out on server that the server is set to Proverb Mode
					toClient.println("Primary Server set to Proverb Mode");		// Send back to client message that Proverb Mode is set
				}
				else {
					JokeServer.secondaryJokeMode = false;		// Set the server mode to Joke Mode
					System.out.println("<S2> Secondary Server set to Proverb Mode");	// Print out on server that the server is set to Joke Mode
					toClient.println("Secondary Server set to Proverb Mode");	// Send back to client message that Joke Mode is set	
				}

			toClient.flush();		// Flush the output stream
			
			// Close connection with server
			sock.close();
			}
		} catch (IOException i){
			System.out.println(i);
		}
	}
}


// Main Joke Admin that will create new threads for each primary JokeClientAdmin connection
class JokeAdmin implements Runnable 
{
	public void run()
	{
		int q_len = 6;			// Number of requests for OpSys to queue
		int port = 5050;		// Primary port number
		Socket sock;			// Socket variable to connect with client

		try {
			// Construct server socket on set port and with max queue length for incoming connection requests
			ServerSocket servsock = new ServerSocket(port, q_len);

			while (true) {
				sock = servsock.accept();		// Accept connections from JokeClientAdmin
				new AdminWorker(sock).start();	// Start new thread each time JokeClientAdmin connects to primary server
			}
		} catch (IOException i) {System.out.println(i);}
		
	}
}

// Secondary Joke Admin that will create new threads for each Secondary JokeClientAdmin connection
class JokeAdminSecondary implements Runnable 
{
	public void run()
	{
		int q_len = 6;			// Number of requests for OpSys to queue
		int secondaryPort = 5051;		// Secondary port number
		Socket sock;			// Socket variable to connect with client

		try {
			// Construct server socket on set port and with max queue length for incoming connection requests
			ServerSocket servsock = new ServerSocket(secondaryPort, q_len);

			while (true) {
				sock = servsock.accept();		// Accept connections from JokeClientAdmin
				new AdminWorker(sock).start();	// Start new thread each time JokeClientAdmin connects to secondary server
			}
		} catch (IOException i) {System.out.println(i);}
		
	}
}