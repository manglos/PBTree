/*
 * Christopher Manglos - CSC 365
 * Internal PBTree Assignment - ADD and REMOVE
 * Dec. 14, 2011
 * class SimpleService
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class SimpleService {
    static final int PORT = 2358;  // use the 2-digit number you selected in class for "47"
    File hdr = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"myheader.hdr");
    File btr = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"mypbtree.btr");
    File flraf = new File(System.getProperty("user.home")+System.getProperty("file.separator")+"myfile.flraf");
    FileOutputStream fos = null;
    ObjectOutputStream oos = null;
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    PBTree tree;
    PBTHeader header;
	 
    SimpleService(){
	tree=null;
	header=null;
	createFiles();
	run();
	save();
    }
	 
    public static void main(String[] args) {
	new SimpleService();
    }
    String print(){
	
	return "<br><h4>--------------------------------ROOT----------------------------------<br><font color=\"FFFFFF\">"+tree.root+"</font><br>--------------------------------ROOT----------------------------------</h4><br>"+"<br>BLOCKCOUNT = <font color=\"FFFFFF\">" + tree.blockCount + "</font><br><br>";
	
    }
    public void createFiles(){
	try{
            fis = new FileInputStream(hdr);
            ois = new ObjectInputStream(fis);
	    
            header = (PBTHeader)ois.readObject();
				
	}catch(Exception e){
	    System.out.println("Header not found");
	    header = new PBTHeader(flraf, 8, 0, 0, new ArrayList<Integer>());
	}
	try{
            fis = new FileInputStream(btr);
            ois = new ObjectInputStream(fis);
			
            tree = (PBTree)ois.readObject();
	    try{
		tree.c = new Cache(header.file, "rw", 28, 4);
	    }catch(Exception e){}

	    tree.loadHeader(header);
	}catch(Exception e){
		
	    System.out.println(e.getMessage() + "\nTree not found, making new...");
	    tree = new PBTree(header);
	    try{
		tree.c = new Cache(header.file, "rw", 28, 4);
	    }catch(Exception xe){}
	    scanWords();
	}		
    }
    void run(){
	try {
	    ServerSocket serverSocket = new ServerSocket(PORT);
      
	    for (;;) {
		Socket client = serverSocket.accept();
	     
		PrintWriter out = new PrintWriter(client.getOutputStream(), true);
		BufferedReader in =  
		    new BufferedReader(new InputStreamReader(client.getInputStream()));
	
		String cmd = in.readLine();  
		String input = "";

		input = cmd.substring(cmd.lastIndexOf("GET /")+5, cmd.lastIndexOf(" HTTP/"));
			  
		String reply = "<html>\n" +
		    "<head><title>Christopher Manglos - Persistent Dictionary</title></head>\n" + 
		    "<body bgcolor=\"black\"><h1><font color=\"4594CC\">My Persistent Dictionary</font></h1></body><br><font color=\"00E1FF\"><h2>"; 
		    
	          
		if(input.indexOf("-")==0){
		    if(tree.remove(input.substring(1)))
			reply+="<br>\"<font color=\"FF003F\">"+input.substring(1)+"</font>\" was removed from tree.\n</html>\n";
		    else
			reply+="<br>\"<font color=\"FFFFFF\">"+input.substring(1)+"</font>\" was not found in tree.\n</html>\n";
		}
		else{
		    if(tree.add(input))
			reply+="<br>\"<font color=\"00C8FF\">"+input+"</font>\" was added in tree.\n</html>\n";
		    else
			reply+="<br>\"<font color=\"00FF44\">"+input+"</font>\" is spelled correctly.\n</html>\n";
		}
		reply+="</h2><br><br>"+print()+"<h2>-----------------------------------------------------------CACHE--------------------------------------------------------------</h2><textarea rows=\"4\" cols=\"150\">"+tree.c+"</textarea>";
		save();
		reply+="<br><h2><font color=\"FFFFFF\">"+tree.count+"</font> values in tree.</h2></font>\n";
	        int len = reply.length();
			
	        out.println("HTTP/1.0 200 OK");
	        out.println("Content-Length: " + len);
	        out.println("Content-Type: text/html\n");
	        out.println(reply);
				
	        out.close();
	        in.close();
	        client.close();
	    }
			
			
	}
	catch (IOException ex) {
	    ex.printStackTrace();
	    System.exit(-1);
	}
    }
    void scanWords(){
	try{
  			
	    FileInputStream fstream = new FileInputStream("words.txt");
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    while((strLine = br.readLine()) !=null){
		tree.add(strLine);
	    }
	    in.close();
			
    	}catch (Exception e){
	    System.out.println("Error: " + e.getMessage());
	}
    }
    void save(){
	tree.c.dump();
	header = new PBTHeader(flraf, PBTree.M, tree.count, tree.blockCount, tree.c.emptyBlocks);
	header.setRoot(tree.root);
	try{
	    fos = new FileOutputStream(btr);
	    oos = new ObjectOutputStream(fos);
	    oos.writeObject(tree);
	    oos.close();
	}catch(Exception ex){
	    System.out.println(ex+"Tree could not be saved.");
	}
	try{
	    fos = new FileOutputStream(hdr);
	    oos = new ObjectOutputStream(fos);
	    oos.writeObject(header);
	    oos.close();
	}catch(Exception ex){
	    System.out.println(ex+"Header could not be saved.");
	}
    }
}
