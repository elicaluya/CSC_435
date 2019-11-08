/* 2012-05-20 Version 2.0

Thanks John Reagan for this well-running code which repairs the original
obsolete code for Elliott's HostServer program. I've made a few additional
changes to John's code, so blame Elliott if something is not running.

-----------------------------------------------------------------------

Play with this code. Add your own comments to it before you turn it in.

-----------------------------------------------------------------------
NOTE: This is NOT a suggested implementation for your agent platform,
but rather a running example of something that might serve some of
your needs, or provide a way to start thinking about what YOU would like to do.
You may freely use this code as long as you improve it and write your own comments.

-----------------------------------------------------------------------

TO EXECUTE: 

1. Start the HostServer in some shell. >> java HostServer

1. start a web browser and point it to http://localhost:1565. Enter some text and press
the submit button to simulate a state-maintained conversation.

2. start a second web browser, also pointed to http://localhost:1565 and do the same. Note
that the two agents do not interfere with one another.

3. To suggest to an agent that it migrate, enter the string "migrate"
in the text box and submit. The agent will migrate to a new port, but keep its old state.

During migration, stop at each step and view the source of the web page to see how the
server informs the client where it will be going in this stateless environment.

-----------------------------------------------------------------------------------

COMMENTS:

This is a simple framework for hosting agents that can migrate from
one server and port, to another server and port. For the example, the
server is always localhost, but the code would work the same on
different, and multiple, hosts.

State is implemented simply as an integer that is incremented. This represents the state
of some arbitrary conversation.

The example uses a standard, default, HostListener port of 1565.

-----------------------------------------------------------------------------------

DESIGN OVERVIEW

Here is the high-level design, more or less:

HOST SERVER
  Runs on some machine
  Port counter is just a global integer incrememented after each assignment
  Loop:
    Accept connection with a request for hosting
    Spawn an Agent Looper/Listener with the new, unique, port

AGENT LOOPER/LISTENER
  Make an initial state, or accept an existing state if this is a migration
  Get an available port from this host server
  Set the port number back to the client which now knows IP address and port of its
         new home.
  Loop:
    Accept connections from web client(s)
    Spawn an agent worker, and pass it the state and the parent socket blocked in this loop
  
AGENT WORKER
  If normal interaction, just update the state, and pretend to play the animal game
  (Migration should be decided autonomously by the agent, but we instigate it here with client)
  If Migration:
    Select a new host
    Send server a request for hosting, along with its state
    Get back a new port where it is now already living in its next incarnation
    Send HTML FORM to web client pointing to the new host/port.
    Wake up and kill the Parent AgentLooper/Listener by closing the socket
    Die

WEB CLIENT
  Just a standard web browser pointing to http://localhost:1565 to start.

  -------------------------------------------------------------------------------*/


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * HostServer Notes: This went pretty smoothly for me, although I did have to edit the HTML functions
 * to get an accurate content length so things would be compatible with browsers other than IE. I also modified
 * things to eliminate inaccurate state numbers based on fav.ico requests. If the string person wasnt found,
 * the requests was ignored
 */

/******************************************************************************************************************************************************************/
//	*NOTE: The following comments in the program are my own original comments by Elijah Caluya on the code from John Reagan
/******************************************************************************************************************************************************************/

// This class is responsible for the migrating of ports when the user enters "migrate" in the text box.
// The worker thread takes in requests and responds accordingly to the text input box and while utilizing an agentHolder 
// object to maintain the state of the connection as well as the AgentListener class to help in responding with the
// correct HTML output to the browser and the console. AgentWorker is started from an AgentListener thread.
class AgentWorker extends Thread {
	
	Socket sock; // Create Socket variable so we can make connection to client
	agentHolder parentAgentHolder; // Create instance variable of class that will hold the current connection and state of the parent agent
	int localPort; // Variable for the port to use
	
	// Initialize the global variables with a constructor
	AgentWorker (Socket s, int prt, agentHolder ah) {
		sock = s;
		localPort = prt;
		parentAgentHolder = ah;
	}
	// Run what is in this method when the thread starts
	public void run() {
		
		PrintStream out = null;	// Initialize PrintStream variable so we can send data through the socket
		BufferedReader in = null;	// Initialize BufferedReader variable so we can read in from the socket
		String NewHost = "localhost";	// Set the host to always be "localhost"
		int NewHostMainPort = 1565;		// The port that the AgentWorker will run on
		String buf = "";	// Empty string to act as buffer for reading in lines/input
		int newPort;	// Variable for the new port when we want to migrate
		Socket clientSock;	// Socket variable for new connection when we want to migrate 
		BufferedReader fromHostServer;	// BufferedReader for to read in from new client connection when we migrate
		PrintStream toHostServer;	// New PrintStream variable to write out to client when we migrate
		
		try {
			// Set the PrintStram and BuffereReader variables so we can receive and send data to and from the client
			out = new PrintStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			// Read in a line from the client connection and store in inLine
			String inLine = in.readLine();
			// Use StringBuilder variable because we need to know the length of the string for non-ie browsers.
			StringBuilder htmlString = new StringBuilder();
			
			// Print out the request from the client on the console
			System.out.println();
			System.out.println("Request line: " + inLine);
			
			// If the user enters "migrate" anywhere in the input box/ is in the request line
			if(inLine.indexOf("migrate") > -1) {
				
				// Create a new socket when the user wants to migrate on the main port 1565
				clientSock = new Socket(NewHost, NewHostMainPort);
				// Set up the Buffered Reader to read in from this new connection
				fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
				// Set up PrintStream to send data to the new connection
				toHostServer = new PrintStream(clientSock.getOutputStream());
				// Send a new request to the server to host the client and receive the next open port
				// Also send the current state of the thread
				toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
				toHostServer.flush();	// Make sure everything is sent
				
				// Wait for a response from the server
				for(;;) {
					buf = fromHostServer.readLine();	// Read in the Line
					if(buf.indexOf("[Port=") > -1) {	// Make sure that it is a valid format for a port and if so continue
						break;	// break the loop
					}
				}
				
				// Get the new port by getting the substring from the buffer
				String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
				// Make the string port into an Integer
				newPort = Integer.parseInt(tempbuf);
				// Output the new port on the console
				System.out.println("newPort is: " + newPort);
				
				// Add to the StringBuilder object the correct HTML header on the new port
				htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine));
				// Add to the StringBuilder object the HTML strings to tell the user we are migrating to a different port
				// and how the user can see how the client is informed of the new location
				htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");
				htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
				// Add final string for HTML output
				htmlString.append(AgentListener.sendHTMLsubmit());

				// Output to console that the waiting parent server at the port will be killed
				System.out.println("Killing parent listening loop.");
				// Put the parent connection's ServerSocket from the old port into a variable so we can close it
				ServerSocket ss = parentAgentHolder.sock;
				// close the old port
				ss.close();
				
			// If it is a normal request with "person" in the Request Line
			} else if(inLine.indexOf("person") > -1) {
				// Increment the current state of this thread to keep track
				parentAgentHolder.agentState++;
				// Add to the StringBuilder object the correct HTML header on the current port
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
				// Add to the StringBuilder object HTML string that states the current state of the thread
				htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
				// Add final HTML string
				htmlString.append(AgentListener.sendHTMLsubmit());

			// If "person" or "migrate" were not in the request line then it is a fav.ico request
			} else {
				// Add to the StringBuilder object the correct HTML header on the current port
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
				// Add HTML string that the request was invalid
				htmlString.append("You have not entered a valid request!\n");
				// Add final HTML string
				htmlString.append(AgentListener.sendHTMLsubmit());		
				
		
			}
			// Send The formatted HTML header with correct response header so the HTML page can be displayed properly on the browser
			AgentListener.sendHTMLtoStream(htmlString.toString(), out);
			
			//close the socket
			sock.close();
			
		// Display any IOExceptions caught
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
}

// Class for holding the ServerSocket connection and the agentState for a connection
class agentHolder { 
	ServerSocket sock;	// ServerSocket object to store socket connection
	int agentState;	// Integer to store the agent state for the current connection
	
	// Constructor is used to initialize agentHolder object with ServerSocket object
	agentHolder(ServerSocket s) { sock = s;}
}

// AgentListener class uses individual ports to listen for requests made on them and to respond accordingly.
// The class is initalized with a socket and port to listen on so that it can keep track of the requests coming thru it
class AgentListener extends Thread {
	Socket sock;	// Socket for connection with client
	int localPort;	// The port this object will listen on
	
	// Use constructor to listen and connect to a specific port and socket
	AgentListener(Socket As, int prt) {
		sock = As;
		localPort = prt;
	}
	// default agentState is 0 since new "game"
	int agentState = 0;
	
	// When the thread is started, run the following code
	public void run() {
		BufferedReader in = null;	// BufferedReader variable for reading in request from client
		PrintStream out = null;		// PrintStream variable for sending out data to the client
		String NewHost = "localhost";	// String for localhost
		System.out.println("In AgentListener Thread");	// Output to console that the AgentListener thread is running
		try {
			String buf;	// String to act as buffer for reading line input
			out = new PrintStream(sock.getOutputStream());	// Set the PrintStream to variable to send data to the client
			in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));	// Set BufferedReader to read in data from the client
			
			// Read in the first line and store in the string buffer
			buf = in.readLine();
			
			// See if the state is in the string buffer
			if(buf != null && buf.indexOf("[State=") > -1) {
				// if so, get the substring of the buffer where it is the state
				String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
				// parse the Integer and store the state in the agentState variable
				agentState = Integer.parseInt(tempbuf);
				// Output to the console the agentState read in
				System.out.println("agentState is: " + agentState);
					
			}
			
			// Output to console the string buffer
			System.out.println(buf);
			// Use StringBuilder so we can create a large string and get length for HTML response
			StringBuilder htmlResponse = new StringBuilder();
			// Add to StringBuilder the proper HTML header on the current port
			htmlResponse.append(sendHTMLheader(localPort, NewHost, buf));
			// Add HTML string for saying the Agent Listener is starting
			htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
			// Add the current port to the string builder
			htmlResponse.append("[Port="+localPort+"]<br/>\n");
			// Add the final ending HTML strings
			htmlResponse.append(sendHTMLsubmit());
			// Send the HTML string with the proper response header so that it will be displayed properly with the added information
			sendHTMLtoStream(htmlResponse.toString(), out);
			
			// Open a connection on the current port
			ServerSocket servsock = new ServerSocket(localPort,2);
			// Create a new agentholder object and store the current socket and agentState
			agentHolder agenthold = new agentHolder(servsock);
			agenthold.agentState = agentState;
			
			// wait for connections.
			while(true) {
				sock = servsock.accept();	// accept connection
				// Output to console a connection was made on the current port
				System.out.println("Got a connection to agent at port " + localPort);
				// create new AgentWorker and start the thread
				new AgentWorker(sock, localPort, agenthold).start();
			}
		
		// Display on console any IOExceptions caught
		// Usually displays when port switches or error occurs
		} catch(IOException ioe) {
			System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort);
			System.out.println(ioe);
		}
	}
	

	// Method for using the form  to Load HTML page on a specific port and return the HTML string to be displayed on browser later
	static String sendHTMLheader(int localPort, String NewHost, String inLine) {
		
		StringBuilder htmlString = new StringBuilder();	// Create new StringBuilder variable for adding HTML strings

		// Add the beginning HTML string for the head and body
		htmlString.append("<html><head> </head><body>\n");
		// Add HTML string for text that displays up top the current port and the host (localhost)
		htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");
		// Add HTML string for displaying the request from the user
		htmlString.append("<h3>You sent: "+ inLine + "</h3>");
		// Add HTML string for form method whose action is to load the page on the localhost on the port specified
		htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
		// Add HTML string to simply tell the user to enter text or "migrate"
		htmlString.append("Enter text or <i>migrate</i>:");
		// Add HTML string for input box to get user input
		htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n");
		
		// Return the string built with all of the HTML strings added to it
		return htmlString.toString();
	}

	// Use this method to get the last string for creating the HTML page and completing the form
	static String sendHTMLsubmit() {
		// Set the submit button to submit the form method
		return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
	}

	// Use this method send the proper response header for an html page.
	static void sendHTMLtoStream(String html, PrintStream out) {
		// Send out in PrintStream the proper response header with content length to work with non-ie browsers
		out.println("HTTP/1.1 200 OK");
		out.println("Content-Length: " + html.length());
		out.println("Content-Type: text/html");
		out.println("");		
		out.println(html);
	}
	
}


// The main HostServer class that listens on port 1565 for requests.
// For each request to this server, the NextPort will be incremented  and a new AgentListener will be started on it.
public class HostServer {
	// default next available port but we will start on 3001 because we increment it when the server runs
	public static int NextPort = 3000;
	
	public static void main(String[] a) throws IOException {
		int q_len = 6;	// Opsys queue
		int port = 1565;	// port the HostServer runs on
		Socket sock;	// Socket for connections between client and server
		
		ServerSocket servsock = new ServerSocket(port, q_len);	// Create new ServerSocket with socket
		// Console output to say the port the server is running on
		System.out.println("John Reagan's DIA Master receiver started at port 1565.");
		// Console output for instructions to connect to the server
		System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:1565\"\n");
		// Server continuously listens on port 1565 for connections and starts a new AgentListener on it
		while(true) {
			// Increment the NextPort for the AgentListener to listen on 
			NextPort = NextPort + 1;
			// Accept connections to server
			sock = servsock.accept();
			// Console output stating the AgentListener will be listening on the next available port
			System.out.println("Starting AgentListener at port " + NextPort);
			// Create new AgentListener on this port so it can listen to requests
			new AgentListener(sock, NextPort).start();
		}
		
	}
}