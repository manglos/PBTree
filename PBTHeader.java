/*
 * Christopher Manglos - CSC 365
 * Internal PBTree Assignment - ADD and REMOVE
 * Dec. 14, 2011
 * class PBTHeader
 */

import java.io.*;
import java.util.*;

class PBTHeader implements Serializable{
	
    int order;
    ArrayList<Integer> emptyBlocks;
    int count, blockCount;
    PBTNode root;
    Cache cache = null;
    File file;
	
    PBTHeader(File f, final int o, int c, int bc, ArrayList<Integer> eb){
	file = f;
	order=o;
	count=c;
	blockCount=bc;
	emptyBlocks=eb;
	try{
	    cache = new Cache(f, "rw", 28, 4);
	}catch(Exception e){}
	root=null;
    }
    ArrayList<Integer> getEmptyBlocks(){
	return emptyBlocks;
    }
    int getBlockCount(){
	return blockCount;
    }
    int getCount(){
	return count;
    }
    void setRoot(PBTNode x){
	root=x;
    }
    Cache getCache(){
	return cache;
    }
}