import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries
import java.util.*;


// Class for client to connect to server
public class JokeClientAdmin 
{

	public static void main (String args[])
	{
		
		String input;
		String message;
		
	
		System.out.println("Hello Admin, enter 'joke' for joke mode or 'proverb' for proverb mode: ");
		System.out.flush();				// Make sure everything written to Standard out is sent
			
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {
			input = in.readLine();
			setMode(input);
		} catch (IOException i) {
			System.out.println(i);
		}
		
	}



	// Method for sending and receiving data to server from client
	static void setMode(String message)
	{
		Socket sock;					// Socket variable for connection to server
		BufferedReader fromServer;		// Buffer variable to store output from server
		PrintStream toServer;			// PrintStrem variable to store output to the server
		DataOutputStream intToServer;
		String textFromServer;			// String variable to store output from server after converted to String

		try {
			sock = new Socket("localhost", 4545);	// Connect to client at given server name and port

			// Read the output from the server through the socket and store the data into the buffer
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Set up output stream to go to the server from the socket
			toServer = new PrintStream(sock.getOutputStream());

			intToServer = new DataOutputStream(sock.getOutputStream());

			intToServer.writeInt(1);


			

			// Send name to Server where the server will read in the line
			toServer.println(message);
			// Make sure that everything sent over to server is sent out
			toServer.flush();


			intToServer.flush();

			
			textFromServer = fromServer.readLine();		// Read in one line from the output from server
			// If the output from server is not null, print the output on a new line.
			System.out.println(textFromServer);
			
			
			// Close connection with server
			sock.close();
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}
	}
}