import java.io.*; 	// For Input and Ouput
import java.net.*;	// For Java networking libraries


// Worker Class is a subclass of built in Thread class to use Thread functionality
class Worker extends Thread 
{

	Socket sock;		// Create local variable for socket object
	String message;
	Worker (Socket s, String m) {sock = s; message = m;}		// Worker class constructor makes the local variable sock to Socket argument given

	// Override Thread.run() method for our own implementation 
	public void run()
	{
		PrintStream out = null;		// PrintStream variable so we can write data to Output Stream back to the client

		try {
			// Initialize our PrintStream variable to write to the OutputStream of the socket (back to the client)
			out = new PrintStream(sock.getOutputStream());
			
			out.println(message);
			sock.close();

		// Print error if there is problem initializing variables for our input and output streams
		} catch (IOException ioe) {System.out.println(ioe);}

	}

}


// Class for server
public class JokeServer 
{

	public static int getJokeProverbIndex(int index)
	{
		index++;
		if (index == 4)
			index = 0;
		return index;
	}


	public static String getInfo(Socket sock)
	{
		BufferedReader inClient = null;	// Create a buffer to read text from character-input stream from the client
		DataInputStream intFromClient;
		String input = "";
		int connectionType;



		try {
			inClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			intFromClient = new DataInputStream(sock.getInputStream());
			connectionType = intFromClient.readInt();

			if (connectionType == 0){
				System.out.println("Got connection from Client");
				input = inClient.readLine();
			}
			else if (connectionType == 1){
				System.out.println("Got connection from Admin");
				input = inClient.readLine();
			}


		} catch (IOException i){
			System.out.println(i);
		}

		return input;
	}



	public static Boolean setMode (Socket sock, String message)
	{
		PrintStream out = null;		// PrintStream variable so we can write data to Output Stream back to the client
		Boolean jokeMode = true;
		String toClient = "";

		if (message.contains("joke")){
			jokeMode = true;
			toClient = "Set Mode to joke";
			System.out.println("Set Mode to joke");
		}
		else if (message.contains("proverb")){
			jokeMode = false;
			toClient = "Set Mode to proverb";
			System.out.println("Set Mode to proverb");
		}


		try {
			// Initialize our PrintStream variable to write to the OutputStream of the socket (back to the client)
			out = new PrintStream(sock.getOutputStream());
			
			out.println(toClient);
			out.flush();

		// Print error if there is problem initializing variables for our input and output streams
		} catch (IOException ioe) {System.out.println(ioe);}

		return jokeMode;
	}


	public static void main(String args[]) throws IOException 
	{
		int q_len = 6;			// Number of requests for OpSys to queue
		int port = 4545;		// Port number we will use
		Socket sock;			// Socket variable to connect with client
		Boolean isJoke = true;
		int jokeIndex = -1, proverbIndex = -1;
		String infoFromClient, name;

		String[] joke_array = new String[]{"JA","JB","JC","JD"};
		String[] proverb_array = new String[]{"PA","PB","PC","PD"};

		// Construct server socket on set port and with max queue length for incoming connection requests
		ServerSocket servsock = new ServerSocket(port, q_len);
		System.out.println("Server is running...");



		while (true) {						// Infinite loop:
			sock = servsock.accept();		// Wait for client connection

			infoFromClient = getInfo(sock);

			if (!infoFromClient.equals("joke") && !infoFromClient.equals("proverb")){
				name = infoFromClient;
				System.out.println("Name from client: " + name);
				if (isJoke){
					jokeIndex = getJokeProverbIndex(jokeIndex);
					new Worker(sock,joke_array[jokeIndex]).start();
				}
				else {
					proverbIndex = getJokeProverbIndex(proverbIndex);
					new Worker(sock,proverb_array[proverbIndex]).start();
				}
			} else {
				isJoke = setMode(sock,infoFromClient);
			}

			
				
		}
	}
}