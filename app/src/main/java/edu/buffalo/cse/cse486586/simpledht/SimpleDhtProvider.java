package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Vector;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Node;

public class SimpleDhtProvider extends ContentProvider {

    static final String TAG = SimpleDhtProvider.class.getSimpleName();
    public static final int SERVER_PORT = 10000;
    public static String requestType;
    public static final String entryNode = "11108";
    public static final String REMOTE_PORT[] = {"11112","11116","11120","11124"};
    public static HashMap<String, String> hm = new HashMap<String, String>();   //local storage
    public static HashMap<String, String> globalHm= new HashMap<String, String>(); //global storage
    public static String pId= null;
    public static String pAdd =null;
    //public static int counter =0;
    NodeInsertion n = new NodeInsertion();
    NodeInsertion next;
    MatrixCursor globalMC = new MatrixCursor(new String[]{"key","value"});
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    public static boolean waitStatus;
    public Timer timer;
    public static boolean specQueryWait;
    public static HashMap<String, packetInfo> specQueryMap = new HashMap<String, packetInfo>(); //storing the values found after triggering specific query

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub

        if(selection.compareTo("@")==0){
            Log.v("@ delete selected: "+ pId," HM: "+hm);
            for(String keys: hm.keySet()){
               hm.remove(keys);
            }
        }
        else if(selection.compareTo("*")==0){

            Log.v("* MAINdelete selected: "+ pId," HM: "+hm);
            for(String keys: hm.keySet())
            {
                hm.remove(keys);
            }
            NodeInsertion fNode = n.findNode(pAdd);
            packetInfo pck = new packetInfo("DeleteAll",fNode.getNext(fNode).getNode(fNode.getNext(fNode)),fNode.getNext(fNode));
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
        }
        else{
            try{
                NodeInsertion nCurr = n.findNode(pAdd);
                NodeInsertion nNext = n.getNext(nCurr);
                NodeInsertion nPrev = n.getPrev(nCurr);

                String currNode = nCurr.getNode(nCurr);
                String nextNode = nNext.getNode(nNext);
                String prevNode = nPrev.getNode(nPrev);

                String hashKey = genHash(selection);
                String hashKeyCurrent = nCurr.getHashNode(nCurr);
                String hashNext = nNext.getHashNode(nNext);
                String hashKeyPrev = nPrev.getHashNode(nPrev);

                if(n.getSize(n)==1){
                    Log.v("RET QUERY FOR 1 ELE:","");
                    Log.v("KEY", selection + " FOUND AT NODE: "+ currNode);
                    for(String key: hm.keySet()){
                        if(key.equals(selection))
                        {  hm.remove(key);
                            break;
                        }
                    }
                }

                else if ((hashKeyPrev.compareTo(hashKeyCurrent) >= 0) &&
                        ((hashKey.compareTo(hashKeyPrev) > 0 ) ||
                                ( hashKey.compareTo(hashKeyCurrent) <= 0)))
                {
                    Log.v("1 MAIN: DEL KEY:", selection + " FROM " + " port " + currNode );
                    for(String key: hm.keySet()){
                        if(key.equals(selection))
                        {  hm.remove(key);
                            break;
                        }
                    }
                }
                else if (hashKey.compareTo(hashKeyPrev) > 0 && hashKeyCurrent.compareTo(hashKey) >= 0 )
                {
                    Log.v("2 MAIN: DEL KEY:", selection + " FROM " + " port " + currNode );
                    for(String key: hm.keySet()){
                        if(key.equals(selection))
                        {  hm.remove(key);
                            break;
                        }
                    }
                }
                else{
                    Log.v("3 IN MAIN DEL Key", selection + " FROM PORT  " + currNode + " TO NEXT PORT " + nextNode);
                    NodeInsertion fNode = n.findNode(pAdd);
                    packetInfo pck = new packetInfo("DeleteSpecificKey",fNode.getNext(fNode).getNode(fNode.getNext(fNode)),fNode.getNext(fNode));
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
                }
            }
            catch (Exception e){
            }

        }
        return 0;
    }

    public void deleteAll(packetInfo p){
        for(String keys: hm.keySet())
        {
            hm.remove(keys);
        }
        NodeInsertion temp = p.cll;
        temp = temp.getNext(temp);
        packetInfo pck1 = new packetInfo("DeleteAll",temp.getNode(temp), temp);
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck1);

    }

    public void deleteSpecificQuery(packetInfo p){

        try{
            String selection = p.keyToFind;
            NodeInsertion nCurr = p.cll;
            NodeInsertion nNext = n.getNext(nCurr);
            NodeInsertion nPrev = n.getPrev(nCurr);

            String currNode = nCurr.getNode(nCurr);
            String nextNode = nNext.getNode(nNext);
            String prevNode = nPrev.getNode(nPrev);

            String hashKey = genHash(selection);
            String hashKeyCurrent = nCurr.getHashNode(nCurr);
            String hashNext = nNext.getHashNode(nNext);
            String hashKeyPrev = nPrev.getHashNode(nPrev);

            if ((hashKeyPrev.compareTo(hashKeyCurrent) >= 0) &&
                    ((hashKey.compareTo(hashKeyPrev) > 0 ) ||
                            ( hashKey.compareTo(hashKeyCurrent) <= 0)))
            {
                Log.v("1 MAIN: DEL KEY:", selection + " FROM " + " port " + currNode );
                for(String key: hm.keySet()){
                    if(key.equals(selection))
                    {  hm.remove(key);
                        break;
                    }
                }
            }
            else if (hashKey.compareTo(hashKeyPrev) > 0 && hashKeyCurrent.compareTo(hashKey) >= 0 )
            {
                Log.v("2 MAIN: DEL KEY:", selection + " FROM " + " port " + currNode );
                for(String key: hm.keySet()){
                    if(key.equals(selection))
                    {  hm.remove(key);
                        break;
                    }
                }
            }
            else{
                Log.v("3 IN MAIN DEL Key", selection + " FROM PORT  " + currNode + " TO NEXT PORT " + nextNode);
                packetInfo pck = new packetInfo("DeleteSpecificKey",nextNode,nNext);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
            }
        }
        catch (Exception e){
        }

    }
    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub

        String key = values.get("key").toString();             // can also use values.getAsString();
        String value = values.get("value").toString();
        Log.v("Linked List", "");
        n.displayNodes();
        try{
            Thread.sleep(500);
        }catch(Exception e){
            Log.e("Insert Error: ", "is: "+ e);
        }
        if(n.getSize(n)==0){
            try{
                n.insert(pAdd,genHash(pId));
            }
            catch (Exception e){

            }
        }

        NodeInsertion fNode = n.findNode(pAdd);
        packetInfo pInsert = new packetInfo("Insert",pAdd,fNode,key, value);
        insertValues(pInsert);
        return uri;
    }

    public void insertValues(packetInfo pInsert){

        String key1 = pInsert.keyToInsert;            // can also use values.getAsString();
        String value = pInsert.valueToInsert;
        Log.v("INSERT QUERY:",key1);

        NodeInsertion nodeCurr = pInsert.currElement;
        NodeInsertion nodeNext = nodeCurr.getNext(nodeCurr);
        NodeInsertion nodePrev = nodeCurr.getPrev(nodeCurr);

        String hashKeyCurrent = nodeCurr.getHashNode(nodeCurr);
        String hashKeyNext = nodeNext.getHashNode(nodeNext);
        String hashKeyPrev = nodePrev.getHashNode(nodePrev);

        String hashKey;   //key to insert
        FileOutputStream fo;

        try{
            hashKey = genHash(key1);

            Log.v("Hash Key ", key1 + ": " + hashKey);
            Log.v("H.Succ WHEN KEY IS ", key1 + " " + nodeNext.getNode(nodeNext) + ": " + hashKeyNext);
            Log.v("H.PREV WHEN KEY IS  ", key1 + " " + nodePrev.getNode(nodePrev) + ": " + hashKeyPrev);

            // if ((hashedKey.compareTo(nodeInfo.getMyHashedPortNumber()) <= 0 &&  hashedKey.compareTo(hashedPredecessor) > 0) || (hashedPredecessor.compareTo(nodeInfo.getMyHashedPortNumber()) >= 0 && hashedKey.compareTo(hashedPredecessor) > 0) || (hashedPredecessor.compareTo(nodeInfo.getMyHashedPortNumber()) >= 0 &&  hashedKey.compareTo(nodeInfo.getMyHashedPortNumber()) <= 0))
            Log.v("HASH_VALUES", " :K: " + hashKey + " :C: " + hashKeyCurrent + " :P: " + hashKeyPrev);


            if ((hashKeyPrev.compareTo(hashKeyCurrent) >= 0) &&
                    ((hashKey.compareTo(hashKeyPrev) > 0 ) ||
                            ( hashKey.compareTo(hashKeyCurrent) <= 0)))
            {
                Log.v("INSERTING KEY:", key1 + " in " + " port " + pInsert.masterNode  );
                hm.put(key1,value);
            }
            else if (hashKey.compareTo(hashKeyPrev) > 0 && hashKeyCurrent.compareTo(hashKey) >= 0 )
            {
                Log.v("INSERTING KEY:", key1 + " in " + " port " + pInsert.masterNode );
                hm.put(key1,value);
            }
            else{
                Log.v("FORWARDING KEY NEXT: ", key1 + " FROM PORT " + pInsert.masterNode + " TO PORT " +  nodeNext.getNode(nodeNext) );
                packetInfo pIns = new packetInfo("Insert",nodeNext.getNode(nodeNext), nodeNext,key1, value);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pIns);

            }

            try{
                fo = getContext().openFileOutput(hashKey, Context.MODE_PRIVATE);
                fo.write(value.getBytes());
                fo.close();
            }
            catch(Exception e){
                Log.e("Content Prov Insert:", "Cannot write in the file!!");
            }
        }
        catch(Exception e){

        }
    }
    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length()-4);
        final String myPort = String.valueOf(Integer.parseInt(portStr)*2);
        pId=portStr;
        pAdd = myPort;
        int flag = 0;

        //Log.d("Inside Oncreate():","Instance Running: "+myPort);
        try{
            Log.d("Hash of port: "+myPort, " is: "+ genHash(portStr));}
        catch (Exception e){}


        try{
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch(Exception e){
            Log.e("Sckt Creation Exception","Cant create a socket!");
        }

        packetInfo pck = new packetInfo("Join","11108",portStr, myPort);
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // TODO Auto-generated method stub

        MatrixCursor mc = new MatrixCursor(new String[]{"key","value"});

        if(selection.compareTo("@")==0){
            Log.v("@ selected: "+ pId," HM: "+hm);
            mc = new MatrixCursor(new String[]{"key","value"});
            for(String keys: hm.keySet()){
                String k = keys;
                String v = hm.get(k);
                //Log.v("In provider:", k + " " + v);
                String content[]= {k,v};
                mc.addRow(content);
            }
        }

        else if(selection.compareTo("*")==0){
            Log.v("* selected: "+ pId," HM: "+hm);
            if(n.getSize(n)==1){
                mc = new MatrixCursor(new String[]{"key","value"});
                for(String keys: hm.keySet()){
                    String k = keys;
                    String v = hm.get(k);
                    //Log.v("In provider:", k + " " + v);
                    String content[]= {k,v};
                    mc.addRow(content);
                }
            }
            else{
                int counter =n.getSize(n);
                NodeInsertion fNode = n.findNode(pAdd);
                packetInfo pck = new packetInfo("QueryAll",fNode.getNext(fNode).getNode(fNode.getNext(fNode)),pAdd,fNode.getNext(fNode),globalHm,counter);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck);
                queryWait();

                for(String key: globalHm.keySet()){
                    String content[] = {key, globalHm.get(key)};
                    mc.addRow(content);
                }
                if (mc.moveToFirst()) {
                    do {
                        int keyIndex = mc.getColumnIndex(KEY_FIELD);
                        int valueIndex = mc.getColumnIndex(VALUE_FIELD);

                        String returnKey = mc.getString(keyIndex);
                        String returnValue = mc.getString(valueIndex);
                        Log.v("Cursor Key: "+returnKey,"Cursor Value: "+ returnValue);

                    } while (mc.moveToNext());
                }
                Log.v(TAG, "Checking in query *: " + pAdd + ": "+globalHm);
            }
        }

        else{
            try{

                NodeInsertion nCurr = n.findNode(pAdd);
                NodeInsertion nNext = n.getNext(nCurr);
                NodeInsertion nPrev = n.getPrev(nCurr);

                String currNode = nCurr.getNode(nCurr);
                String nextNode = nNext.getNode(nNext);
                String prevNode = nPrev.getNode(nPrev);

                String hashKey = genHash(selection);
                String hashKeyCurrent = nCurr.getHashNode(nCurr);
                String hashNext = nNext.getHashNode(nNext);
                String hashKeyPrev = nPrev.getHashNode(nPrev);

                if(n.getSize(n)==1){
                    Log.v("RET QUERY FOR 1 ELE:","");
                    Log.v("KEY", selection + " FOUND AT NODE: "+ currNode);
                    for(String key: hm.keySet()){
                        if(key.equals(selection))
                        {   String content[] = {key, hm.get(key)};
                            mc.addRow(content);
                            break;
                        }
                    }
                }

                else if ((hashKeyPrev.compareTo(hashKeyCurrent) >= 0) &&
                        ((hashKey.compareTo(hashKeyPrev) > 0 ) ||
                                ( hashKey.compareTo(hashKeyCurrent) <= 0)))
                {
                    Log.v("1 MAIN: RETURNING KEY:", selection + " FROM " + " port " + currNode );
                    for(String key: hm.keySet()){
                        if(key.equals(selection))
                        {   String content[] = {key, hm.get(key)};
                            mc.addRow(content);
                            break;
                        }
                    }
                }
                else if (hashKey.compareTo(hashKeyPrev) > 0 && hashKeyCurrent.compareTo(hashKey) >= 0 )
                {
                    Log.v("2 MAIN: RETURNING KEY:", selection + " FROM " + " port " + currNode );
                    for(String key: hm.keySet()){
                        if(key.equals(selection))
                        {   String content[] = {key, hm.get(key)};
                            mc.addRow(content);
                            break;
                        }
                    }
                }
                else{
                    Log.v("3 IN MAIN FRD QUERY Key", selection + " FROM PORT  " + currNode + " TO NEXT PORT " + nextNode);
                    specQueryMap.put(selection, null);
                    packetInfo pQuery = new packetInfo("SpecificQuery", nextNode, pAdd, nNext, selection, null, null);   //pADD = querying node here
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pQuery);
                    specQueryWait();
                    packetInfo pp = specQueryMap.get(selection);
                    if(pp.valueFound!=null){
                        String content[] = {selection, pp.valueFound};
                        mc.addRow(content);
                        specQueryMap.remove(selection);
                    }
                    Log.v("3 AFTER WAIT","");
                }
            }
            catch (Exception e){
            }
        }

        if (mc == null) {
            Log.e(TAG, "Cursor Result null");
        }

        if (mc.moveToFirst()) {
            Log.e(TAG, "CURSOR BEFPRE RETURNING FOR PORT!!!" + pAdd + " selection " + selection);
            do {
                int keyIndex = mc.getColumnIndex(KEY_FIELD);
                int valueIndex = mc.getColumnIndex(VALUE_FIELD);

                String returnKey = mc.getString(keyIndex);
                String returnValue = mc.getString(valueIndex);
                Log.v("Cursor Key:----- "+returnKey,"Cursor Value:------ "+ returnValue+ " check infinity:" + selection);

            } while (mc.moveToNext());
        }
        return mc;
    }

    public void queryWait(){
        waitStatus = true;
        while(waitStatus);
    }

    public void specQueryWait(){
        specQueryWait = true;
        while(specQueryWait);
    }
    public packetInfo queryAll(packetInfo p){
        //Log.v(TAG, "Checking CLL in query: " + p.cll.getNode(p.cll));
        int i=0;

        NodeInsertion temp = p.cll;
        HashMap<String,String> h= p.globalStorage;
        String nodeName=null;
        packetInfo pck1= null;
        packetInfo pck2 = null;
        if(p.counter>0){
            h.putAll(hm);
            nodeName= temp.getNode(temp);
            temp = temp.getNext(temp);
            pck1 = new packetInfo("QueryAll",temp.getNode(temp), p.queryingNode, temp,h,--p.counter);
            pck2 = new packetInfo("QueryReply",p.queryingNode, p.queryingNode, temp,h,p.counter);
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck1);
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pck2);
        }
        return  pck1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    public void broadCastJoinInfo(packetInfo p, NodeInsertion ni){
        //Log.v(TAG,"size of LL: "+ n.getSize(n));
        NodeInsertion temp = ni.start;
        int size=0;
        while(size<n.getSize(n)){
            //Log.v(TAG,size+1+ "broadcasting to: " + temp.getNode(temp));
            packetInfo pi = new packetInfo("BroadCast",temp.getNode(temp),ni);
            temp = temp.getNext(temp);
            size++;
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pi);
        }
    }

    public void specificQuery(packetInfo specQuery) {
        //same logic as in queryAll. just the difference- ow we will call pAdd instead of succ;

        try{

            String selection = specQuery.keyToFind;
            NodeInsertion nCurr = specQuery.cll;
            NodeInsertion nNext = n.getNext(nCurr);
            NodeInsertion nPrev = n.getPrev(nCurr);

            String currNode = nCurr.getNode(nCurr);
            String nextNode = nNext.getNode(nNext);
            String prevNode = nPrev.getNode(nPrev);

            String hashKey = genHash(selection);
            String hashKeyCurrent = nCurr.getHashNode(nCurr);
            String hashNext = nNext.getHashNode(nNext);
            String hashKeyPrev = nPrev.getHashNode(nPrev);
            String valToFind;

            if ((hashKeyPrev.compareTo(hashKeyCurrent) >= 0) &&
                    ((hashKey.compareTo(hashKeyPrev) > 0 ) ||
                            ( hashKey.compareTo(hashKeyCurrent) <= 0)))
            {
                Log.v("1IN SPEC QUER FUN-KEY: ", specQuery.keyToFind+ " FOUND AT " + currNode + ". NOW SENDING BACK TO QUERYING NODE-" + specQuery.queryingNode);
                for (String key : hm.keySet()) {
                    if (key.equals(selection)) {
                        valToFind = hm.get(key);
                        packetInfo pQuery = new packetInfo("SpecificQuery",specQuery.queryingNode,specQuery.queryingNode,nCurr,selection,valToFind,currNode);   //sending reply to the querying node
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pQuery);
                        break;
                    }
                }
            }
            else if (hashKey.compareTo(hashKeyPrev) > 0 && hashKeyCurrent.compareTo(hashKey) >= 0 )
            {
                Log.v("2IN SPEC QUER FUN-KEY: ", specQuery.keyToFind+ " FOUND AT " + currNode + ". NOW SENDING BACK TO QUERYING NODE-" + specQuery.queryingNode);
                for (String key : hm.keySet()) {
                    if (key.equals(selection)) {
                        valToFind = hm.get(key);
                        packetInfo pQuery = new packetInfo("SpecificQuery",specQuery.queryingNode,specQuery.queryingNode,nCurr,selection,valToFind,currNode);   //sending reply to the querying node
                        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,pQuery);
                        break;
                    }
                }
            }
            else{
                Log.v("3 IN SPEC QUER FUN Key", selection + " FROM PORT  " + currNode + " TO NEXT PORT " + nextNode);
                specQueryMap.put(selection, null);
                packetInfo pQuery = new packetInfo("SpecificQuery", nextNode, specQuery.queryingNode, nNext, selection, null, null);   //pADD = querying node here
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, pQuery);
            }
        }
        catch (Exception e){
            Log.v("Spec Quer Err: ",""+e);
        }

    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void>{
        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
        @Override
        protected Void doInBackground(ServerSocket ... sockets) {
            ServerSocket serverSocket = sockets[0];
            Socket socket=null;
            Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");

            do {
                try {
                    socket = serverSocket.accept();
                    ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                    packetInfo p = (packetInfo) is.readObject();

                    if(p.requestType.equals("Join")){
                        Log.v(TAG,"Connected!: " + p.requestType + " " + p.masterNode + " " + p.myPortId);
                        String hashNode = genHash(p.myPortId);
                        Log.v("HashNode for:"+ p.myPortAdd , "is:" + hashNode);
                        n.insert(p.myPortAdd,hashNode);
                        n.displayNodes();
                        next=n.start.getNext(n.start);
                        broadCastJoinInfo(p,n);
                    }
                    else if (p.requestType.equals("BroadCast")){
                        n = p.cll;
                        //Log.v(TAG,"CHECKING ACTUAL SIZE "+ p.cll.getSize(p.cll));
                        n.setSize(p.cll.getSize(p.cll));
                        String s = n.start.getNode(n.start);
                        //Log.v(TAG,"Broadcast Reply to node: " + p.myPortAdd + " CLL start node:" + s);
                        //n.displayNodes();
                        Log.v("TESTING SUCC PRED ","");
                        n.testSuccPred(n.start);
                    }
                    else if( p.requestType.equals("QueryAll")){
                        NodeInsertion nextNode = p.cll.getNext(p.cll);
                        queryAll(p);
                        if(p.myPortAdd.equals(p.queryingNode)){
                            globalHm.putAll(p.globalStorage);
                            waitStatus = false;
                        }

                    }
                    else if(p.requestType.equals("QueryReply")){
                        globalHm.putAll(p.globalStorage);
                        //Log.v(TAG, p.counter+ "Checking in Server task all global storage for node: " + p.queryingNode + ": "+globalHm);

                    }
                    else if(p.requestType.equals("Insert")){
                        insertValues(p);
                    }
                    else if (p.requestType.equals("SpecificQuery")) {
                        Log.v("IN SERVER: "," KEY " + p.keyToFind + "CHECKING SPEC QUER:" + " MASTER NODE: " + p.masterNode + " AND QUERYING NODE: " + p.queryingNode);
                        if (p.masterNode.equals(p.queryingNode)) {
                            specQueryMap.put(p.keyToFind,p);
                            specQueryWait = false;
                        }else
                            specificQuery(p);
                    }
                    else if (p.requestType.equals("DeleteAll")){
                        if (!p.masterNode.equals(p.queryingNode)) {
                            deleteAll(p);
                        }
                    }
                    else if (p.requestType.equals("DeleteSpecificKey")){
                        if (!p.masterNode.equals(p.queryingNode)) {
                        deleteSpecificQuery(p);}
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, " Cant accept:" + e);
                }

            }while(true);
        }

        protected void onProgressUpdate(String...strings){
        }
    }

    private class ClientTask extends AsyncTask<packetInfo, Void, Void>{
        @Override
        protected Void doInBackground(packetInfo...pack){
            packetInfo p = pack[0];
            try {
                String m = p.masterNode;
                if(p.requestType.equals("Join"))
                {
                    //Log.d("At Client Side: ","Sending join request to port: "+ m);
                }
                else{
                    //Log.d("At Client Side: ","Sending broadcast request to port: "+ m);
                }

                //timer = new Timer(16000,p);
                //timer.start();


                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(m));
                //socket.setSoTimeout(2000);
                //timer.reset();
                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                os.writeObject(p);
                //timer.stopThread(timer);
            }
            catch (SocketTimeoutException se){
                Log.e("Time out Connection: ", "is: " + se);
            }

            catch(Exception e){

                Log.e("Connection err: ", "is: " + e);

//                try {
//                    Log.d("Client Side in catch: ", "Sending join request to port: " + p.myPortAdd);
//                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(p.myPortAdd));
//
//                    ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
//                    os1.writeObject(p);
//                    //Log.e(TAG, "Cannot connect to remote port");
//                }
//                catch(Exception e1){
//
//                }
            }
            return null;
        }
    }


    public class Timer extends Thread
    {
        /** Rate at which timer is checked */
        protected int m_rate = 100;

        /** Length of timeout */
        private int m_length;

        /** Time elapsed */
        private int m_elapsed;
        Timer t1;
        packetInfo pInfoThread;

        /**
         * Creates a timer of a specified length
         * @param	length	Length of time before timeout occurs
         */
        public Timer ( int length, packetInfo pInfoThread )
        {
            // Assign to member variable
            m_length = length;

            // Set time elapsed
            m_elapsed = 0;
            this.pInfoThread=pInfoThread;
        }


        /** Resets the timer back to zero */
        public synchronized void reset()
        {
            m_elapsed = 0;
        }

        public synchronized void stopThread(Thread t){
            System.err.println ("Stopping!!"+ t);
            if(t!=null){
                timer=null;
            }
            System.err.println ("After Stopping!!"+ timer);
        }
        /** Performs timer specific code */
        public void run()
        {
            System.err.println ("Started!!");
            // Keep looping
            for (;;)
            {
                // Put the timer to sleep
                try
                {
                    Thread.sleep(m_rate);
                }
                catch (InterruptedException ioe)
                {
                    continue;
                }

                // Use 'synchronized' to prevent conflicts
                synchronized ( this )
                {
                    // Increment time remaining
                    m_elapsed += m_rate;

                    // Check to see if the time has been exceeded
                    if (m_elapsed > m_length)
                    {
                        // Trigger a timeout
                        timeout(this);
                        break;
                    }
                }

            }
        }

        // Override this to provide custom functionality
        public void timeout(Timer t)
        {
            try {
                System.err.println("Checking timer in tieout: " + timer);
                //System.err.println ("Network timeout occurred.... terminating");
                if(timer!=null)
                {Log.d("Client Side in thread: ", "Sending join request to port: " + pInfoThread.myPortAdd);
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(pInfoThread.myPortAdd));
                    ObjectOutputStream os1 = new ObjectOutputStream(socket.getOutputStream());
                    os1.writeObject(pInfoThread);
                    //timer=null;
                }

            }
            catch(Exception e){}
        }
    }


}
