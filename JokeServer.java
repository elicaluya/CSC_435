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
		
		String jokeState = "";
		String proverbState = "";

		try {
			// Initialize buffer variable to read the input stream from the client connection
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			// Initialize our PrintStream variable to write to the OutputStream of the socket (back to the client)
			out = new PrintStream(sock.getOutputStream());
			

			try {
				String name;
				// readLine method converts input from buffer into String and we set that to the variable "name"
				name = in.readLine();
				System.out.println("Got name from client: " + name);		// Print looking up hostname/address in server

				// Read in the state of the jokes and proverbs for client
				jokeState = in.readLine();
				proverbState = in.readLine();
				

				// Call sendMessage to write to client the info about hostname/address
				sendMessage(name, out, jokeState, proverbState);
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


	// Get joke according to state of client
	static String getJoke(String set, int size, String name)
	{
		String[] jokes = {"JA","JB","JC","JD"};
		return jokes[size/2] + " " + name + ":";
	}

	// Get proverb according to state of client
	static String getProverb(String set, int size, String name)
	{
		String[] proverbs = {"PA","PB","PC","PD"};
		return proverbs[size/2] + " " + name + ":";
	}	

	// Method for looking up address from client and sending back info back to client
	static void sendMessage (String name, PrintStream toClient,String jState, String pState)
	{
		String message = "";
		
			
		if (isJoke){
			if (jState.length() == 8){
				jState = "";
				System.out.println("Joke Cycle completed! Resetting jokes for " + name);
			}
			
			message = getJoke(jState, jState.length(),name);
			jState = jState + message.substring(0,2);
		}
		else{
			if (pState.length() == 8){
				pState = "";
				System.out.println("Proverb Cycle completed! Resetting proverbs for " + name);
			}

			message = getProverb(pState, pState.length(),name);
			pState = pState + message.substring(0,2);
		}

		// Send Joke or proverb back to client
		toClient.println(message);
		toClient.println(jState);
		toClient.println(pState);

		toClient.flush();
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

	static Boolean isJoke = true;

	public static void main(String args[]) throws IOException 
	{
		int q_len = 10;			// Number of requests for OpSys to queue
		int port = 1565;		// Port number we will use
		Socket sock;			// Socket variable to connect with client

		
		JokeAdmin admin = new JokeAdmin();
		Thread thread = new Thread(admin);
		thread.start();

		// Construct server socket on set port and with max queue length for incoming connection requests
		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("Elijah Caluya's Inet server 1.8 starting up, listening at port 1565.\n");

		

		while (true) {						// Infinite loop:
			sock = servsock.accept();		// Wait for client connection
			System.out.println("Got connection from client");
			// Create new Worker thread once client connection is accepted and do code in run() method with start() call
			new Worker(sock, isJoke).start();		
		}
	}
}



class AdminWorker extends Thread
{
	Socket sock;

	AdminWorker(Socket s) {sock = s;}

	public void run()
	{
		DataInputStream fromClient;
		PrintStream toClient;
		Boolean mode;

		try {
			fromClient = new DataInputStream(sock.getInputStream());

			toClient = new PrintStream(sock.getOutputStream());

			mode = fromClient.readBoolean();

			if (mode){
				JokeServer.isJoke = true;
				System.out.println("Server set to Joke Mode");
				toClient.println("Server set to Joke Mode");
				toClient.flush();
			} else {
				JokeServer.isJoke = false;
				System.out.println("Server set to Proverb Mode");
				toClient.println("Server set to Proverb Mode");
				toClient.flush();
			}
		} catch (IOException i){
			System.out.println(i);
		}
	}
}


class JokeAdmin implements Runnable 
{
	public void run()
	{
		int q_len = 10;			// Number of requests for OpSys to queue
		int port = 1566;		// Port number we will use
		Socket sock;			// Socket variable to connect with client

		try {
			// Construct server socket on set port and with max queue length for incoming connection requests
			ServerSocket servsock = new ServerSocket(port, q_len);

			while (true) {
				sock = servsock.accept();
				new AdminWorker(sock).start();
			}
		} catch (IOException i) {System.out.println(i);}
		
	}
}