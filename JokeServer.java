import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries
import java.util.*;


// Worker Class is a subclass of built in Thread class to use Thread functionality
class Worker extends Thread 
{

	Socket sock;		// Create local variable for socket object
	static Boolean isJoke;
	Worker (Socket s, Boolean j) {sock = s; isJoke = j;}		// Worker class constructor makes the local variable sock to Socket argument given

	// Override Thread.run() method for our own implementation 
	public void run()
	{
		PrintStream out = null;		// PrintStream variable so we can write data to Output Stream back to the client
		BufferedReader in = null;	// Create a buffer to read text from character-input stream from the client
		ObjectInputStream stateFromClient;
		ObjectOutputStream stateToClient;

		HashSet<String> jokeState = new HashSet<>();
		HashSet<String> proverbState = new HashSet<>();

		try {
			// Initialize buffer variable to read the input stream from the client connection
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Stream for getting the state of the client
			stateFromClient = new ObjectInputStream(sock.getInputStream());

			// Initialize our PrintStream variable to write to the OutputStream of the socket (back to the client)
			out = new PrintStream(sock.getOutputStream());
			// Stream for sending back to client the state after update
			stateToClient = new ObjectOutputStream(sock.getOutputStream());

			try {
				String name;
				// readLine method converts input from buffer into String and we set that to the variable "name"
				name = in.readLine();
				System.out.println("Got name from client: " + name);		// Print looking up hostname/address in server

				try {
					jokeState = (HashSet<String>)stateFromClient.readObject();
					proverbState = (HashSet<String>)stateFromClient.readObject();
				} catch (ClassNotFoundException c){System.out.println(c);}

				// Call sendMessage to write to client the info about hostname/address
				sendMessage(name, out, stateToClient, jokeState, proverbState);
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


	static String getJoke(HashSet<String> set, int size)
	{
		String[] jokes = {"JA","JB","JC","JD"};
		if (size == 4){
			size = 0;
			set.clear();
		}
		return jokes[size];

	}

	static String getProverb(HashSet<String> set, int size)
	{
		String[] proverbs = {"PA","PB","PC","PD"};
		if (size == 4){
			size = 0;
			set.clear();
		}
		return proverbs[size];
	}	

	// Method for looking up address from client and sending back info back to client
	static void sendMessage (String name, PrintStream messageOut, ObjectOutputStream stateToClient, HashSet<String> jState, HashSet<String> pState)
	{
		try {
			String message = "";
			if (isJoke){
				message = getJoke(jState, jState.size());
				jState.add(message);
			}
			else{
				message = getProverb(pState, pState.size());
				pState.add(message);
			}

			// Send Joke or proverb back to client
			messageOut.println(message);
			messageOut.flush();

			stateToClient.writeObject(jState);
			stateToClient.writeObject(pState);
			stateToClient.flush();
			
		// If unable to get the info from the hostname/address, send back to client failed message
		} catch (IOException i) {
			System.out.println(i);
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
public class JokeServer 
{
	public static void main(String args[]) throws IOException 
	{
		int q_len = 6;			// Number of requests for OpSys to queue
		int port = 1565;		// Port number we will use
		Socket sock;			// Socket variable to connect with client
		Boolean isJoke = true;

		// Construct server socket on set port and with max queue length for incoming connection requests
		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Clark Elliott's Inet server 1.8 starting up, listening at port 1565.\n");

		while (true) {						// Infinite loop:
			sock = servsock.accept();		// Wait for client connection
			// Create new Worker thread once client connection is accepted and do code in run() method with start() call
			new Worker(sock, isJoke).start();		
		}
	}
}