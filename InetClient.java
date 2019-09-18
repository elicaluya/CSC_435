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
		// Set the buffer to read in user input
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
		try {
			String name;
			do {
				System.out.print("Enter a hostname or an IP address, (quit) to end: ");
				System.out.flush();				// Make sure everything written to Standard out is sent
				name = in.readLine();			// Read in string from buffer
				if (name.indexOf("quit") < 0)	// Check if string entered is not "quit"
					getRemoteAddress(name, serverName);	// If not quit, call getRemoteAddress method
			} while (name.indexOf("quit") < 0);			// When the user enters quit, print out message and program ends
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
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			sock = new Socket(serverName, 1565);

			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());

			toServer.println(name); toServer.flush();


			for (int i = 1; i <= 3; i++){
				textFromServer = fromServer.readLine();
				if (textFromServer != null) System.out.println(textFromServer);
			}
			sock.close();
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
}