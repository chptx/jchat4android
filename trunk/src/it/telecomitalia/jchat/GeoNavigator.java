/*****************************************************************
 jChat is a  chat application for Android based on JADE
  Copyright (C) 2008 Telecomitalia S.p.A. 
 
 GNU Lesser General Public License

 This is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 

 This software is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this software; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/

package it.telecomitalia.jchat;

import jade.util.Logger;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import com.google.android.maps.MyLocationOverlay;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatusListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * Handles the operations of starting/stopping My Contact location update by location provider. It also allows specifying 
 * a custom location provider to be used. 
 * <p>
 * Location update is issued by the LocationManager and takes place by periodically firing a broadcast Intent
 * that is caught by a customized IntentReceiver.
 * Using a mocked GPS location provider, data shall be red from a file named kml inside 
 * <code> /data/misc/location/(locProviderName) </code> where locProviderName is the name of the location provider
 * read from the strings.xml file.
 * 
 * @author Cristina Cucc�
 * @author Marco Ughetti 
 * @author Stefano Semeria
 * @author Tiziana Trucco
 * @version 1.0 
 */
public class GeoNavigator {

	/** 
	 * Custom action used for the location update Intent that is fired 
	 */
	public static final String LOCATION_UPDATE_ACTION= "com.tilab.msn.LOCATION_UPDATE";
	
	/** 
	 * Minimum distance in meters for sending new location update
	 */
	private final float MINIMUM_DISTANCECHANGE_FOR_UPDATE = 0.0f;  
    
    /** 
     * Minimum time in milliseconds for between location updates
     */
    private final long MINIMUM_TIME_BETWEEN_UPDATE = 0;  

    /** 
     * Instance of Jade Logger for debugging
     */
    private static final Logger myLogger = Logger.getMyLogger(GeoNavigator.class.getName());
    
    /** 
     * Intent fired for location updates 
     */
    private Intent updateIntent;
    
    /** 
     * The intent filter to use when registering intent receiver. 
     */
    private IntentFilter filter;
	
	/** 
	 * The default location provider name  
	 */
	private static final String DEFAULT_PROVIDER_NAME="gps";
	
	/** 
	 * The name of the location provider to be used. 
	 */
	private static String locProviderName = DEFAULT_PROVIDER_NAME;
	
	
	/** 
	 * The instance of the {@link GeoNavigator} object. 
	 */
	private static GeoNavigator navigator = null;
	
	/**
	 * Current application context 
	 */
	private Context myContext;
	
	/** 
	 * Instance of the Android location manager. 
	 */
	private LocationManager manager;

	
	private LocationListener listener;
	
	/**
	 * Gets the single instance of GeoNavigator.
	 * 
	 * @param c the application context
	 * 
	 * @return single instance of GeoNavigator
	 * @throws FileNotFoundException 
	 */
	public static GeoNavigator getInstance(Context c) {
		if (navigator == null)
			navigator = new GeoNavigator(c);
		return navigator;
	}
	
	
	/**
	 * Instantiates a new geo navigator.
	 * Uses the static instance of the provider name (if any) or otherwise defaults to DEFAULT_PROVIDER_NAME
	 * 
	 * @param c the application context
	 */
	private GeoNavigator(Context c) {
		manager = (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);

		myContext = c;	
		listener = new ContactsLocationListener();
	}
	
	
	/**
	 * Request the Location manager to start firing intents with location updates
	 */
	public void startLocationUpdate(){
		myLogger.log(Logger.INFO, "Starting location update...");
			
		manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, MINIMUM_DISTANCECHANGE_FOR_UPDATE,listener );

	}
	
	/**
	 * Sets the location provider name.
	 * @param provName the new location provider name
	 */
	public static void setLocationProviderName(String provName) {
		if (provName != null)
			locProviderName = provName;
	}
	
		
		
	/**
	 * Stop the firing of broadcast intents for location update.
	 */
	public void stopLocationUpdate(){
		myLogger.log(Logger.INFO, "Stopping location updates....");
		manager.removeUpdates(listener);
	}
	
	
	
	
	private class ContactsLocationListener implements LocationListener {

		
		public void onLocationChanged(Location location) {
			myLogger.log(Logger.INFO, "Location listener has received location update for location " + location);
			ContactManager.getInstance().updateMyContactLocation(location);
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
		 */
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
		 */
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}
}