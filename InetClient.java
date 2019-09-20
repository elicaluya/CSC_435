import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


// Class for client to connect to server
public class InetClient 
{

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
	
		try {
			String name;
			do {
				System.out.print("Enter a hostname or an IP address, (quit) to end: ");
				System.out.flush();				// Make sure everything written to Standard out is sent
				name = in.readLine();			// Set String variable to string line read in from buffer
				if (name.indexOf("quit") < 0)	// Check if string entered is not "quit"
					getRemoteAddress(name, serverName);	// If not quit, call getRemoteAddress() method to read output from server
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
	static void getRemoteAddress(String name, String serverName)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader fromServer;		// Buffer variable to store output from server
		PrintStream toServer;			// PrintStrem variable to store output to the server
		String textFromServer;			// String variable to store output from server after converted to String

		try {
			sock = new Socket(serverName, 1565);	// Connect to client at given server name and port

			// Read the output from the server through the socket and store the data into the buffer
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up output stream to go to the server from the socket
			toServer = new PrintStream(sock.getOutputStream());

			// Send name to Server where the server will read in the line
			toServer.println(name);
			// Make sure that everything sent over to server is sent out
			toServer.flush();


			// Read in the print line from the server three times, since the server sends back 3 lines of output 
			// Each time, print the output on a new line
			for (int i = 1; i <= 3; i++){
				textFromServer = fromServer.readLine();		// Read in one line from the output from server
				// If the output from server is not null, print the output on a new line.
				if (textFromServer != null) System.out.println(textFromServer);
			}
			// Close connection with server
			sock.close();
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}
	}
}