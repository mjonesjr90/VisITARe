package com.thesis.visitare;

/**
 * 
 * @author malcomjonesjr
 * The Node class defines a node in the VisITARe framework. It contains a constructor 
 * that allows an instance of a node to be created. Each node consists of an ID, a
 * location, a timestamp, and a data object. Get methods are provided for data access
 * on each node.
 */

public class Node {
	int nodeID;
	NodeLocation nodeLoc = new NodeLocation();
	String timestamp;
	Object data;

    public Node(int nID, NodeLocation nLoc, String nTimestamp, Object nData) {
		this.nodeID = nID;
		this.nodeLoc = nLoc;
		this.timestamp = nTimestamp;
		this.data = nData;
	}

//    public String toString() {
//		String nodeString;
//		nodeString = "Company: "+String.valueOf(nodeID)+ '\n'+
//						"Location: Lat: "+String.valueOf(nodeLoc.getLat())+ ", Long: "+String.valueOf(nodeLoc.getLon())+ ", Alt: "+String.valueOf(nodeLoc.getAlt())+ '\n'+
//						"Date: "+timestamp+ '\n'+
//						"Description: "+data+ '\n';
//		return nodeString;
//    }

	public int getID() {
		return this.nodeID;
	}
	public NodeLocation getLocation(){
		return this.nodeLoc;
	}
	public String getTimestamp() {
		return this.timestamp;
	}
	public Object getData() {
		return this.data;
	}
	
}
