# CSC_435
Git Repo for CSC 435 Distributed Systems class at DePaul University

JokeServer:
- JokeServer.java
- JokeClient.java
- JokeClientAdmin.java
Joke clients connect to joke server and requests a joke or proverb. The server then sends back the joke or proverb and the state is maintained in only the client. The state is sent back and forth between the client and server.
The JokeClientAdmin is able to change the mode in sending a joke or proverb to the client. The admin toggles this by pressing enter.
Secondary server functionality is available for client, admin, and server.
