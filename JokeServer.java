import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


// Worker Class is a subclass of built in Thread class to use Thread functionality
class Worker extends Thread 
{

	Socket sock;		// Create local variable for socket object
	Boolean isJoke = true;
	Worker (Socket s) {sock = s;}		// Worker class constructor makes the local variable sock to Socket argument given

	// Override Thread.run() method for our own implementation 
	public void run()
	{
		PrintStream out = null;		// PrintStream variable so we can write data to Output Stream back to the client
		BufferedReader in = null;	// Create a buffer to read text from character-input stream from the client

		try {
			// Initialize buffer variable to read the input stream from the client connection
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Initialize our PrintStream variable to write to the OutputStream of the socket (back to the client)
			out = new PrintStream(sock.getOutputStream());

			try {
				String name;
				// readLine method converts input from buffer into String and we set that to the variable "name"
				name = in.readLine();
				System.out.println("Got from client: " + name);	
				
				sendMessage(out,this.isJoke);
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

	public void sendMessage(PrintStream out, Boolean joke){
		if (joke)
			out.println("Why did the chicken cross the road?");
		else
			out.println("One who sits on toilet, gets high on pot");
	}

}


// Class for server
public class JokeServer 
{
	public static void main(String args[]) throws IOException 
	{
		int q_len = 6;			// Number of requests for OpSys to queue
		int port = 1565;		// Port number we will use
		Socket sock;			// Socket variable to connect with client

		// Construct server socket on set port and with max queue length for incoming connection requests
		ServerSocket servsock = new ServerSocket(port, q_len);
		System.out.println("Server is running...");

		while (true) {						// Infinite loop:
			sock = servsock.accept();		// Wait for client connection
			// Create new Worker thread once client connection is accepted and do code in run() method with start() call
			new Worker(sock).start();		
		}
	}
}