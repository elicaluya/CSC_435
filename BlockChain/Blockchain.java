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


import java.io.*;
import java.util.*;
import java.text.*;


// Main class for Blockchain
public class Blockchain {
	public static void main(String args[]) throws Exception {

	}
}


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
	String BlockNum;
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

	public String getBlockNum(){return this.BlockNum;}
	@XmlElement
		public void setBlockNum(String BN){this.BlockNum = BN;}

	public String getSeed(){return this.Seed;}
	@XmlElement
		public void setSeed(String S){this.Seed = S;}

	public String getTimeCreated(){return this.TimeCreated;}
	@XmlElement
		public void setTimeCreated(String TC){this.TimeCreated = TC;}
}