import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries
import java.util.*;	// For Scanner


// Class for client to connect to server
public class JokeClient 
{

	static void getMessage(Socket sock)
	{
		BufferedReader fromServer;		// Buffer variable to store output from server
		String textFromServer;

		try {
			// Read the output from the server through the socket and store the data into the buffer
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			textFromServer = fromServer.readLine();		// Read in one line from the output from server
			// If the output from server is not null, print the output on a new line.
			System.out.println(textFromServer);
			sock.close();
		} catch (IOException i) {
			System.out.println(i);
		}


	}


	// Method for sending and receiving data to server from client
	static void getConnection(String name)
	{
		Socket sock;					// Socket variable for connection to server
		PrintStream stringToServer;			// PrintStrem variable to store output to the server
		DataOutputStream intToServer;
		

		try {
			sock = new Socket("localhost", 4545);	// Connect to client at given server name and port

			
			// Set up output stream to go to the server from the socket
			stringToServer = new PrintStream(sock.getOutputStream());

			intToServer = new DataOutputStream(sock.getOutputStream());


			intToServer.writeInt(0);
			intToServer.flush();


			// Send name to Server where the server will read in the line
			stringToServer.println(name);
			// Make sure that everything sent over to server is sent out
			stringToServer.flush();
			
			getMessage(sock);
			
		// If there are any errors, print out error message
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();		// Print out IOException data to console log
		}
	}



	public static void main (String args[])
	{
		String name;


		// Initialize the buffer to read in user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
		System.out.print("Enter name: ");
		System.out.flush();				// Make sure everything written to Standard out is sent

		
		try {
			name = in.readLine();
			getConnection(name);
		} catch (IOException x) {x.printStackTrace();}		// Print data in output console

		

	}

}