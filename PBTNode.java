/*
 * Christopher Manglos - CSC 365
 * Internal PBTree Assignment - ADD and REMOVE
 * Dec. 14, 2011
 * class PBTNode
 */

import java.util.*;
import java.io.*;

public class PBTNode implements Serializable{

    String[] keys;
    int[] links;
    int blocknum;
	
    PBTNode(){
	keys = new String[PBTree.M-1];
	links = new int[PBTree.M];
	blocknum=-1;
	for(int i=0;i<links.length;i++)
	    links[i]=-1;
    }
    PBTNode(int b){
	keys = new String[PBTree.M-1];
	links = new int[PBTree.M];
	blocknum = b;
	for(int i=0;i<links.length;i++)
	    links[i]=-1;
    }
    boolean add(String s){
	if(s==null)return false;
		
				
	for(int i=0;i<keys.length;i++){
	    if(keys[i]==null){
		keys[i]=s;
		return true;
	    }
	    if(s.compareTo(keys[i])<0){
		pushKeys(i);
		pushLinks(i);
		keys[i]=s;
		return true;
	    }
	}
	return false;
    }
    boolean del(String s){
	if(s!=null && this.contains(s)){
	    int index = getIndex(s);
	    keys[index] = null;
	    pullKeys(index);
	    pullLinks(index);
	    return true;
	}
	return false;
    }	
    void pushKeys(int x){
	for(int i=keys.length;i>=x;i--){
	    if((i+1)<keys.length)
		keys[i+1]=keys[i];
	}
    }
    void pushLinks(int x){
	for(int i=links.length;i>=x;i--){
	    if((i+1)<links.length)
		links[i+1]=links[i];
	}
    }
    void pullKeys(int x){
	for(int i=x;i<keys.length;i++){
	    if((i+1)<keys.length)
		keys[i]=keys[i+1];
	    else
		keys[i]=null;
	}
    }
    void pullLinks(int x){
	for(int i=x;i<links.length;i++){
	    if((i+1)<links.length)
		links[i]=links[i+1];
	    else
		links[i]=-1;
	}
    }
    int getIndex(String s){
	
	for(int i=0;i<keys.length;i++){
	    if(keys[i].equalsIgnoreCase(s))
		return i;
	}
	return -1;
    }
    int getIndex(int b){
	
	for(int i=0;i<links.length;i++){
	    if(links[i]==b)
		return i;
	}
	return -1;
    }
    void setBlockNum(int b){
	blocknum = b;
    }
    int getBlockNum(){
	return blocknum;
    }
    String maxKey(){
	for(int i = keys.length-1; i>=0;i--){
	    if(keys[i]!=null)
		return keys[i];
	}
	return null;
    }
    int getLink(String s){
	int i=0;
	if(keys[keys.length-1]!=null && s.compareTo(keys[keys.length-1])>0){
	    return links[keys.length];
	}
	while(keys[i]!=null && i<keys.length){
	    if(s.compareTo(keys[i])>0)
		i++;
	    else
		return links[i];
	}
	//System.out.println(i);
	return links[i];
    }
    boolean contains(String s){
	for(int i=0;i<keys.length;i++){
	    if(s.equals(keys[i]))
		return true;
	}
	return false;
    }
    boolean isFull(){
	if(size()==keys.length)
	    return true;
	return false;
    }
    boolean isEmpty(){
	if(size()==0)
	    return true;
	return false;
    }
    void setLinks(int[] x){
	for(int i=0;i<links.length;i++)
	    links[i]=x[i];
    }
    boolean isLeaf(){
	for(int i=0;i<links.length;i++){
	    if(links[i]!=-1)
		return false;
	}
	return true;
    }
    int size(){
	int count=0;
	while(keys[count]!=null){
	    count++;
	    if(count==PBTree.M-1)
		return count;
	}
	return count;
    }
    public String toString(){
	String result="";
	for(int i=0;i<keys.length;i++)
	    result+=" "+keys[i];
	for(int i=0;i<links.length;i++)
	    result+=" "+links[i];
			
	return result;
    }
}
	