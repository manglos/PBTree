/*
 * Christopher Manglos - CSC 365
 * Internal PBTree Assignment - ADD and REMOVE
 * Dec. 14, 2011
 * class Cache
 */

import java.io.*;
import java.util.*;

public class Cache implements Serializable{
    
    int cacheSize;
    transient RandomAccessFile file = null;
    int blocksize;
    PBTNode[] nodes;
    boolean[] dirty; 
    int[] index;
    ArrayList<Integer> emptyBlocks = new ArrayList<Integer>();
	
    Cache(File f, String mode, int b, int c) throws FileNotFoundException{

	file = new RandomAccessFile(f, mode);
	blocksize = b;
	cacheSize = c;
	nodes = new PBTNode[cacheSize];
	dirty = new boolean[cacheSize];
	for(int i=0;i<cacheSize;i++)
	    dirty[i]=false;
	index = new int[cacheSize];
	for(int i=0;i<cacheSize;i++)
	    index[i]=-1;
		
    }
    //ISFULL-----------------------------------------------------------------------------------------------
    boolean isFull(){
	for(int i=0;i<cacheSize;i++){
	    if(nodes[i]==null || !dirty[i])
		return false;
	}
	return true;
    }
    //ADDTOCACHE-------------------------------------------------------------------------------------------
    boolean addToCache(PBTNode x, boolean d){
	if(!isFull()){
	    for(int i=0;i<nodes.length;i++){
		if(nodes[i]!=null && nodes[i].getBlockNum()==x.getBlockNum()){
		    nodes[i]=x;
		    dirty[i]=true;
		    index[i]=x.getBlockNum();
		    return true;
		}
		else if(nodes[i]==null || !dirty[i]){
		    nodes[i]=x;
		    index[i]=x.getBlockNum();
		    dirty[i]=d;
		    return true;
		}
	    }
	}
	else{
	    dump();
	    addToCache(x, d);
	}
	return true;
    }
    //CONTAINS-------------------------------------------------------------------------------------------
    boolean contains(int b){
	for(int i=0;i<cacheSize;i++)
	    if(nodes[i]!=null && index[i]==b)
		return true;
		
	return false;
    }
    void dump(){
	for(int i = 0;i<cacheSize;i++){
	    if(nodes[i]!=null){
		put(nodes[i], nodes[i].getBlockNum());
		nodes[i]=null;
		dirty[i]=false;
		index[i]=-1;
	    }
	}
    }
    //FIRSTEMPTY--------------------------------------------------------------------------------
    boolean firstEmpty(PBTNode x, PBTNode p){
		
	if(emptyBlocks.size()>0 && p!=null){
			
	    int b = emptyBlocks.get(0);
			
	    int index;
	    if(!p.isEmpty()){
		index = p.getIndex(x.getBlockNum());
	    }
	    else		
		index=0;
			
	    x.setBlockNum(b);
	    p.links[index]=b;
	    emptyBlocks.remove(0);
	    return true;
	}
	return false;
    }
    //DELBLOCK--------------------------------------------------------------------------
    boolean delBlock(int off){
	if(off==0)
	    return false;

	emptyBlocks.add(off);
	PBTNode x = new PBTNode(off);
		
	addToCache(x, true);
		
	return true;
    }
    //GET-----------------------------------------------------------
    PBTNode get(int off) throws IOException{
	if(off==-1)
	    return null;

	byte[] block = new byte[((2*PBTree.M)-1)*blocksize];
	PBTNode x = new PBTNode();

	if(contains(off)){
	    for(int i=0;i<cacheSize;i++)
		if(index[i]==off){
		    dirty[i]=true;
		    return nodes[i];
		}
	}
			
	file.seek(off*blocksize*((2*PBTree.M)-1));
		
	if(file.read(block)!=-1){
	    String tmp = new String(block);
	    String[] v = tmp.split(" ");
	    ArrayList<String> val = new ArrayList<String>();

	    for(int i=0; i<v.length;i++)
		if(!v[i].equals(""))
		    val.add(v[i]);

	    int y=0;
	    
	    for(int i=0;i<val.size()-PBTree.M;i++){
		x.add(val.get(i));
	    }
	    for(int i=val.size()-PBTree.M;i<val.size();i++){
		try{x.links[i-(val.size()-PBTree.M)]=Integer.parseInt(val.get(i));}catch(Exception e){}
	    }
	    
	    x.setBlockNum(off);
	    addToCache(x, false);
	    return x;
	}
	return null;
    }
    //PUT------------------------------------------------------------
    boolean put(PBTNode x, int off){
	int padLength;
	String padding;
	byte[] block = new byte[((2*PBTree.M)-1)*blocksize];
		
	for(int i = 0; i<PBTree.M-1;i++){
	    String tmp;
	    if(x.keys[i]==null)
		tmp = "";
	    else
		tmp = x.keys[i];
			
	    padLength = blocksize - tmp.length();
	    padding="";

	    for(int p = 0; p<padLength;p++)
		padding+=" ";
	
	    tmp+=padding;

	    byte[] tmpByte = tmp.getBytes();
	    for(int y = 0; y<blocksize;y++)
		block[(i*blocksize)+y] = tmpByte[y];
	}
	for(int i = PBTree.M-1; i<((2*PBTree.M)-1);i++){
	    String tmp = x.links[i-(PBTree.M-1)]+"";
			
	    padLength = blocksize - tmp.length();
	    padding="";

	    for(int p = 0; p<padLength;p++)
		padding+=" ";
	
	    tmp+=padding;

	    byte[] tmpByte = tmp.getBytes();
	    for(int y = 0; y<blocksize;y++)
		block[(i*blocksize)+y] = tmpByte[y];
	}
	try{
	    file.seek(off*blocksize*((2*PBTree.M)-1));
	    file.write(block);
	}catch(Exception e){return false;}
	return true;
    }
    public String toString(){
	String result="";
	for(int i=0;i<cacheSize;i++)
	    if(nodes[i]!=null)
		result+="\nIndex " + i + ": " + nodes[i].toString()+" at B# " +nodes[i].getBlockNum()+" Dirty?: " + dirty[i];
			
	return result;
    }
}