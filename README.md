# CSC_435
Git Repo for CSC 435 Distributed Systems class at DePaul University

JokeServer:
- JokeServer.java
- JokeClient.java
- JokeClientAdmin.java
Joke clients connect to joke server and requests a joke or proverb. The server then sends back the joke or proverb and the state is maintained in only the client. The state is sent back and forth between the client and server.
The JokeClientAdmin is able to change the mode in sending a joke or proverb to the client. The admin toggles this by pressing enter.
Secondary server functionality is available for client, admin, and server.


MyWebServer:
- MyWebserver.java
MyWebServer is run a seperate command prompt window and the user connects with it via web browser at http://localhost:2540/. This then opens the current directory that MyWebServer is stored in wtih the functionality to open the files and go to other subdirectories. MyWebServer can also display html or txt files that is entered through the browser request bar i.e. http://localhost:2540/dog.txt will open dog.txt in the web browser. MyWebServer will also take GET request from FORM method in the addnums.html page and return a formatted result in the web browser. MyWebServer has functionality to visit the parent directories of subdirectories, but also does not allow the user to visit the parent directory in which MyWebServer is stored in.
