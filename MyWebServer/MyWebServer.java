/*--------------------------------------------------------

1. Elijah Caluya / Date: 10/13/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac MyWebServer.java


4. Precise examples / instructions to run this program:

e.g.:




5. List of files needed for running the program.

e.g.:




5. Notes:

e.g.:


----------------------------------------------------------*/

import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries
import java.util.*;

class WebServerWorker extends Thread {    // Class definition
    Socket sock;                   // Class member, socket, local to ListnWorker.
    WebServerWorker (Socket s) {sock = s;} // Constructor, assign arg s
                                      //to local sock
    public void run(){
        // Get I/O streams from the socket:
        PrintStream out = null;
        BufferedReader in = null;
        String getRequest = "";
        try {
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            
            String request;

            request = in.readLine();
            

            if (request != null){
                if (!request.contains("favicon.ico")){
                    System.out.println(request);

                    getRequest = parseRequest(request);
                
                
                    while (in.ready()){
                        request = in.readLine();
                        System.out.println(request);
                    }


                    if (!(getRequest.length() == 0)){
                        if (getRequest.substring(getRequest.length()-1).equals("/"))
                            dirResponse(out,sock,getRequest);
                        else if (getRequest.contains(".html") || getRequest.contains(".txt"))
                            fileResponse(out,sock,getRequest);
                        else 
                            notFoundResponse(out,getRequest);
                        
                    }
                    else
                        dirResponse(out,sock,getRequest);
                }
            }
            

            sock.close(); // close this connection, but not the server;
            
         } catch (IOException x) {
            System.out.println("Connection reset. Listening again...");
        }
    }

    /******************************************************************************************************************************************************************/
    //                                         Method used to get the filename/directory name from the HTTP GET request
    /******************************************************************************************************************************************************************/
    public String parseRequest(String request) {
        String fileName = "";

        // Indexes to extract the file name substring from the request
        int frontIndex = request.indexOf("/");
        int endIndex = request.indexOf(" ", frontIndex);
        fileName = request.substring(frontIndex+1,endIndex);

        return fileName;
    }


    /******************************************************************************************************************************************************************/
    //                                         Send back to client the opened file that they requested
    /******************************************************************************************************************************************************************/
    public void fileResponse(PrintStream out, Socket sock, String request) throws IOException{
        String fileName = "";   // String variable to store the file to open
        String mimeType = "";   // String variable to store the MIME type depending on what type of file is being opened

        fileName = request;

        // Set the correct MIME type depending on what type of file the user is requesting
        if (fileName.substring(fileName.length()-5).equals(".html"))
            mimeType = "text/html";     // If HTML file set MIME type to text/html
        else if (fileName.substring(fileName.length()-4).equals(".txt"))
            mimeType = "text/plain";    // If text file set MIME type to text/plain
            
        System.out.println("Opening file: " + fileName + "\n");



        // Create File variable to open the file by file name
        File file = new File(fileName);

        // Buffered reader to read through the opened file
        BufferedReader readFile = null;
        String line = "";

        // String builder variable so we can store all of the lines in the opened file
        // We have to send a long string back to the client so StringBuilder was the easiest way for me to get all contents of the file into one string
        // Knowledge and methods for StringBuilder referenced from: https://www.javatpoint.com/StringBuilder-class
        StringBuilder fileContents = new StringBuilder();


        // All knowledge and methods for Files referenced from Prof. Elliott's examples and https://www.geeksforgeeks.org/file-class-in-java/
        // Check if file exists to avoid FileNotFoundException
        if (file.exists()){
            // Read through contents of file with a Buffered Reader
            readFile = new BufferedReader(new FileReader(fileName));
          
            // Append all of the contents in the file to a String Builder Object
            while ((line = readFile.readLine()) != null)
                fileContents.append(line+"\n");
                

            // Write back to client the proper header response with content length, content type, and contents of the file            
            out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + (fileContents.toString().getBytes().length+1) + "\r\n" 
                            + "Content-Type: " + mimeType + "\r\n" + "\r\n\r\n" + fileContents.toString());
            out.flush();
        }
        else
            notFoundResponse(out,fileName);

    }


    /******************************************************************************************************************************************************************/
    //                             Send back to client an HTML directory with hot links of the current files and directories they requested
    /******************************************************************************************************************************************************************/
    public void dirResponse(PrintStream out, Socket sock, String directory) throws IOException{
        String directoryName = "";  // String variable to store the directory we want to open
        String parentDir = "";      // String variable to store the parent directory

        // Use string builder to get the long HTTP response back to client because it will be the whole contents of the directory
        StringBuilder dirContents = new StringBuilder();    


        // Checking if the request is for the current directory because the request sent for current directory is empty string
        if (directory.equals(""))
            directoryName = "./";   // Set to current directory
        else
            directoryName = directory;  // otherwise set it to directory requesting to open


        File dir = new File(directoryName);     // Open the directory to read through
        
        if (dir.exists()){

            System.out.println("Opening directory: " + dir.getPath() + "\n");      // Print out which directory we are opening
            dirContents.append("<pre> \n <h1>Index of " + dir.getPath() + "</h1>\n");   // Display current directory in HTML form

        
            // If we are in current directory we do not want access to our inner files so calls to parent directory will just go to current directory
            if (directoryName.equals("./"))
                parentDir = ".";    // set to current directory so access to inner files is prevented
            else
                parentDir = "..";   // Otherwise just go to parent directory

            // Put in HTML hot link to go to parent directory
            dirContents.append("<a style = font-weight:bold href ="+parentDir+"/><---PARENT DIRECTORY</a><br>\n");



            // Get all the files and directory under diretcory opened
            File[] strFilesDirs = dir.listFiles();
            // Loop through contents of directory and create hot links for each one and specifying what type of file it is
            for ( int i = 0; i < strFilesDirs.length; i++ ) {
                if (strFilesDirs[i].isDirectory()){
                    dirContents.append("<a href="+strFilesDirs[i].getName()+"/>Dir: "+strFilesDirs[i].getName()+"/</a><br>\n");
                }   
                else if (strFilesDirs[i].isFile()){
                    dirContents.append("<a href="+strFilesDirs[i].getName()+">File: "+strFilesDirs[i].getName()+"</a><br>\n");
                }
            }


            // Write back to client the proper header response with content length, content type, and contents of the file            
            out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + (dirContents.toString().getBytes().length+1) + "\r\n" 
                            + "Content-Type: text/html\r\n" + "\r\n\r\n" + dirContents.toString());
            out.flush();
        }
        else
            notFoundResponse(out,directoryName);
    }


    /******************************************************************************************************************************************************************/
    //                             Respond back to client with a 404 HTTP response saying that the requested file/directory is not on the web server
    /******************************************************************************************************************************************************************/
    public void notFoundResponse(PrintStream out, String file){
        // Create HTML page to display to user saying that the requested file could not be found on the server
        String notFound = "<h1>404 Not Found</h1>\n" + "<a style = font-weight:bold href =./><---HOME DIRECTORY</a><br>\n"
                            + file + " was not found on the web server!";
        out.println("HTTP/1.1 404 NOT FOUND\r\n" + "Content-Length: " + (notFound.length()+2) + "\r\n" 
                        + "Content-Type: text/html\r\n" + "\r\n\r\n" + notFound);
        out.flush(); 
    }
}



public class MyWebServer {

  public static boolean controlSwitch = true;

  public static void main(String a[]) throws IOException {
    int q_len = 6; /* Number of requests for OpSys to queue */
    int port = 2540;
    Socket sock;

    ServerSocket servsock = new ServerSocket(port, q_len);

    System.out.println("Elijah Caluya's Web Server running at 2540.\n");
    while (controlSwitch) {
      // wait for the next client connection:
      sock = servsock.accept();
      new WebServerWorker (sock).start(); // Uncomment to see shutdown bug:
      //try{Thread.sleep(10000);} catch(InterruptedException ex) {}
    }
  }
}