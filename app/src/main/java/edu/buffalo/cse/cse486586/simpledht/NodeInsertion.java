package edu.buffalo.cse.cse486586.simpledht;

import android.nfc.Tag;
import android.util.Log;

import org.w3c.dom.Node;

import java.io.Serializable;

//
///**
// * Created by Tulika on 09-04-2017.
// */
//
//
//
public class NodeInsertion implements Serializable{

    public NodeInsertion start=null, end=null;
    public int size;

    int data;
    String node;
    String hashNode;
    NodeInsertion prev;
    NodeInsertion next;

    NodeInsertion(){

    }
    NodeInsertion(int data, NodeInsertion prev, NodeInsertion next){
        this.data= data;
        this.prev=prev;
        this.next=next;
    }
    NodeInsertion(String node, String hashNode, NodeInsertion prev, NodeInsertion next){
        this.node = node;
        this.hashNode = hashNode;
        //this.data= data;
        this.prev=prev;
        this.next=next;
    }

    //Appyling the getters and setters
    public void setData(int data){
        this.data = data;
    }
    public void setNode(String node){
        this.node = node;
    }
    public void setHashNode(String hashNode){
        this.hashNode = hashNode;
    }
    public void setNext(NodeInsertion next){
        this.next = next;
    }
    public void setPrev(NodeInsertion prev){
        this.prev = prev;
    }
    public int getData(NodeInsertion n){
        return n.data;
    }
    public NodeInsertion getNext(NodeInsertion n){
        return n.next;
    }
    public NodeInsertion getPrev(NodeInsertion n){
        return n.prev;
    }
    public String getNode(NodeInsertion n){
        return n.node;
    }
    public String getHashNode(NodeInsertion n){
        return n.hashNode;
    }

    public int getSize(NodeInsertion n){
        return n.size;
    }
    public void setSize(int s){
        this.size = s;
    }
    public void testSuccPred(NodeInsertion n){
        int s = size;
        int i =0;
        System.out.println("SIZE IS: " + s);
        while(i<s){
            System.out.println("CURR: "+ n.getNode(n) + " NEXT: " + n.getNext(n).getNode(n.getNext(n)) + " PREV: " + n.getPrev(n).getNode(n.getPrev(n)) );
            //System.out.println("HASH CURR: "+ n.getNode(n) + " NEXT: " + n.getNext(n).getNode(n.getNext(n)) + " PREV: " + n.getPrev(n).getNode(n.getPrev(n)) );
            n = n.getNext(n);
            i++;
        }

//        System.out.print(n.getNext(n));
//        System.out.print(n.getPrev(n));
    }
    public void insert(String node, String hashNode){
        NodeInsertion n = new NodeInsertion(node,hashNode,null,null);

        //Log.v("Inserting in LinkedList","Node: "+data);
        if(start == null){
            start = n;
            end = n ;
            n.setNext(n);
            n.setPrev(n);
        }

        if(hashNode.compareTo(start.getHashNode(start)) <= 0){
            n.setNext(start);
            n.setPrev(end);
            start.setPrev(n);
            end.setNext(n);
            start = n;
        }
        else if(hashNode.compareTo(end.getHashNode(end)) >= 0){
            end.setNext(n);
            n.setPrev(end);
            n.setNext(start);
            start.setPrev(n);
            end=n;
        }

        else {
            NodeInsertion prev=start,next=start.getNext(start);
            while(hashNode.compareTo(end.getHashNode(end))<=0){

                if(hashNode.compareTo(prev.getHashNode(prev))>=0 && hashNode.compareTo(next.getHashNode(next))<=0){
                    prev.setNext(n);
                    n.setPrev(prev);
                    n.setNext(next);
                    next.setPrev(n);
                    break;
                }

                else{
                    prev = next;
                    next = next.getNext(next);
                }
            }
        }
        size++;
    }

    public void displayNodes(){
        NodeInsertion temp = start;
        int i =0;
        if(temp!=null){
            System.out.println(i + " " + temp.getHashNode(temp) + " " + temp.getNode(temp));
            temp=temp.getNext(temp);
            i++;
        }
        while(temp!=start){
            System.out.println(i + " " + temp.getHashNode(temp) + " " + temp.getNode(temp));
            temp = temp.getNext(temp);
            i++;
        }
    }

    public NodeInsertion findNode(String nodeId){
        NodeInsertion temp = start;
        int s=0;
        //Log.v("Checking size: ","size is: "+size);
        //Log.v("Checking start: ","Start node is: "+temp.getNode(temp));
        while(s<size){
            //Log.v(s+"Searching: ","Node: "+temp.getNode(temp));
            if(temp.getNode(temp).equals(nodeId)){
                return temp;
            }
            else{
                temp = temp.getNext(temp);
            }
            s++;
        }
        return null;

    }

    public void insert(int data){
        NodeInsertion n = new NodeInsertion(data,null,null);
        if(start == null){
            start = n;
            end = n ;
            n.setNext(n);
            n.setPrev(n);
        }

        if(data<=start.getData(start)){
            n.setNext(start);
            n.setPrev(end);
            start.setPrev(n);
            end.setNext(n);
            start = n;
        }

        else if(data>=end.getData(end)){
            end.setNext(n);
            n.setPrev(end);
            n.setNext(start);
            start.setPrev(n);
            end=n;
        }

        else {
            NodeInsertion prev=start,next=start.getNext(start);
            while(data<=end.getData(end)){
                if(data>=prev.getData(prev) && data<= next.getData(next)){
                    prev.setNext(n);
                    n.setPrev(prev);
                    n.setNext(next);
                    next.setPrev(prev);
                    break;
                }
                else{
                    prev = next;
                    next = next.getNext(next);
                }
            }
        }
    }

    public void display(){
        NodeInsertion temp = start;
        if(temp!=null){
            System.out.println(temp.getData(temp));
            temp=temp.getNext(temp);
        }
        while(temp!=start){
            System.out.println(temp.getData(temp));
            temp = temp.getNext(temp);
        }
    }

}
