package com.tilab.msn;

import android.location.Location;

public class ContactLocation extends Location {

	private final boolean hasMoved;
	public static final ContactLocation INVALID_LOCATION=new ContactLocation();

	public ContactLocation(){
		hasMoved = false;
		setLatitude(Double.NEGATIVE_INFINITY);
		setLongitude(Double.NEGATIVE_INFINITY);
		setAltitude(Double.NEGATIVE_INFINITY);
	}
	
	public ContactLocation( ContactLocation toBeCopied){
		this(toBeCopied, toBeCopied.hasMoved);
	}
	
	private ContactLocation(Location loc, boolean moved){
		super(loc);
		hasMoved = moved;
	}
	
	private ContactLocation(double latitude, double longitude, double altitude, boolean moved){
		setLatitude(latitude);
		setLongitude(longitude);
		setAltitude(altitude);
		hasMoved = moved;
	}
	
	public ContactLocation changeLocation(Location loc)
	{   boolean moved = !this.equals(loc);
		return new ContactLocation(loc,moved);
	}
	
	public ContactLocation changeLocation(double latitude, double longitude, double altitude)
	{   boolean moved = ( (getLatitude() != latitude) || (getLongitude() != longitude) || (getAltitude() != altitude) );
		return new ContactLocation(latitude, longitude, altitude ,moved);
	}
	
	public boolean hasMoved(){
		return hasMoved;
	}
	
}