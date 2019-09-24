import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries
import java.util.*;


// Class for client to connect to server
public class JokeClient 
{

	static HashSet<String> jokeState = new HashSet<>();
	static HashSet<String> proverbState = new HashSet<>();

	public static void main (String args[])
	{
		String serverName;
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];

		System.out.println("Clark Elliott's Inet Client, 1.8.\n");
		System.out.println("using server: " + serverName + ", Port: 1565");
		// Initialize the buffer to read in user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String name = "";

	
		try {
			System.out.println("Enter your name: ");
			name = in.readLine();

		} catch (IOException i){
			System.out.println(i);
		}


		try {
			String request;
			do {
				System.out.print("Press enter to send request to server or 'quit': ");
				System.out.flush();				// Make sure everything written to Standard out is sent
				request = in.readLine();			// Set String variable to string line read in from buffer
				if (request.indexOf("quit") < 0)	// Check if string entered is not "quit"
					getRequest(name, serverName, jokeState, proverbState);	// If not quit, call getRequest() method to read output from server
			// While the input is not quit, keep reading in line and sending info to server
			} while (name.indexOf("quit") < 0);			// Else, print out message and then end program.
				System.out.println("Cancelled by user request.");
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console
	}


	// Method for converting byte IP address to string 
	static String toText (byte ip[])
	{
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < ip.length; ++i){
			if (i > 0) result.append(".");
				result.append(0xff & ip[i]);
		}
		return result.toString();
	}


	// Method for sending and receiving data to server from client
	static void getRequest(String name, String serverName, HashSet<String> jokeSet, HashSet<String> proverbSet)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader messageFromServer;		// Buffer variable to store output from server
		PrintStream nameToServer;			// PrintStrem variable to store output to the server
		ObjectInputStream stateFromServer;
		ObjectOutputStream stateToServer;
		String textmessageFromServer;			// String variable to store output from server after converted to String

		try {
			sock = new Socket(serverName, 1565);	// Connect to client at given server name and port

			// Read the output from the server through the socket and store the data into the buffer
			messageFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Stream for reading in objects a.k.a our states
			stateFromServer = new ObjectInputStream(sock.getInputStream());


			// Set up output stream to go to the server from the socket
			nameToServer = new PrintStream(sock.getOutputStream());
			// Stream for sending out objects a.k.a our states
			stateToServer = new ObjectOutputStream(sock.getOutputStream());


			// Send name to Server where the server will read in the line
			nameToServer.println(name);
			// Make sure that everything sent over to server is sent out
			nameToServer.flush();

			stateToServer.writeObject(jokeSet);
			stateToServer.writeObject(proverbSet);
			stateToServer.flush();


			
			textmessageFromServer = messageFromServer.readLine();		// Read in one line from the output from server
			// If the output from server is not null, print the output on a new line.
			if (textmessageFromServer != null) System.out.println(textmessageFromServer);

			try {
				jokeState = (HashSet<String>) stateFromServer.readObject();
				proverbState = (HashSet<String>) stateFromServer.readObject();
			} catch (ClassNotFoundException c) {System.out.println(c);}

			// Close connection with server
			sock.close();
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}
	}
}