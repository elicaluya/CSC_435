import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


// Class for client to connect to server
public class JokeClientAdmin 
{

	static Boolean isJoke = true;

	public static void main (String args[])
	{
		String serverName;
		// If User doesn't provide a server name in arguments, then set servername to localhost
		if (args.length < 1) serverName = "localhost";
		else serverName = args[0];

		System.out.println("Elijah Caluya's Joke Client Admin, 1.8.\n");
		System.out.println("using server: " + serverName + ", Port: 1566");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Boolean jokeMode = true;
	
		try {
			String input;
			do {
				if (jokeMode)
					System.out.println("Current Mode is Joke Mode");
				else
					System.out.println("Current Mode is Proverb Mode");
				System.out.println("Press Enter to change mode or 'quit' to stop admin client");
				input = in.readLine();
				if (input.indexOf("quit") < 0)	// Check if string entered is not "quit"
					jokeMode = changeMode(serverName,jokeMode);	// If not quit, call changeMode() method to read output from server
			// While the input is not quit, keep reading in line and sending info to server
			} while (input.indexOf("quit") < 0);			// Else, print out message and then end program.
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
	static Boolean changeMode(String serverName, Boolean isJoke)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader fromServer;		// Buffer variable to store output from server
		DataOutputStream toServer;			// PrintStrem variable to store output to the server
		String textFromServer;			// String variable to store output from server after converted to String

		try {
			sock = new Socket(serverName, 1566);	// Connect to client at given server name and port

			// Read the output from the server through the socket and store the data into the buffer
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up output stream to go to the server from the socket
			toServer = new DataOutputStream(sock.getOutputStream());

			// Send name to Server where the server will read in the line
			toServer.writeBoolean(!isJoke);
			// Make sure that everything sent over to server is sent out
			toServer.flush();

			if (isJoke)
				isJoke = false;
			else
				isJoke = true;

			
			textFromServer = fromServer.readLine();		// Read in one line from the output from server
			// If the output from server is not null, print the output on a new line.
			if (textFromServer != null) System.out.println(textFromServer);
			
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