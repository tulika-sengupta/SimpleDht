package edu.buffalo.cse.cse486586.simpledht;

import android.database.MatrixCursor;

import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Tulika on 10-04-2017.
 */

public class packetInfo implements Serializable {
    private static final long serialVersionUID = 789456345456678789L;
    public String requestType;
    public String masterNode;    //required when nodes join the ring
    public String myPortId;
    public String myPortAdd;
    public NodeInsertion cll;    //circular linked list
    public NodeInsertion currElement;
    public HashMap<String,String> globalStorage = new HashMap<String, String>();
    int counter;
    char selection;
    public String queryingNode;
    public String keyToInsert;
    public String valueToInsert;
    public String valueFound; //value found after querying. //lookup
    String keyToFind;
    String valueFoundatNode;

    //joining
    packetInfo(String requestType, String masterNode, String myPortId, String myPortAdd){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortId = myPortId;
        this.myPortAdd = myPortAdd;
    }

    //broadcasting and For Delete
    packetInfo(String requestType, String masterNode, NodeInsertion cll){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortAdd = masterNode;

        //this.myPortAdd = myPortAdd;
        this.cll = cll;
    }

    //querying all
    packetInfo(String requestType, String masterNode, NodeInsertion cll,HashMap<String,String> globalStorage, int counter, char selection){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortAdd = masterNode;
        this.globalStorage = globalStorage;
        //this.myPortId = myPortId;
        this.cll = cll;
        this.counter = counter;
        this.selection = selection;
    }

    packetInfo(String requestType, String masterNode, NodeInsertion cll,HashMap<String,String> globalStorage, int counter){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortAdd = masterNode;
        this.globalStorage = globalStorage;
        //this.myPortId = myPortId;
        this.cll = cll;
        this.counter = counter;

    }

    packetInfo(String requestType, String masterNode, String queryingNode, NodeInsertion cll,HashMap<String,String> globalStorage, int counter){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortAdd = masterNode;
        this.queryingNode = queryingNode;
        this.globalStorage = globalStorage;
        //this.myPortId = myPortId;
        this.cll = cll;
        this.counter = counter;

    }

    //Specific Query Message Type
    packetInfo(String requestType, String masterNode, String queryingNode, NodeInsertion cll, String key, String valueFound, String valueFoundatNode){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortAdd = masterNode;
        this.queryingNode = queryingNode;
        this.globalStorage = globalStorage;
        this.cll = cll;
        this.valueFound = valueFound;
        this.keyToFind = key;
        this.valueFoundatNode = valueFoundatNode;
    }

//    packetInfo(String requestType, String masterNode, String queryingNode, NodeInsertion cll, String key, String valueFound, String valueFoundatNode, SimpleDhtProvider sdh){
//        this.requestType = requestType;
//        this.masterNode = masterNode;
//        this. myPortAdd = masterNode;
//        this.queryingNode = queryingNode;
//        this.globalStorage = globalStorage;
//        this.cll = cll;
//        this.valueFound = valueFound;
//        this.keyToFind = key;
//        this.valueFoundatNode = valueFoundatNode;
//        this.sdh = sdh;
//    }

    //Insert
    //"Insert",pAdd,n, key, values
    packetInfo(String requestType, String masterNode, NodeInsertion currElement, String keyToInsert, String valueToInsert ){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this.myPortAdd = masterNode;
        this.keyToInsert = keyToInsert;
        this.valueToInsert = valueToInsert;
        this.currElement = currElement;
    }

    packetInfo(String requestType, String masterNode){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortAdd = masterNode;
        //this.myPortAdd = myPortAdd;
        this.cll = cll;
    }

    // Join Acknowledgement
    packetInfo(String requestType, String masterNode, String myPortAdd){
        this.requestType = requestType;
        this.masterNode = masterNode;
        this. myPortId = myPortAdd;
        //this.myPortAdd = myPortAdd;
        this.cll = cll;
    }


    //Delete
    //"DeleteAll",fNode.getNext(fNode).getNode(fNode.getNext(fNode)),fNode.getNext(fNode)
//    packetInfo(String requestType, String masterNode,NodeInsertion ni){
//        this.requestType = requestType;
//        this.masterNode = masterNode;
//        this. myPortAdd = masterNode;
//        this.globalStorage = globalStorage;
//        //this.myPortId = myPortId;
//        this.cll = cll;
//        this.counter = counter;
//
//    }
}
