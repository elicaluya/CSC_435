/*--------------------------------------------------------

1. Elijah Caluya / Date: 11/3/2019

2. Java version used, if not the official version for the class:

e.g. build 1.8.0_222-b10

3. Precise command-line compilation examples / instructions:

e.g.:

> javac Blockchain.java


4. Precise examples / instructions to run this program:

e.g.:

> 

5. List of files needed for running the program.

e.g.:

1) Blockchain.java


5. Notes:

e.g.:

----------------------------------------------------------*/

// From BlockInputE.java
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


// From WorkB.java
import javax.xml.bind.DatatypeConverter;


// From BlockI.java
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.*;


import java.util.*;
import java.text.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;



/******************************************************************************************************************************************************************/
// Main class for Blockchain
public class Blockchain {

	public static int KeyServerPortBase = 4710;
  	public static int UnverifiedBlockServerPortBase = 4820;
  	public static int BlockchainServerPortBase = 4930;

  	// Need private key to sign
  	private static PrivateKey privateKey;
  	// Linked List to implement the BlockChain. Referenced from https://www.javatpoint.com/java-linkedlist
  	public static LinkedList<BlockRecord> BlockChain;
  	// Use Blocking Queue for the Univerified Blocks. Referenced from https://www.geeksforgeeks.org/blockingqueue-interface-in-java/
  	public static BlockingQueue<BlockRecord> UnverifiedBlockQueue;


  	private static String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  	public static int totalNumProcesses = 3;

  	public static Boolean runSystem = false;

	public static void main(String args[]) throws Exception {

		int pnum;

		if (args.length < 1) pnum = 0;
    	else if (args[0].equals("0")) pnum = 0;
    	else if (args[0].equals("1")) pnum = 1;
    	else if (args[0].equals("2")) pnum = 2;
    	else pnum = 0; 	// If the process number provided is not 0-2, then default to 0


	}
}


/******************************************************************************************************************************************************************/
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


// Method to compare TimeCreated of so the BlockRecord with the earliest timestap is pulled from the queue first.
class CompareBlockRecord implements Comparator<BlockRecord> {
	public int compare(BlockRecord first, BlockRecord second){
		return first.getTimeCreated().compareTo(second.getTimeCreated());
	}
}



/******************************************************************************************************************************************************************/
// Class for Public Key record for matching the Process ID with its public key. Can be marshalled and unmarshalled with JAXB annotation.
@XmlRootElement
class PublicKeyBlockRecord{
	String PubKey;
	int PID;

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
	HashMap<Integer,PublicKey> procPubKey;
	// Use of java.util.concurrent.CountDownLatch is referenced from: https://www.geeksforgeeks.org/countdownlatch-in-java/
	CountDownLatch latch;


	public PublicKeyServer(int p, HashMap<Integer,PublicKey> procPK, CountDownLatch l){
		this.port = p;
		this.procPubKey = procPK;
		this.latch = l;
	}


	public void run() {
		try {
			// Start up the server to listen to client connections with public key records
			System.out.println("Starting Public Key Server input thread using " + Integer.toString(port));
			sock = new ServerSocket(port,6);

			while (true) {
				Socket socket = sock.accept();		
				new PublicKeyWorker(socket, procPubKey,latch).start();	// Start worker thread when client connects
			}
		} 
		catch (Exception e){e.printStackTrace();}
	}



	// PublicKeyWorker class referenced from Prof. Elliott's starter files bc.java and BlockI.java
	// Public Key Worker class that sends public key record with request from client
	class PublicKeyWorker extends Thread {
		Socket sock;
		HashMap<Integer,PublicKey> procPubKey;
		CountDownLatch latch;

		// Use of ObjectInputStream from: http://tutorials.jenkov.com/java-io/objectinputstream.html
		ObjectInputStream in;
		String xmlPubKey = "";

		public PublicKeyWorker(Socket s, HashMap<Integer,PublicKey> procPK, CountDownLatch l){
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

				procPubKey.put(pid,publicKey);	// Add the process ID and public key to the hashmap

				if (procPubKey.size() == Blockchain.totalNumProcesses)
					latch.countDown();
			}
			catch (Exception e) {e.printStackTrace();}
		}
	}

}



/******************************************************************************************************************************************************************/
// Referenced from Prof. Elliott's starter files bc.java and BlockI.java
// Class for Unverified blocks in processing queue
class UnverifiedBlockServer implements Runnable {
	
	ServerSocket sock;
	int port;
	HashMap<Integer,PublicKey> procPubKey;


	public UnverifiedBlockServer(int p){this.port = p;}

	public void run() {
		try {
			// Start up the server to listen 
			System.out.println("Starting Unverified Block Server thread using " + Integer.toString(port));
			sock = new ServerSocket(port,6);
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

				byte[] decodeInput = Base64.getDecoder().decode(inputString.getBytes());	// Decode input string into bytes
				xmlInput = new String(decodeInput);

				// Unmarshal into BlockRecord object
				JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				StringReader reader = new StringReader(xmlInput);
				BlockRecord br = (BlockRecord) unmarshaller.unmarshal(reader);

				System.out.println("Retrieved block record for unverified block created by " + br.getACreatingProcess());

				Blockchain.UnverifiedBlockQueue.put(br);

				System.out.println("Unverified block record with id: " + br.getABlockID() + " has been put inside the queue");
				in.close();
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
				// Once server accepts client connection, run the system if it is not already running for process 0 and 1
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

		public BlockChainWorker(Socket s){this.sock = s;}
	

		public void run(){
			try {

			}
			catch (Exception e){e.printStackTrace();}
		}

	}
}