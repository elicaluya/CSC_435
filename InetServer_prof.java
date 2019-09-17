/** File is: InetServer.java, Version 1.8 (small)
A multithreaded server for InetClient. Elliott, after Hughes, Shoffner, Winslow
This will not run unless TCP/IP is loaded on your machine.
----------------------------------------------------------------------*/
import java.io.*; // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries

class Worker extends Thread { // Class definition
	Socket sock; // Class member, socket, local to Worker.
 	Worker (Socket s) {sock = s;} // Constructor, assign arg s to local sock

 	public void run(){
 		// Get I/O streams in/out from the socket:
 		PrintStream out = null;
 		BufferedReader in = null;
 
 		try {
 			in = new BufferedReader
 			(new InputStreamReader(sock.getInputStream()));
 			out = new PrintStream(sock.getOutputStream());

 			// Note that this branch might not execute when expected:
			try {
 				String name;
 				name = in.readLine ();
 				System.out.println("Looking up " + name);
				printRemoteAddress(name, out);
 			} catch (IOException x) {
 				System.out.println("Server read error");
 				x.printStackTrace ();
 			}

 			sock.close(); // close this connection, but not the server;
 		} catch (IOException ioe) {System.out.println(ioe);}
 	}

 
 	static void printRemoteAddress (String name, PrintStream out) {
 		try {
 			out.println("Looking up " + name + "...");
 			InetAddress machine = InetAddress.getByName (name);
 			out.println("Host name : " + machine.getHostName ()); // To client...
 			out.println("Host IP : " + toText (machine.getAddress ()));
 		} catch(UnknownHostException ex) {
 			out.println ("Failed in atempt to look up " + name);
 			}
 	}


 	// Not interesting to us:
 	static String toText (byte ip[]) { /* Make portable for 128 bit format */
 		StringBuffer result = new StringBuffer ();

 		for (int i = 0; i < ip.length; ++ i) {
 			if (i > 0) result.append (".");
 				result.append (0xff & ip[i]);
 		}
 		return result.toString ();
 	}
}


public class InetServer_prof {

	public static void main(String a[]) throws IOException {
 		int q_len = 6; /* Not intersting. Number of requests for OpSys to queue */
 		int port = 1565;
 		Socket sock;

 		ServerSocket servsock = new ServerSocket(port, q_len);

 		System.out.println
 			("Clark Elliott's Inet server 1.8 starting up, listening at port 1565.\n");
 
 		while (true) {
 			sock = servsock.accept(); // wait for the next client connection
 			new Worker(sock).start(); // Spawn worker to handle it
 		}
 	}
}