/*--------------------------------------------------------

1. Elijah Caluya / Date: 11/3/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac Blockchain.java


4. Precise examples / instructions to run this program:

e.g.:
Run in seperate command windows in this order. The program will not fully start until process 2 has started.

> java Blockchain 0
> java Blockchain 1
> java Blockchain 2

5. List of files needed for running the program.

e.g.:

1) Blockchain.java


5. Notes:

e.g.:
Program does not start until process 2 is started in another window. This starts all the processes to start competing for verifying
the blocks. Once a block has been verified, the block will be added to the process' local block chain. Then that updated block chain
will be sent to the other processes so they will stop working to verify that block and then they can move on to the next block.

I was not able to implement the user pressing C for credit, R for reading of file records, V for verifying the blockchain, and
L for listing each block.

----------------------------------------------------------*/

/*
Web sources used by Prof. Elliott in his starter files:

http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object

XML validator:
https://www.w3schools.com/xml/xml_validator.asp

XML / Object conversion:
https://www.mkyong.com/java/jaxb-hello-world-example/


 I have included references to the  web sources that I have used in my code if they were not referenced by Prof. Elliot.
*/


// From BlockInputE.java
// Mainly used for marshalling and unmarshalling data to and from XML
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


// From WorkB.java
// DataTypeConverter used for converting string into hexadecimal values
import javax.xml.bind.DatatypeConverter;


// From BlockI.java
// Mainly used for encryption and key pair values
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;


// Mainly used for threads, ObjectOutputStreams, ObjectInputStreams
import java.util.*;
import java.text.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;



/******************************************************************************************************************************************************************/
// Main class for Blockchain
public class Blockchain {

	public static int KeyServerPortBase = 4710;		// Port number for server receiving Public keys
  	public static int UnverifiedBlockServerPortBase = 4820;	// Port number for server receiving unverified blocks
  	public static int BlockchainServerPortBase = 4930;	// Port number for server receiving updated blockchains

  	// Need private key to sign
  	private static PrivateKey privateKey;
  	
  	// Linked List to implement the BlockChain. Referenced from https://www.javatpoint.com/java-linkedlist
  	// Use linked list so we can easily add and pull the previous block from the block chain
  	public static LinkedList<BlockRecord> BlockChain;

  	// Use Blocking Queue for the Univerified Blocks. Referenced from https://www.geeksforgeeks.org/blockingqueue-interface-in-java/
  	// Need blocking queue so we can wait for when the queue is non-empty to retrieve and remove elements
  	public static BlockingQueue<BlockRecord> Queue;
  	
  	// Variable for storing the current Process ID
  	public static int PID = 0;

  	// Our Alpha numeric string to do work on
  	private static String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  	// Since we are only testing with processes 0, 1, and 2, set the total number of processes to 3 for reference
  	public static int totalNumProcesses = 3;

  	// Value to make sure the program does not start unless process 2 starts
  	public static Boolean runSystem = false;

	public static void main(String args[]) throws Exception {

		if (args.length < 1) PID = 0;
    	else if (args[0].equals("0")) PID = 0;
    	else if (args[0].equals("1")) PID = 1;
    	else if (args[0].equals("2")) PID = 2;
    	else PID = 0; 	// If the process number provided is not 0-2, then default to 0

    	
    	// Inititalize the queue so that it is able to prioritize blocks based on time
    	Queue = new PriorityBlockingQueue<BlockRecord>(10,new CompareBlockRecord());

    	// Create Hashmap to match the process ID with public key
    	HashMap<String,PublicKey> procPublicKey = new HashMap<String,PublicKey>();

    	// Initialize CountDownLatch so we can wait until all public keys are read by the process.
    	// Use of java.util.concurrent.CountDownLatch is referenced from: https://www.geeksforgeeks.org/countdownlatch-in-java/
    	CountDownLatch countDownLatch = new CountDownLatch(1);

    	BlockChain = new LinkedList<BlockRecord>();	// Initialize block chain 

    	System.out.println("Process: " + Integer.toString(PID) + " is running");

    	// Start the PublicKeyServer thread with the process ID from the arguments
    	PublicKeyServer pubKeyServer = new PublicKeyServer(KeyServerPortBase + PID,procPublicKey,countDownLatch);
    	new Thread(pubKeyServer).start();

    	// Start the UnverifiedBlockServer thread with process ID from the arguments
    	UnverifiedBlockServer unverifiedBlockServer = new UnverifiedBlockServer(UnverifiedBlockServerPortBase + PID);
    	new Thread(unverifiedBlockServer).start();

    	// Start the the BlockChainServer thread with process ID from the arguments
    	BlockChainServer blockChainServer = new BlockChainServer(BlockchainServerPortBase + PID);
    	new Thread(blockChainServer).start();

    	// Once we have process two running, we can start the system.
    	if (PID == 2) runSystem = true;

    	// Sleep to wait until all processes have started to run the system.
    	while (!runSystem) Thread.sleep(200);

    	// Start the system when all processes have started
    	startSystem(procPublicKey,countDownLatch);
    	countDownLatch.await();
    	Thread.sleep(10000);
	}



/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files bc.java, WorkB, and BlockInputE.java
	// Function to start the whole system
	public static void startSystem(HashMap<String,PublicKey> procPubKey, CountDownLatch latch){
		Socket sock;
		ObjectOutputStream out = null;

		try {
			String file = "BlockInput" + Integer.toString(PID) + ".txt";

			if (PID == 2){
				BlockRecord dummyBlock = new BlockRecord();	// Start with new dummy block
				String id = new String(UUID.randomUUID().toString());	// Create UUID for process
				dummyBlock.setABlockID(id);

				String marshalledBlock = marshalToXML(dummyBlock);	// Marshal the block data to XML
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(marshalledBlock.getBytes());
				byte[] byteHash = md.digest();
				String hashString = Base64.getEncoder().encodeToString(byteHash);	// Encode to SHA256 string with Base64 encoding
				dummyBlock.setASHA256String(hashString);
				dummyBlock.setBlockNum(1);
				BlockChain.add(dummyBlock);

				for (int i = 0; i < totalNumProcesses; i++) {
					// Create output stream to send the marshalled objects
					sock = new Socket("localhost", BlockchainServerPortBase + i);
					out = new ObjectOutputStream(sock.getOutputStream());
					
					// Set up Block Record Group for the BlockChain
					BlockRecordGroup brg = new BlockRecordGroup();
					brg.setBlockRecordGroup(BlockChain);

					// Marshal the group so we can send it to each server
					JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecordGroup.class);
					Marshaller marshaller = jaxbContext.createMarshaller();
					StringWriter writer = new StringWriter();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
					marshaller.marshal(brg,writer);
					String xmlString = writer.toString();
					out.writeObject(xmlString);
				}
				out.flush();
				out.close();
			}
			sendPubKey();	// Send out Public key to all processes

			// CountDownLatch await() method referenced from https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/CountDownLatch.html 
			latch.await();	// Make current thread to wait until the latch is counted down to zero

			// Read in the text file so we can get the data for a block and send that unverified block to the other processes
			readFile(file);

			// Verify the blocks and update the block chain when a solution is found
			VerifyBlocks vb = new VerifyBlocks(procPubKey,privateKey);
			vb.start();
		}
		catch (Exception e) {e.printStackTrace();}
	}



/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files BlockI.java and BlockInputE.java
	//Method for marshalling Block record to XML
	public static String marshalToXML(BlockRecord br){
		String xmlString = "";
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			StringWriter writer = new StringWriter();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(br,writer);
			xmlString = writer.toString();
		}
		catch (Exception e){e.printStackTrace();}
		

		return xmlString;
	}


/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files BlockInputE.java and BlockI.java
	// Method for sending public key of the current process to all of the processes.
	public static void sendPubKey(){
		Socket sock;
		ObjectOutputStream out;

		try {
			for (int i = 0; i < totalNumProcesses; i++){
				sock = new Socket("localhost",KeyServerPortBase+i);	// Create new connection for each process
				PublicKeyBlockRecord pkbr = new PublicKeyBlockRecord();	// Create new PublicKeyBlockRecord variable
				
				// Create the public key and private key
				KeyPair kp = generateKeyPair(1000);
				PublicKey pubKey = kp.getPublic();
				privateKey = kp.getPrivate();
				
				// Encode the public key and set properties of the PublicKeyBlockRecord object
				String encodedPubKey = Base64.getEncoder().encodeToString(pubKey.getEncoded());
				pkbr.setPID(PID);	// Set process ID of PublicKeyBlockRecord
				pkbr.setPubKey(encodedPubKey);	// Set Public Key of PublicKeyRecord

				// Marshal the PublicKeyRecord into XML and then send to each public key server that is running
				JAXBContext jaxbContext = JAXBContext.newInstance(PublicKeyBlockRecord.class);
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
				StringWriter writer = new StringWriter();
				marshaller.marshal(pkbr,writer);
				String xmlString = writer.toString();

				// Write the XML object out with ObjectOutputStream
				out = new ObjectOutputStream(sock.getOutputStream());
				out.writeObject(xmlString);
				out.flush();
				out.close();
			}
		}
		catch (Exception e){e.printStackTrace();}
	}


/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files BlockInputE.java and BlockI.java
	// Method for reading in the txt files into Block Records
	public static void readFile(String fileName){
		// BufferedReader use referenced from https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html
		BufferedReader reader = null;	// BufferedReader used for reading the txt file
		Socket sock;	// Socket for making connection with UnverifiedBlockServer
		ObjectOutputStream out;	// ObjectOutputStream used for sending the updated xml object to the other processes
		String uuid = "";
		String input = "";


		try {
			reader = new BufferedReader(new FileReader(fileName));
			String[] tokens = new String[10];	// String array for tokens to put into Block Record
			input = reader.readLine();

			while (input != null){
				BlockRecord block = new BlockRecord();	// Make a new block
				uuid = new String(UUID.randomUUID().toString());
				block.setABlockID(uuid);	// Set the block id to the newly made random UUID
				block.setACreatingProcess("Process" + Integer.toString(PID));	// Set Creating process ID to the PID
				
				byte[] signedBytes = signData(uuid.getBytes(),privateKey); // Sign the block with private key
				String signedBlock = Base64.getEncoder().encodeToString(signedBytes);
				block.setSignedBlockID(signedBlock);	// Set the signed block ID to newly signed block ID

				// Create Timestamp for the block
				Date date = new Date();
				String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
				String TimeStampString = T1 + "." + PID + "\n";

				// Set token split delimeter and then set the entries for the block from the data 
				tokens = input.split(" +");
				block.setTimeCreated(TimeStampString);
				block.setSeed("");
				// Set by index since we know the order in which the data comes through
				block.setFFname(tokens[0]);
				block.setFLname(tokens[1]);	
				block.setFDOB(tokens[2]);
				block.setFSSNum(tokens[3]);
				block.setGDiag(tokens[4]);
				block.setGTreat(tokens[5]);
				block.setGRx(tokens[6]);

				// Marshal the block to xml
				String xmlString = marshalToXML(block);
				String xmlSeed = xmlString.substring(xmlString.indexOf("<seed>"));

				// Sign the xml string with SHA256
				MessageDigest MD = MessageDigest.getInstance("SHA-256");
				MD.update(xmlSeed.getBytes());
				byte[] byteData = MD.digest();

				// Set SHA256 string to the block header as well as the signed version of the string
				String SHA256string = Base64.getEncoder().encodeToString(byteData);
				block.setASHA256String(SHA256string);
				byte[] signedHash = signData(byteData,privateKey);
				String signedSHA256 = Base64.getEncoder().encodeToString(signedHash);
				block.setASignedSHA256(signedSHA256);
				xmlString = marshalToXML(block);	// Marshal block to XML with the updated info read in

				String encodedXML = Base64.getEncoder().encodeToString(xmlString.getBytes());

				// Send out the updated block to all of the processes
				for (int i = 0; i < totalNumProcesses; i++){
					sock = new Socket("localhost",UnverifiedBlockServerPortBase + i);
					out = new ObjectOutputStream(sock.getOutputStream());
					out.writeObject(encodedXML);
					out.flush();
					out.close();
					System.out.println("Sent Unverified Encoded XML to all processes");
				}

				input = reader.readLine();	// Read in any more lines if there are any
			}
		}
		catch (Exception e){e.printStackTrace();}
	}

/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files BlockI.java
	// Method for generating key pair
	public static KeyPair generateKeyPair(long seed) throws Exception {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
    	SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
    	rng.setSeed(seed);
    	keyGenerator.initialize(1024, rng);

    	return (keyGenerator.generateKeyPair());
	}

/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files BlockI.java
	// Method for signing data using a private key
	public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
    	Signature signer = Signature.getInstance("SHA1withRSA");
    	signer.initSign(key);
    	signer.update(data);
    	return (signer.sign());
  	}

/******************************************************************************************************************************************************************/
	// Helper method for checking if the given Block Record is in the block chain
	public static boolean isInBlockChain(BlockRecord br){
		for (BlockRecord b : BlockChain){
			if (b.getABlockID().equals(br.getABlockID()))
				return true;
		}
		return false;
	}

/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files BlockI.java
	// Helper method to verify signature with a public key
	public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
    	Signature signer = Signature.getInstance("SHA1withRSA");
    	signer.initVerify(key);
    	signer.update(data);

    	return (signer.verify(sig));
  	}

/******************************************************************************************************************************************************************/
	// Referenced from Prof. Elliott's starter files WorkB.java
	// Helper function to get a random alphanumeric string
	public static String randomAlphaNumeric(int count) {
    	StringBuilder builder = new StringBuilder();
    	while (count-- != 0) {
      	int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
      	builder.append(ALPHA_NUMERIC_STRING.charAt(character));
    	}
    	return builder.toString();
  	}


}


/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's starter files BlockInputE.java and BlockI.java
// Class for the actual Block Record Data
@XmlRootElement
class BlockRecord {
	String SHA256String;
	String SignedSHA256;
	String BlockID;
	String VerificationProcessID;
	String CreatingProcess;
	String PreviousHash;
	String Fname;
	String Lname;
	String SSNum;
	String DOB;
	String Diag;
	String Treat;
	String Rx;
	String SignedBlockID;
	int BlockNum;
	String Seed;
	String TimeCreated;


	// Getter and setter methods for the block data
	public String getASHA256String() {return SHA256String;}
	@XmlElement
	  public void setASHA256String(String SH){this.SHA256String = SH;}

	public String getASignedSHA256() {return SignedSHA256;}
	@XmlElement
	  public void setASignedSHA256(String SH){this.SignedSHA256 = SH;}

	public String getACreatingProcess() {return CreatingProcess;}
	@XmlElement
	  public void setACreatingProcess(String CP){this.CreatingProcess = CP;}

	public String getAVerificationProcessID() {return VerificationProcessID;}
	@XmlElement
	  public void setAVerificationProcessID(String VID){this.VerificationProcessID = VID;}

	public String getABlockID() {return BlockID;}
	@XmlElement
	  public void setABlockID(String BID){this.BlockID = BID;}

	public String getFSSNum() {return SSNum;}
	@XmlElement
	  public void setFSSNum(String SS){this.SSNum = SS;}

	public String getFFname() {return Fname;}
	@XmlElement
	  public void setFFname(String FN){this.Fname = FN;}

	public String getFLname() {return Lname;}
	@XmlElement
	  public void setFLname(String LN){this.Lname = LN;}

	public String getFDOB() {return DOB;}
	@XmlElement
	  public void setFDOB(String DOB){this.DOB = DOB;}

	public String getGDiag() {return Diag;}
	@XmlElement
	  public void setGDiag(String D){this.Diag = D;}

	public String getGTreat() {return Treat;}
	@XmlElement
	  public void setGTreat(String D){this.Treat = D;}

	public String getGRx() {return Rx;}
	@XmlElement
	  public void setGRx(String D){this.Rx = D;}

	public String getSignedBlockID(){return this.SignedBlockID;}
	@XmlElement
		public void setSignedBlockID(String SBID){this.SignedBlockID = SBID;}

	public int getBlockNum(){return this.BlockNum;}
	@XmlElement
		public void setBlockNum(int BN){this.BlockNum = BN;}

	public String getSeed(){return this.Seed;}
	@XmlElement
		public void setSeed(String S){this.Seed = S;}

	public String getTimeCreated(){return this.TimeCreated;}
	@XmlElement
		public void setTimeCreated(String TC){this.TimeCreated = TC;}
}


/******************************************************************************************************************************************************************/
// Class to compare TimeCreated of so the BlockRecord with the earliest timestap is pulled from the queue first.
class CompareBlockRecord implements Comparator<BlockRecord> {
	public int compare(BlockRecord first, BlockRecord second){
		return first.getTimeCreated().compareTo(second.getTimeCreated());
	}
}


/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's starter files BlockInputE.java and BlockI.java
// Class for for a group of block records that can be marshalled and unmarshalled to an object with JAXB annotation.
// Mainly used for being able to marshal and present in XML the whole Block Chain
@XmlRootElement
class BlockRecordGroup {
	List<BlockRecord> BlockRecords;

	// Get and set methods
	public List<BlockRecord> getBlockRecordGroup(){return BlockRecords;}
	@XmlElement
		public void setBlockRecordGroup(List<BlockRecord> records){BlockRecords = records;}
}



/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's starter files BlockInputE.java and BlockI.java
// Class for Public Key record for matching the Process ID with its public key. Can be marshalled and unmarshalled with JAXB annotation.
@XmlRootElement
class PublicKeyBlockRecord{
	String PubKey;
	int PID;

	// Getter and setter methods for the public key data
	public String getPubKey(){return this.PubKey;}
	@XmlElement
		public void setPubKey(String PK){this.PubKey = PK;}

	public int getPID(){return this.PID;}
	@XmlElement
		public void setPID(int ID){this.PID = ID;}
}



/******************************************************************************************************************************************************************/
// PublicKeyServer class referenced from Prof. Elliott's starter file bc.java
// Used to accept public key record connections from clients
class PublicKeyServer implements Runnable {

	static ServerSocket sock;
	int port;

	// Use HashMap to make key value pair for the Processes' public key
	HashMap<String,PublicKey> procPubKey;
	// Use of java.util.concurrent.CountDownLatch is referenced from: https://www.geeksforgeeks.org/countdownlatch-in-java/
	CountDownLatch latch;

	// Constructor for PublicKeyServer class
	public PublicKeyServer(int p, HashMap<String,PublicKey> procPK, CountDownLatch l){
		this.port = p;
		this.procPubKey = procPK;
		this.latch = l;
	}


	public void run() {
		try {
			// Start up the server to listen to client connections with public key records
			System.out.println("Starting Public Key Server input thread using " + Integer.toString(port));
			sock = new ServerSocket(port,6);

			// Accept connections then start PublicKeyWorker thread
			while (true) {
				Socket socket = sock.accept();		
				new PublicKeyWorker(socket, procPubKey,latch).start();
			}
		} 
		catch (Exception e){e.printStackTrace();}
	}



	// PublicKeyWorker class referenced from Prof. Elliott's starter files bc.java and BlockI.java
	// Public Key Worker class that sends public key record with request from client
	class PublicKeyWorker extends Thread {
		Socket sock;
		HashMap<String,PublicKey> procPubKey;
		CountDownLatch latch;

		// Use of ObjectInputStream from: http://tutorials.jenkov.com/java-io/objectinputstream.html
		ObjectInputStream in;
		String xmlPubKey = "";

		public PublicKeyWorker(Socket s, HashMap<String,PublicKey> procPK, CountDownLatch l){
			this.sock = s;
			this.procPubKey = procPK;
			this.latch = l;
		}

		public void run(){
			try {
				in = new ObjectInputStream(sock.getInputStream());
				xmlPubKey = (String) in.readObject();	// Read in from input stream and store public key
				// Create JAXBContext for unmarshalling string xml public key
				JAXBContext jaxbContext = JAXBContext.newInstance(PublicKeyBlockRecord.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(xmlPubKey);
				PublicKeyBlockRecord keyBlockRecord = (PublicKeyBlockRecord)unmarshaller.unmarshal(reader);	// Unmarshal into PublicKeyBlockRecord object

				String pubKey = keyBlockRecord.getPubKey();
				int pid = keyBlockRecord.getPID();

				// Decode from Base64 into byte array and then generate public key from the decoded bytes
				byte[] decodePublicKey = Base64.getDecoder().decode(pubKey);
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodePublicKey));

				System.out.println("Retrieved public key for Process " + Integer.toString(pid) + ":" + publicKey.getEncoded());

				procPubKey.put("Process" + Integer.toString(pid),publicKey);	// Add the process ID and public key to the hashmap

				if (procPubKey.size() == Blockchain.totalNumProcesses)
					latch.countDown();
			}
			catch (Exception e) {e.printStackTrace();}
		}
	}

}



/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's starter files bc.java and BlockI.java
// Class for processing Unverified blocks in processing queue
class UnverifiedBlockServer implements Runnable {
	
	ServerSocket sock;
	int port;
	HashMap<String,PublicKey> procPubKey;


	public UnverifiedBlockServer(int p){this.port = p;}

	public void run() {
		try {
			// Start up the server to listen 
			System.out.println("Starting Unverified Block Server thread using " + Integer.toString(port));
			sock = new ServerSocket(port,6);
			
			// Accept connections of unverified blocks and start the UnverifiedBlockWorker thread
			while (true){
				Socket socket = sock.accept();
				new UnverifiedBlockWorker(socket).start();
			}
		}
		catch (Exception e) {e.printStackTrace();}
	}


	// Worker class for updating the queue for unverified blocks
	class UnverifiedBlockWorker extends Thread {
		Socket sock;

		ObjectInputStream in;
		String inputString = "";
		String xmlInput = "";

		public UnverifiedBlockWorker(Socket s){this.sock = s;}


		public void run(){
			try {
				// Read in from the ObjectInputStream and put value into String
				in = new ObjectInputStream(sock.getInputStream());
				String inputString = (String)in.readObject();

				// Decode input string into bytes for unmarshalling
				byte[] decodeInput = Base64.getDecoder().decode(inputString.getBytes());	
				xmlInput = new String(decodeInput);

				// Unmarshal the input string into BlockRecord object
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(xmlInput);
				BlockRecord br = (BlockRecord) unmarshaller.unmarshal(reader);

				System.out.println("Got block record for unverified block from " + br.getACreatingProcess());

				// Add the unmarshalled block record into the queue
				Blockchain.Queue.put(br);

				System.out.println("Unverified block record id: " + br.getABlockID() + " added to queue.");
				in.close();	// close ObjectInputStream since we don't need it anymore
			}
			catch (Exception e) {e.printStackTrace();}
		}
	}
}



/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's Starter files in bc.java and BlockI.java
// Class for server that handles connections that update the Block Chain
class BlockChainServer implements Runnable {
	ServerSocket sock;
	int port;

	public BlockChainServer(int p){this.port = p;}

	public void run(){
		try {
			// Start up block chain server to listen for connections
			System.out.println("Starting Block Chain Server input thread using " + Integer.toString(port));
			sock = new ServerSocket(port,6);
			
			while (true){
				// Once server accepts client connection set the boolean variable runSystem to true so process0 and process1 can start
				Socket socket = sock.accept();
				if (!Blockchain.runSystem)
					Blockchain.runSystem = true;
				new BlockChainWorker(socket).start();	// Start the BlockChainWorker thread
			}
		}
		catch (Exception e){e.printStackTrace();}
	}


	// Worker class thread for updating the Block Chain
	class BlockChainWorker extends Thread {
		Socket sock;
		ObjectInputStream in;
		String inputString;

		public BlockChainWorker(Socket s){this.sock = s;}
	

		public void run(){
			try {
				// Read in the object and put into string for unmarshalling
				in = new ObjectInputStream(sock.getInputStream());
				inputString = (String)in.readObject();
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecordGroup.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(inputString);
				// Unmarshal the input read in from the Object Input Stream into Block Record Group class
				BlockRecordGroup brg = (BlockRecordGroup) unmarshaller.unmarshal(reader);
				// Create new BlockChain with the newly unmarshalled BlockRecordGroup
				LinkedList<BlockRecord> bc = new LinkedList<BlockRecord>(brg.getBlockRecordGroup());

				System.out.println("Received new Block Chain, now updating local blockchain with new one received.");

				Blockchain.BlockChain = bc;	// Update the BlockChain

				// Marshal new Blockchain to xml file only from process 0
				if (Blockchain.PID == 0){
					// Marshal the group of Block Records so we can add it to the ledger
					JAXBContext context = JAXBContext.newInstance(BlockRecordGroup.class);
					Marshaller marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
					// Use Buffered Writer to write to the xml file
					// BufferedWriter referenced from https://www.geeksforgeeks.org/io-bufferedwriter-class-methods-java/
					BufferedWriter write = new BufferedWriter(new FileWriter("BlockchainLedger.xml"));
					marshaller.marshal(brg,write);
					write.close();
					System.out.println("Wrote new blockchain to BlockChainLedger.xml");
				}
				in.close();
			}
			catch (Exception e){e.printStackTrace();}
		}

	}
}


/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's starter files BlockI.java and WorkB.java
// Main class the handles the unverified Blocks in the queue as well adding them to the block chain when work is done
class VerifyBlocks extends Thread {
	private HashMap<String,PublicKey> procPubKey;
	private PrivateKey privateKey;

	public VerifyBlocks(HashMap<String,PublicKey> ppk, PrivateKey pk){this.procPubKey = ppk; this.privateKey = pk;}


	public void run(){
		try {
			while (true){
				// Use of BlockingQueue.poll() referenced from https://www.programcreek.com/java-api-examples/?class=java.util.concurrent.BlockingQueue&method=poll
				// Block until there is a new item available with a 100 ms Timeout
				BlockRecord block = Blockchain.Queue.poll(100,TimeUnit.MILLISECONDS);	
				// If you get a null block, then try again
				if (block == null) continue;

				// Create a copy of the Blockchain so that we can verify the blocks
				LinkedList<BlockRecord> blockChainCopy = new LinkedList<BlockRecord>();
				for (BlockRecord b : Blockchain.BlockChain)
					blockChainCopy.add(b);

				int blockNum = blockChainCopy.size() + 1;	// Set the block number to the next block in the chain

				// Check to see if the block has already been verified and if it has then ignore it and go to the next one
				if (Blockchain.isInBlockChain(block)){
					System.out.println(block.getABlockID() + " is in Blockchain. No need to verify.");
					continue;
				}

				// Retrieve signed block ID for decoding and regular block ID for verification
				String uuid = block.getABlockID();
				String signedID = block.getSignedBlockID();
				byte[] decodedID = Base64.getDecoder().decode(signedID);
				String creatingProcess = block.getACreatingProcess();	// Store the block's creating process
				PublicKey pubKey = procPubKey.get(creatingProcess);	// use creating process to retrieve public key

				// Decode the signed and regular SHA256 string in the block to verify the signatures
				String stringSHA256 = block.getASHA256String();
				byte[] sha256Bytes = Base64.getDecoder().decode(stringSHA256);
				String signedSHA256 = block.getASignedSHA256();
				byte[] signedSHA256Bytes = Base64.getDecoder().decode(signedSHA256);

				// Check to see if the signature is correct with the public key
				if (!Blockchain.verifySig(uuid.getBytes(),pubKey,decodedID)) throw new Exception("Signed UUID does not match with UUID.");

				if (!Blockchain.verifySig(sha256Bytes,pubKey,signedSHA256Bytes)) throw new Exception("Signed SHA256 does not match with SHA256 string");

				// Boolean value for checking if all the work has been completed;
				boolean workComplete = false;
				// Keep looping until all the work is complete
				while (!workComplete){
					Thread.sleep(100);	// Sleep to extend time for work
					String randomSeed = Blockchain.randomAlphaNumeric(8);
					block.setSeed(randomSeed);	// Set the random string to the block's Seed

					// Marshal to XML the seed of the block for combination with previous hash
					String xmlString = Blockchain.marshalToXML(block);
					xmlString = xmlString.substring(xmlString.indexOf("<seed>"));

					// Store the previous SHA256 string from the copy of the block chain to create a new hash
					String prevSHA256 = blockChainCopy.getLast().getASHA256String();
					String combinedHash = prevSHA256 + xmlString;	// Combine the XML string and previous hash

					// Use combined hash for creating work
					MessageDigest MD = MessageDigest.getInstance("SHA-256");
					MD.update(combinedHash.getBytes());
					byte[] bytesHash = MD.digest();
					String hexString  = DatatypeConverter.printHexBinary(bytesHash);
					int workNum = Integer.parseInt(hexString.substring(0,4),16);

					// Checking to see if a solution was found
					if (workNum < 20000){
						System.out.println("Puzzle has been solved.");
						workComplete = true;
						// Create new Hash values since we found the solution
						String newHash = Base64.getEncoder().encodeToString(bytesHash);
						byte[] signedData = Blockchain.signData(bytesHash,privateKey);	// Sign data with private key
						String newSignedHash = Base64.getEncoder().encodeToString(signedData);

						// set the new hash values to the block
						block.setASHA256String(newHash);
						block.setASignedSHA256(newSignedHash);

						// Check to see if the block chain has been modified during this process of verifying blocks
						if (blockChainCopy.size() != Blockchain.BlockChain.size()){
							if (!Blockchain.isInBlockChain(block)){
								System.out.println("Modified BlockChain does not contain the block. Resetting verification...");
								
								// Create new copy of block chain to verify again
								workComplete = false;
								blockChainCopy = new LinkedList<BlockRecord>();
								for (BlockRecord b : Blockchain.BlockChain)
									blockChainCopy.add(b);

								blockNum = blockChainCopy.size() + 1;	// Update block number with new block number of new list copy
							}
						}
						else {
							// Add newly verified block to the block chain
							block.setAVerificationProcessID("Process "+Integer.toString(Blockchain.PID));
							block.setBlockNum(blockNum);	// Update the block number for the block
							Blockchain.BlockChain.add(block);
							System.out.println("Added " + block.getABlockID() + " to the Block Chain");

							// Sending out the updated blockchain to all processes
							for (int i = 0; i < Blockchain.totalNumProcesses; i++){
								Socket sock = new Socket("localhost",Blockchain.BlockchainServerPortBase + i);
								ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
								
								// Put the Block chain into a block record group
								BlockRecordGroup brg = new BlockRecordGroup();
								brg.setBlockRecordGroup(Blockchain.BlockChain);

								// Marshal the updated block chain so we can send the objects to all the processes
								JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecordGroup.class);
								Marshaller marshaller = jaxbContext.createMarshaller();
								StringWriter writer = new StringWriter();
								marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
								marshaller.marshal(brg,writer);
								String toProcesses = writer.toString();
								out.writeObject(toProcesses);	// Write out updated blockchain to all processes
								out.flush();
								out.close();
							}
						} 
					}
				}
			}
		}
		catch (Exception e){e.printStackTrace();}
	}
}