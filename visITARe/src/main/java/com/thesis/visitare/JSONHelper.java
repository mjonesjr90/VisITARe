package com.thesis.visitare;

/**
 * The following classes were modified from 
 * http://prativas.wordpress.com/category/android/part-1-retrieving-a-json-file/
 * 
 * convertJsonToStringUsingHttpResponse(String url)
 * convertJsonToStringFromAssetFolder(String fileName,Context context)
 * 
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JSONHelper{
	public final static String TAG = "JSONHelper";
	private static Context mContext;

    //no need to instantiate this
    public JSONHelper(Context context){
    	mContext = context;
    }
    
    public static String convertJsonToStringUsingHttpResponse(String url) throws IOException {
    	Log.d(TAG, "Entering HTTP conversion method");
    	InputStream contentStream;
        StringBuilder builder = new StringBuilder();
        
        HttpClient client = new DefaultHttpClient();
        Log.d(TAG, "Created HTTPClient");
        HttpGet httpGet = new HttpGet(url);
        Log.d(TAG, "Created HTTPGet");
        HttpResponse response = client.execute(httpGet);
        Log.d(TAG, "Created HTTPResponse");
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            // get contents from stream through entity
            contentStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(contentStream));
            String line;
            while ((line = reader.readLine()) != null){
            	builder.append(line);
            }
        } else {
            // throw giveSomeException();
            //Log.e("RemoteDataFetcher", "Error in download");
            Log.d(TAG, "HTTP: "+ builder.toString());
        }
        return builder.toString();
    }
	
    public static String convertJsonToStringFromAssetFolder(String fileName,Context context) throws IOException {
		Log.d(TAG, "Entering file conversion method");
        AssetManager manager = context.getAssets();
        Log.d(TAG, "AssetManager created");
        InputStream file = manager.open(fileName);
        Log.d(TAG, "Input stream opened");
        
        byte[] data = new byte[file.available()];
        file.read(data);
        file.close();
        return new String(data);
    }
    
    /**
     * @param jsonString
     * @return List<UserData>
     * takes a json string and parses it into json objects
     * You must know what is in the data and what to parse out of it
     */
	public static ArrayList<Node> parseAll(String jsonString) {
		String newJSON = null;
		Log.d(TAG, "Created new json string holder: newJSON");
		try {
			Log.d(TAG, "Trying to convert to string");
			if(jsonString.contains("http")){
				newJSON = convertJsonToStringUsingHttpResponse(jsonString);
			}
			else{
				newJSON = convertJsonToStringFromAssetFolder(jsonString, mContext);
			}
		} catch (IOException e1) {
			Log.d(TAG, "Couldn't convert");
			e1.printStackTrace();
		}
		
	    ArrayList<Node> nodeList = new ArrayList<Node>();
    	String nodeID, lat, lon, timestamp, data;
		
	    try {
	    	Log.d(TAG, "JSON String is: "+ newJSON);
		    //create a json object with the JSON string passed in
		    JSONObject jAll = new JSONObject(newJSON);
		    Log.d(TAG, "Object created");
		    
		    //An array of node JSON objects
		    JSONArray nodeArray = jAll.getJSONArray("Nodes");
		    Log.d(TAG, "Node Array created");
		    
		    //Iterate through node array, working on one node at a time
		    for(int i=0;i<nodeArray.length();i++){
		    	//Get node object from node array
		    	JSONObject node = nodeArray.getJSONObject(i);
		    	
		    	//Get data from the JSONObject"
		    	nodeID = node.getString("NodeID");
		    	Log.v(TAG, "nodeID: "+nodeID);
		    	lat = node.getString("Lat");
		    	Log.v(TAG, "lat: "+lat);
		    	lon = node.getString("Long");
		    	Log.v(TAG, "lon: "+lon);
		    	timestamp = node.getString("Timestamp");
		    	Log.v(TAG, "timestamp: "+timestamp);
		    	data = node.getString("Data");
		    	Log.v(TAG, "data: "+data);
		    	
		    	Log.d(TAG, "Node "+i+" info recieved");
		    	//Create Node object
		    	Node myNode = new Node(Integer.parseInt(nodeID), new NodeLocation(Double.parseDouble(lat), Double.parseDouble(lon),0), timestamp, data);
		    	Log.d(TAG, "Node "+i+" created");
		    	//Add Node to List
		    	nodeList.add(myNode);
		    }
		    
		} catch (JSONException e) {
		    e.printStackTrace();
		}
	    return nodeList;
    }
}