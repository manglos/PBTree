/*
 * Christopher Manglos - CSC 365
 * Internal PBTree Assignment - ADD and REMOVE
 * Dec. 14, 2011
 * class PBTree
 */

import java.util.*;
import java.io.*;

public class PBTree implements Serializable{
	
    PBTNode root, rSplit;
    public int count = 0;
    static int blockCount = 0;
    static int M;
    int min;
    int order;
    Cache c=null;
	
    PBTree(int o){
	root=null;
	PBTree.M = o;
	min=M/2;		
    }
    PBTree(PBTHeader h){
	PBTree.M = h.order;
	min=M/2;
	count = h.getCount();
	blockCount = h.getBlockCount();
	c=h.getCache();
	c.emptyBlocks = h.getEmptyBlocks();
    }
    //LOADHEADER-----------------------------------------------------------------------------
    void loadHeader(PBTHeader h){
	PBTree.M = h.order;
	min=M/2;
	count = h.getCount();
	blockCount = h.getBlockCount();
	c.emptyBlocks = h.getEmptyBlocks();
    }
    //ADD-----------------------------------------------------------------------------------
    boolean add(String s){
	if(root==null){
	    root=new PBTNode(0);
	    count++;
	    blockCount++;
	    root.add(s);
	    c.addToCache(root, true);
	    return true;
	}
	else if(insert(s, root)){
	    count++;
	    return true;
	}
	return false;
    }
    //INSERT--------------------------------------------------------------------------------
    boolean insert(String s, PBTNode x){
		
	if(x.contains(s))
	    return false;
	
		
	if(!x.isFull() && x.isLeaf()){
	    x.add(s);
	    c.addToCache(x, true);
	    return true;
	}
	else if(x.isFull() && x==root){

	    root = new PBTNode(0);
	    x.setBlockNum(blockCount++);
	    String tmp = split(x);

	    root.add(tmp);
			
	    root.links[0] = x.getBlockNum();
	    root.links[1] = rSplit.getBlockNum();
			
	    c.firstEmpty(x, root);
	    c.firstEmpty(rSplit, root);
	    c.addToCache(root, true);
	    c.addToCache(x, true);
	    c.addToCache(rSplit, true);
			
			
	    if(s.compareTo(tmp)<0)
		return insert(s, x);
	    else if(s.compareTo(tmp)>0)
		return insert(s, rSplit);
				
	    return false;
	}
	else{

	    PBTNode next = null;
	    try{
		next = c.get(x.getLink(s));
	    }catch(Exception e){e.printStackTrace();}

	    if(next.isFull()){

		String tmp = split(next);
		x.add(tmp);
		int index = x.getIndex(tmp);
				
		x.links[index]=next.getBlockNum();
		x.links[index+1]=rSplit.getBlockNum();
				
		c.firstEmpty(next, x);
		c.firstEmpty(rSplit, x);
		c.addToCache(x, true);
		c.addToCache(next, true);
		c.addToCache(rSplit, true);
				
		if(s.compareTo(tmp)<0)
		    return insert(s, next);
		if(s.compareTo(tmp)>0)
		    return insert(s, rSplit);
				
		return false;
	    }
	    return insert(s, next);
	}
    }
    //SPLIT------------------------------------------------------------
    String split(PBTNode x){
	rSplit = new PBTNode(blockCount++);
	int mid = M/2 - 1;
	String tmp = x.keys[mid];
		
	for(int i=mid+1; i<M-1;i++){
	    rSplit.add(x.keys[i]);
	    x.keys[i] = null;
	}
		
	for(int i=mid+1; i<M;i++){
	    rSplit.links[(i-(mid+1))] = x.links[i];
	    x.links[i]=-1;
	}
		
	x.keys[mid] = null;	
		
	return tmp;
    }
    //REMOVE-----------------------------------------------------------
    boolean remove(String s){
	if(remove(s, root, null)){
	    count--;
	    return true;
	}
	else
	    return false;
    }
    boolean remove(String s, PBTNode x, PBTNode p){

	String tmp = null;
	
	if(x==null || x.keys[0]==null)
	    return false;
	if(x!=null)
	    stealOrMerge(x,p);
	
	PBTNode next = null;
	
	try{
	    next = c.get(x.getLink(s));
	}catch(Exception e){}
			
	int index;
	
	if(next!=null)
	    index = x.getIndex(next.getBlockNum());
			
	if(x.contains(s)){
	    if(x.isLeaf()){
		x.del(s);
		c.addToCache(x, true);
	    }
	    else{
		tmp = successor(s, x, null);
				
		if(x.contains(s)&&tmp!=null)
		    x.keys[x.getIndex(s)]=tmp;
	    }
	    if(x.size()>=min-1 || x==root)
		return true;
	}
	else
	    return remove(s, next, x);
		
	return true;
    }
    //STEALORMERGE-------------------------------------------------------
    boolean stealOrMerge(PBTNode x, PBTNode p){
	
	if(x.size()<min && p!=null){
	    if(!steal(x, p)){
			
		int index=p.getIndex(x.getBlockNum());
				
		c.delBlock(x.getBlockNum());
		PBTNode mNode = merge(x,p);
				
		if(index>0)
		    p.links[index-1]=x.getBlockNum();
		else
		    p.links[index]=x.getBlockNum();
				
		if(root.size()==0){
				
		    root=mNode;
		    root.setBlockNum(0); 
		    c.addToCache(root, true);
		    c.delBlock(mNode.getBlockNum());
		    return true;
					
		}
				
		c.firstEmpty(x, p);
		c.addToCache(p, true);
		c.addToCache(mNode, true);
				
		return true;
	    }
	    else{
				
		return true;
	    }
	}
	return false;
    }
    //STEAL---------------------------------------------------------------
    boolean steal(PBTNode x, PBTNode p){
	int index = p.getIndex(x.getBlockNum());
	PBTNode tmp=null;
		
	if(index>0){
	    try{tmp = c.get(p.links[index-1]);}catch(Exception e){}
	}
	else if(index==0){
	    try{tmp = c.get(p.links[index+1]);}catch(Exception e){}
	}
		
	if(index>0 && tmp.size()>=min){

	    String a = tmp.maxKey();
	    int aIndex = tmp.getIndex(a);
	    PBTNode aNode = null;
	    PBTNode bNode = null;
	    try{aNode = c.get(tmp.links[aIndex+1]);
		bNode = c.get(tmp.links[aIndex]);}catch(Exception e){}
	    String b = p.keys[index-1];

	    x.pushKeys(0);
	    x.keys[0]=b;

	    p.keys[index-1]=a;
			
	    tmp.del(a);
			
	    if(!x.isLeaf()){
		x.pushLinks(0);
		x.links[0]=aNode.getBlockNum();
		tmp.links[aIndex]=bNode.getBlockNum();
		tmp.links[aIndex+1]=-1;
	    }
			
	    c.addToCache(tmp, true);
	    c.addToCache(p, true);
	    c.addToCache(x, true);
	    return true;
	}
	else if(index==0 && tmp.size()>=min){

	    String a = tmp.keys[0];
	    PBTNode aNode = null;
	    try{aNode = c.get(tmp.links[0]);}catch(Exception e){}
	    String b = p.keys[index];
			
	    x.add(b);
	    p.keys[index]=a;
			
	    tmp.del(a);
			
	    if(!x.isLeaf())
		x.links[x.getIndex(b)+1]=aNode.getBlockNum();
			
	    c.addToCache(p, true);
	    c.addToCache(x, true);
	    c.addToCache(tmp, true);

	    return true;
	}	
	return false;
    }
    //MERGE---------------------------------------------------------------------
    PBTNode merge(PBTNode x, PBTNode p){
	
	int index =p.getIndex(x.getBlockNum());
		
	PBTNode tmp = null;
	if(index==0){
	    try{tmp = c.get(p.links[1]);}catch(Exception e){}
	}
	else{
	    try{tmp = c.get(p.links[index-1]);}catch(Exception e){}
	}
		
	if(tmp!=null){
	    if(index==0){
		int size = x.size();
		x.keys[size]=p.keys[0];
		size++;
				
		for(int i = 0; i<tmp.size();i++)
		    x.keys[size+i]=tmp.keys[i];
		for(int i = 0; i<=tmp.size();i++)
		    x.links[size+i]=tmp.links[i];
				
		p.del(p.keys[0]);
	    }
	    else{
		x.pushKeys(0);
		x.keys[0]=p.keys[index-1];
		int size = x.size();
				
		for(int i = tmp.size()-1; i>=0;i--){
		    x.pushKeys(0);
		    x.keys[0]=tmp.keys[i];
		}
		for(int i = tmp.size(); i>=0;i--){
		    x.pushLinks(0);
		    x.links[0]=tmp.links[i];
		}
		p.del(p.keys[index-1]);
	    }
	}
		
	c.delBlock(tmp.getBlockNum());
	return x;	
    }
    //SUCCESSOR------------------------------------------------------------------
    String successor(String s, PBTNode x, PBTNode p){

	String tmp = null;
	PBTNode y = null;
	try{y=c.get(x.links[0]);}catch(Exception e){}
		
	if(x.contains(s)){
	    int index = x.getIndex(s);
	    try{y = c.get(x.links[index+1]);}catch(Exception e){}
	    tmp = successor(s, y, x);
	}
	else if(x.links[0]!=-1 && !y.isEmpty())
	    tmp = successor(s, y, x);
	if(tmp==null){
	    tmp = x.keys[0];
	    x.del(tmp);
	}

	stealOrMerge(x, p);
		
	if(x.contains(s)&&tmp!=null)
	    x.keys[x.getIndex(s)]=tmp;
		
	c.addToCache(x, true);
	return tmp;
    }
    public String toString(){
	String result = "";
	result+=root.toString();
	for(int i=0;i<M;i++)
	    result+="\nLink #" + i + "\t" + root.links[i];
			
	return result;
    }
}
