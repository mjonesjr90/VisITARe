package com.thesis.visitare;

/**
 * 
 * @author malcomjonesjr
 * The NodeLocation class allows for the creation of an instance 
 * of a nodes location using latitude, longitude, and altitude. 
 * The values of these variables are stored as Java double 
 * primitive data types, allowing for more precise accuracy when 
 * compared to integers.
 *
 */

public class NodeLocation {
	double lat;
	double lon;
	double alt;
	public NodeLocation(){
		this.lat = 0;
		this.lon = 0;
		this.alt = 0;
	}
	public NodeLocation(double nLat, double nLon, double nAlt){
		this.lat = nLat;
		this.lon = nLon;
		this.alt = nAlt;
	}
	public double getLat(){
		return this.lat;
	}
	public double getLon(){
		return this.lon;
	}
	public double getAlt(){
		return this.alt;
	}
}
