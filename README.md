# CSC_435
Git Repo for CSC 435 Distributed Systems class at DePaul University

JokeServer:
- JokeServer.java
- JokeClient.java
- JokeClientAdmin.java
- Joke clients connect to joke server and requests a joke or proverb. The server then sends back the joke or proverb and the state is maintained in only the client. The state is sent back and forth between the client and server.
The JokeClientAdmin is able to change the mode in sending a joke or proverb to the client. The admin toggles this by pressing enter.
Secondary server functionality is available for client, admin, and server.



MyWebServer:
- MyWebserver.java
- MyWebServer is run a seperate command prompt window and the user connects with it via web browser at http://localhost:2540/. This then opens the current directory that MyWebServer is stored in wtih the functionality to open the files and go to other subdirectories. MyWebServer can also display html or txt files that is entered through the browser request bar i.e. http://localhost:2540/dog.txt will open dog.txt in the web browser. MyWebServer will also take GET request from FORM method in the addnums.html page and return a formatted result in the web browser. MyWebServer has functionality to visit the parent directories of subdirectories, but also does not allow the user to visit the parent directory in which MyWebServer is stored in.




Blockchain:
- Blockchain.java
- Use script to start three different processes of Blockchain
      - In script:
          REM for three procesess:
          start java Blockchain 0
          start java Blockchain 1
          java Blockchain 2
- The program will wait until process 2 begins to actually start the blockchain process.
- The 3 processes compete to verify a puzzle. Once a process finishes, the other processes stop.
- Once verified the block, in the form of txt file input, is encrypted and added to the Blockchain and the updated Blockchain is sent to the other processes.
- Multiple marshalling/unmarshalling and en/decrypting methods in between to get the correct information from the input and from other processes.
- All blocks are able to compete to verify blocks as well as contribute to the Blockchain and the final result is printed in an XML file.
- Overall simulates Blockchain/Ledger-based entities in a smaller setting
  - Note: in Class Assignment, we needed to implement functionality to press certain keys to show the blockchain and verify the           blockchain. I was not able to implement this, but I was able to implement the core Blockchain function of processes competing to         verify blocks, add them to the Blockchain, send the updated Blockchain to the other processes, and start with the next block.
