/* MyListener

This is VERY quick and dirty code that leaves workers lying around. But you get the idea.

 */


import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries

class WebServerWorker extends Thread {    // Class definition
    Socket sock;                   // Class member, socket, local to ListnWorker.
    WebServerWorker (Socket s) {sock = s;} // Constructor, assign arg s
                                      //to local sock
    public void run(){
        // Get I/O streams from the socket:
        PrintStream out = null;
        BufferedReader in = null;
        try {
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            String request;

            request = in.readLine ();
            
            sendResponse(out,sock,request);
            
            sock.close(); // close this connection, but not the server;
            
         } catch (IOException x) {
            System.out.println("Connection reset. Listening again...");
        }
    }


    public void sendResponse(PrintStream out, Socket sock, String request) throws IOException{
        String fileName = "";
        String mimeType = "";

        // Indexes to extract the file name substring from the request
        int frontIndex = request.indexOf("/");
        int endIndex = request.indexOf(" ", frontIndex);
        
        // If it is a favicon.ico request, ignore it because we only need the file name
        if (!request.substring(frontIndex+1, endIndex).equals("favicon.ico")){
            fileName = request.substring(frontIndex+1, endIndex);
            System.out.println("File name: " + fileName);
        

            // Set the correct MIME type depending on what type of file the user is requesting
            if (fileName.substring(fileName.length()-4).equals("html"))
                mimeType = "text/html";     // If HTML file set MIME type to text/html
            else if (fileName.substring(fileName.length()-3).equals("txt"))
                mimeType = "text/plain";    // If text file set MIME type to text/plain


            // Create File variable to open the file by file name
            File file = new File(fileName);

            // Buffered reader to read through the opened file
            BufferedReader readFile = null;
            String line = null;

            // String builder variable so we can store all of the lines in the opened file
            StringBuilder fileContents = new StringBuilder();


            // Check if file exists to avoid FileNotFoundException
            if (file.exists()){
                // Read through contents of file with a Buffered Reader
                readFile = new BufferedReader(new FileReader(fileName));
            
                // Append all of the contents in the file to a String Builder Object
                while ((line = readFile.readLine()) != null)
                    fileContents.append(line + "\n");
                

                // Write back to client the proper header response with content length, content type, and contents of the file            
                out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + (fileContents.toString().getBytes().length+1) + "\r\n" 
                            + "Content-Type: " + mimeType + "\r\n" + "\r\n\r\n" + fileContents.toString());
                out.flush();

                System.out.println("Contents of File: \n" + fileContents.toString());
                System.out.flush();
            }
            
        }

        
    }
}

public class MyWebServer {

  public static boolean controlSwitch = true;

  public static void main(String a[]) throws IOException {
    int q_len = 6; /* Number of requests for OpSys to queue */
    int port = 2540;
    Socket sock;

    ServerSocket servsock = new ServerSocket(port, q_len);

    System.out.println("Clark Elliott's Port listener running at 2540.\n");
    while (controlSwitch) {
      // wait for the next client connection:
      sock = servsock.accept();
      new WebServerWorker (sock).start(); // Uncomment to see shutdown bug:
      // try{Thread.sleep(10000);} catch(InterruptedException ex) {}
    }
  }
}