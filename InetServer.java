import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


// Worker Class is a subclass of built in Thread class to use Thread functionality
class Worker extends Thread 
{

	Socket sock;		// Create local variable for socket object
	Worker (Socket s) {sock = s;}		// Worker class constructor makes the local variable sock to parameter given

	// Override Thread.run() method for our own implementation 
	public void run()
	{
		PrintStream out = null;		// PrintStream variable so we can write data to Output Stream back to the client
		BufferedReader in = null;	// Create a buffer to read text from character-input stream from the client

		try {
			// Initialize buffer variable to read the input stream from the client connection
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Initialize our PrintStream variable to write to the OutputStream of the socket (the client)
			out = new PrintStream(sock.getOutputStream());

			try {
				String name;
				// readLine method converts input from buffer into String and we set that to the variable "name"
				name = in.readLine();
				System.out.println("Looking up " + name);
				// Call printRemoteAddress to write to client info about hostname/address
				printRemoteAddress(name, out);
			} catch (IOException x) {
				// Throw error if there is a problem reading from the buffer from the socket
				System.out.println("Server read error");
				x.printStackTrace();
			}

			// Close the connection with the socket
			sock.close();
		// Print error if there is problem initializing variables for our input and output streams
		} catch (IOException ioe) {System.out.println(ioe);}
	}


	// Method for looking up address from client and sending back info to client
	static void printRemoteAddress (String name, PrintStream out)
	{
		try {
			// Send back to client message that server is looking up hostname
			out.println("Looking up " + name + "...");
			//  get the info from the hostname/address given by client
			InetAddress machine = InetAddress.getByName(name);
			// Send back to client the hostname that the machine variable got
			out.println("Host name : " + machine.getHostName());
			// Send back to client the IP address from the machine variable got
			out.println("Host IP : " + toText(machine.getAddress()));
		// If unable to get the info from the hostname/address, send back to client failed message
		} catch (UnknownHostException ex) {
			out.println("Failed in attempt to look up " + name);
		}
	}


	// Method for converting byte IP address to string 
	static String toText(byte ip[])
	{
		StringBuffer result = new StringBuffer();

		for (int i = 0; i <  ip.length; ++i){
			if (i > 0) result.append(".");
					result.append(0xff & ip[i]);
		}
		return result.toString();
	}
}


// Class for server
public class InetServer 
{
	public static void main(String args[]) throws IOException 
	{
		int q_len = 6;			// Number of requests for OpSys to queue
		int port = 1565;		// Port number we will use
		Socket sock;			// SOcket variable to connect with client

		// Construct server socket on set port and with number of queue requests
		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Clark Elliott's Inet server 1.8 starting up, listening at port 1565.\n");

		while (true) {
			sock = servsock.accept();		// Wait for client connection
			new Worker(sock).start();		// Create new Worker thread for the client
		}
	}
}