/*--------------------------------------------------------

1. Elijah Caluya / Date: 10/13/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac MyWebServer.java


4. Precise examples / instructions to run this program:

e.g.:

> java MyWebServer

Open up Firefox web browser and make web requests on http://localhost:2540/


5. List of files needed for running the program.

e.g.:

1) MyWebServer.java


5. Notes:

e.g.:
Start the MyWebServer program first then make requests to the Web Server in a seperated Firefox browser on http://localhost:2540/.
Requests to specific files on the web server will return the opened file and requests for a directory will display the conents of the 
directory in the HTML page. The Web Server will also interpret GET requests for FORM method on addnums.html file. There is specific
code to interpret the .fake-cgi request in the addnums.html file and return a formatted HTML page with the data entered.
The Web Server will also respond with 404 Response code if the user is requesting a file that does not exists on the web server or
if the file requested is not a directory, .txt, or .html file.

----------------------------------------------------------*/

import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries
import java.util.*; // For array of File objects

class WebServerWorker extends Thread {    // Class definition
    Socket sock;                   // Class member, socket, local to ListnWorker.
    WebServerWorker (Socket s) {sock = s;} // Constructor, assign arg s
                                      //to local sock
    public void run(){
        // Get I/O streams from the socket:
        PrintStream out = null;
        BufferedReader in = null;
        String getRequest = "";     // String to hold the file/directory after GET and before HTTP/1.1
        try {
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            // Read in from the input stream request from client
            String request;
            request = in.readLine();
            

            if (request != null){       // Read in all request lines
                if (!request.contains("favicon.ico")){  // Ignore favicon.ico GET request
                    System.out.println(request);    // Print out tonly the first GET request with file/directory name

                    getRequest = parseRequest(request);   // Store the desired file/directory name
                
                    // Keep reading in while the BufferedReader is still ready to be read
                    // Referenced from: https://www.tutorialspoint.com/java/io/bufferedreader_ready.htm
                    while (in.ready()){
                        request = in.readLine();
                        System.out.println(request);    // Print out the Entire request from the client
                    }

                    // If the request is to not see current directory
                    if (!(getRequest.length() == 0)){
                        // If trailing "/" then user reqested a directory
                        if (getRequest.substring(getRequest.length()-1).equals("/"))
                            dirResponse(out,getRequest);    // Send directory response

                        // If .html or .txt then the user is requesting for a file
                        else if (getRequest.contains(".html") || getRequest.contains(".txt"))
                            fileResponse(out,getRequest);   // Send file response

                        // If the response is coming from FORM method
                        else if (getRequest.contains(".fake-cgi?"))
                            addNums(out,getRequest);    // Send response for FORM method

                        // If the request is invalid (i.e. invalid file type, invalid request, or file not in server)
                        // Send back to client a 404 NOT FOUND header response
                        else 
                            notFoundResponse(out,getRequest);   // Send 404 response code
                        
                    }
                    // If the request is to see the current directory MyWebServer is started in
                    else
                        dirResponse(out,getRequest);
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
        // Parse file/directory name  in between GET and HTTP/1.1 request string
        fileName = request.substring(frontIndex+1,endIndex);

        return fileName;
    }



    /******************************************************************************************************************************************************************/
    //                                         Send back to client the opened file that they requested
    /******************************************************************************************************************************************************************/
    public void fileResponse(PrintStream out, String request) throws IOException{
        String fileName = "";   // String variable to store the file to open
        String mimeType = "";   // String variable to store the MIME type depending on what type of file is being opened

        fileName = request;

        // Set the correct MIME type depending on what type of file the user is requesting
        if (fileName.substring(fileName.length()-5).equals(".html"))
            mimeType = "text/html";     // If HTML file set MIME type to text/html
        else if (fileName.substring(fileName.length()-4).equals(".txt"))
            mimeType = "text/plain";    // If text file set MIME type to text/plain
            
        System.out.println("Opening file: " + fileName + "\n"); // Print on console what file is being opened


        // Create File variable to open the file by file name
        File file = new File(fileName);

        // Buffered reader to read through the opened file
        BufferedReader readFile = null;
        String line = "";

        // Use this String variable to store the contents of the file By continously adding onto the string after each line read.
        String fileContents = "";


        // All knowledge and methods for Files referenced from Prof. Elliott's examples and https://www.geeksforgeeks.org/file-class-in-java/
        // Check if file exists to avoid FileNotFoundException
        if (file.exists()){
            // Read through contents of file with a Buffered Reader
            readFile = new BufferedReader(new FileReader(fileName));
          
            // Append all of the contents in the file to a String Builder Object
            while ((line = readFile.readLine()) != null)
                fileContents+=line + "\n";
                

            // Write back to client the proper header response with content length, content type, and contents of the file            
            out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + (fileContents.length()+1) + "\r\n" 
                            + "Content-Type: " + mimeType + "\r\n" + "\r\n\r\n" + fileContents);
            out.flush();
        }
        else
            notFoundResponse(out,fileName);     // The file does not exist on the web server so send 404 response code

    }



    /******************************************************************************************************************************************************************/
    //                             Send back to client an HTML directory with hot links of the current files and directories they requested
    /******************************************************************************************************************************************************************/
    public void dirResponse(PrintStream out, String directory) throws IOException{
        String directoryName = "";  // String variable to store the directory we want to open
        String parentDir = "";      // String variable to store the parent directory

        // Create String variable to store all the contents of te opened file.
        String dirContents = "";    


        // Checking if the request is for the current directory because the request sent for current directory is empty string
        if (directory.equals(""))
            directoryName = "./";   // Set to current directory
        else
            directoryName = directory;  // otherwise set it to directory requesting to open


        File dir = new File(directoryName);     // Open the directory to read through
        
        // Checking if the directory is in the web server
        if (dir.exists()){
            System.out.println("Opening directory: " + dir.getPath() + "\n");      // Print out which directory we are opening
            dirContents += "<pre> \n <h1>Index of " + dir.getPath() + "</h1>\n";   // Display current directory in HTML form

        
            // If we are in current directory we do not want access to our inner files so calls to parent directory will just go to current directory
            if (directoryName.equals("./"))
                parentDir = ".";    // set to current directory so access to inner files is prevented
            else
                parentDir = "..";   // Otherwise just go to parent directory

            // Put in HTML hot link to go to parent directory
            dirContents += "<a style = font-weight:bold href =" + parentDir + "/><---PARENT DIRECTORY</a><br>\n";


            // Get all the files and directory under diretcory opened
            File[] strFilesDirs = dir.listFiles();
            // Loop through contents of directory and create hot links for each one and specifying what type of file it is
            for ( int i = 0; i < strFilesDirs.length; i++ ) {
                if (strFilesDirs[i].isDirectory()){
                    dirContents += "<a href="+strFilesDirs[i].getName()+"/>Dir: "+strFilesDirs[i].getName()+"/</a><br>\n";
                }   
                else if (strFilesDirs[i].isFile()){
                    dirContents += "<a href="+strFilesDirs[i].getName()+">File: "+strFilesDirs[i].getName()+"</a><br>\n";
                }
            }


            // Write back to client the proper header response with content length, content type, and contents of the file            
            out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + (dirContents.length()+1) + "\r\n" 
                            + "Content-Type: text/html\r\n" + "\r\n\r\n" + dirContents);
            out.flush();
        }
        else
            notFoundResponse(out,directoryName);    // The directory does not exist on web server so send 404 response code
    }


    /******************************************************************************************************************************************************************/
    //                             Respond back to client with a 404 HTTP response saying that the requested file/directory is not on the web server
    /******************************************************************************************************************************************************************/
    public void notFoundResponse(PrintStream out, String file){
        // Create HTML page to display to user saying that the requested file could not be found on the server
        String notFound = "<h1>404 Not Found</h1>\n" + "<a style = font-weight:bold href =./><---HOME DIRECTORY</a><br>\n"
                            + file + " was not found on the web server!";
        // Send back proper 404 NOT FOUND header response back to the client
        out.println("HTTP/1.1 404 NOT FOUND\r\n" + "Content-Length: " + (notFound.length()+2) + "\r\n" 
                        + "Content-Type: text/html\r\n" + "\r\n\r\n" + notFound);
        out.flush();

        System.out.println("Could not find " + file + " in Web Server!");
        System.out.flush(); 
    }


    /******************************************************************************************************************************************************************/
    //                                          Method to handle the FORM method in addnums.html
    /******************************************************************************************************************************************************************/
    public void addNums(PrintStream out, String request) throws IOException{
        // Get all the info from FORM after .fake-cgi to parse string
        int formIndex = request.indexOf("?");
        String formEntry = request.substring(formIndex);
        
        // Get the name from FORM. Start with index 8 because first 8 characters are "?person="
        String name = formEntry.substring(8,formEntry.indexOf("&num1="));
        // Get the first number from FORM
        String num1 = formEntry.substring(formEntry.indexOf("&num1=")+6,formEntry.indexOf("&num2="));
        // Get the second number from FORM
        String num2 = formEntry.substring(formEntry.indexOf("&num2=")+6,formEntry.length());

        int sum = 0;
        Boolean validEntry = true;
        // Check to see if the user entered valid numbers to add up
        try {
            // Get the sum of the numbers entered
            sum = Integer.parseInt(num1) + Integer.parseInt(num2);
        }
        catch (Exception e){
            validEntry = false; // Make value false if the user did not enter numbers in any of the boxes
        }

        String addNumString = "";   // Store string to send back to client in header

        if (validEntry){
            // Create string with the entered name, numbers, and the sum of those numbers for the HTML page to be returned
            addNumString = "<a style = font-weight:bold href = /addnums.html><---Back to addnums.html</a><br>\n" +
                            "<h1>Add Nums Result:</h1>\n <p>Dear " + name + ", the sum of " + num1 + " and " + num2
                            + " is " + Integer.toString(sum) + "</p>";
        }
        else {
            // Create string for response header saying that the user entered invalid numbers if they input an invalid entry
            addNumString = "<a style = font-weight:bold href = /addnums.html><---Back to addnums.html</a><br>\n" +
                            "<h1>Add Nums Result:</h1>\n <p>Dear " + name + ", please enter valid numbers in the entry boxes.</p>";
        }
            
                                
        // Send back to client with proper header response with the evaluated HTML string 
        out.println("HTTP/1.1 200 OK\r\n" + "Content-Length: " + (addNumString.length()+1) + "\r\n" 
                        + "Content-Type: text/html\r\n" + "\r\n\r\n" + addNumString);
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