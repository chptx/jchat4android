package com.tilab.msn;



import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.AID;
import jade.core.Profile;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.ContextMenuInfo;
import android.widget.TabHost.TabSpec;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayController;


public class ContactListActivity extends MapActivity implements ConnectionListener {
   
	private JadeGateway gateway;
	private final Logger myLogger = Logger.getMyLogger(this.getClass().getName());
	private TabHost mainTabHost;
	private ListView contactsListView;
	private MapController mapController;
	private OverlayController overlayCtrl;

	//Adapter for the contacts list
	private ContactListAdapter contactsAdapter;
	
	//MENUITEM CONSTANT
	private final int MENUITEM_ID_EXIT=Menu.FIRST;
	
	//Menu entries
	private final int CONTEXT_MENU_ITEM_CHAT = Menu.FIRST+1;
	private final int CONTEXT_MENU_ITEM_CALL = Menu.FIRST+2;
	private final int CONTEXT_MENU_ITEM_SMS = Menu.FIRST+3;
	
	//NEEDED TAGS FOR THE TABHOST (to address them)
	private final String CONTACTS_TAB_TAG="ContactsTab";
	private final String MAPVIEW_TAB_TAG="MapViewTab";

	
	//Array of updaters
	private Map<String, ContactsUIUpdater> updaters;
	
	public static final String OTHER_PARTICIPANTS = "com.tilab.msn.Prova";
	
	private void initUI(){
		//Setup the main tabhost
        setContentView(R.layout.main);
        mainTabHost = (TabHost) findViewById(R.id.main_tabhost);
        mainTabHost.setup();
    
      //Fill the contacts tab
        TabSpec contactsTabSpecs = mainTabHost.newTabSpec(CONTACTS_TAB_TAG);
		contactsTabSpecs.setIndicator(getText(R.string.contacts_tab_name));
		contactsTabSpecs.setContent(R.id.content1);
		mainTabHost.addTab(contactsTabSpecs);
        
    	//Fill the map tab
		TabSpec mapTabSpecs = mainTabHost.newTabSpec(MAPVIEW_TAB_TAG);
		mapTabSpecs.setIndicator(getText(R.string.mapview_tab_name));
		mapTabSpecs.setContent(R.id.content2);
		mainTabHost.addTab(mapTabSpecs);
        
        
		//init the map view
		MapView mapView = (MapView) findViewById(R.id.myMapView);
		mapController = mapView.getController();
		
		overlayCtrl = mapView.createOverlayController();
		overlayCtrl.add(new ContactsPositionOverlay(mapView,getResources()),true);
	
		
		//Create the updater array
        updaters = new HashMap<String, ContactsUIUpdater>(2);
        updaters.put(CONTACTS_TAB_TAG, new ContactListUpdater(this)); 
        updaters.put(MAPVIEW_TAB_TAG, new MapUpdater(this));
	
        //set the default updater
		TilabMsnApplication myApp =  (TilabMsnApplication) getApplication();
		myApp.myBehaviour.setContactsUpdater(updaters.get(CONTACTS_TAB_TAG));
	
		//Select default tab
		mainTabHost.setCurrentTabByTag(CONTACTS_TAB_TAG);
		
		
	     //Set the handler for the click on the tab host
		mainTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

					
					public void onTabChanged(String arg0) {
						
						TilabMsnApplication myApp =  (TilabMsnApplication) getApplication();
						myLogger.log(Logger.FINER, "Tab was switched! Current tab is "+ arg0 + " Changing the updater...");
						
						//FIXME: THIS LOOKS LIKE AN ANDROID BUG!!!!
						//We should investigate!!!!!
						if (arg0 == null){
							// TODO Auto-generated method stub	
							myApp.myBehaviour.setContactsUpdater(updaters.get(CONTACTS_TAB_TAG));
							//This forced update could be dangerous!!!
							contactsAdapter.updateAdapter(ContactManager.getInstance().getMyContact().getLocation(), ContactManager.getInstance().getOtherContactList());
							contactsListView.setAdapter(contactsAdapter);
							
						} else {
							myApp.myBehaviour.setContactsUpdater(updaters.get(MAPVIEW_TAB_TAG));
							
						}
					}        	
		});

		contactsListView = (ListView) findViewById(R.id.contactsList);
		//added ContextMenu
		contactsListView.setOnPopulateContextMenuListener(
				new View.OnPopulateContextMenuListener(){
					public void  onPopulateContextMenu(ContextMenu menu, View v, Object menuInfo) {
						ListView myLv = (ListView) v;
						ContextMenuInfo info = (ContextMenuInfo) menuInfo;
						myLv.setSelection(info.position);

						menu.add(0, CONTEXT_MENU_ITEM_CHAT, R.string.menu_item_chat);
						menu.add(0, CONTEXT_MENU_ITEM_CALL, R.string.menu_item_call);
						menu.add(0, CONTEXT_MENU_ITEM_SMS, R.string.menu_item_sms);
					}
				}
		);		
	
		contactsAdapter = new ContactListAdapter(this);
		ContactManager.getInstance().readPhoneContacts(this);
		contactsAdapter.updateAdapter(ContactManager.getInstance().getMyContact().getLocation(), ContactManager.getInstance().getOtherContactList());
		contactsListView.setAdapter(contactsAdapter);
	}		
    
	
	private String getRandomNumber(){
		Random rnd = new Random();
		int randInt  = rnd.nextInt();
		return "RND" + String.valueOf(randInt);
	}
	
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        //Initialize the UI
        initUI();
           
        //fill Jade connection properties
        Properties jadeProperties = new Properties(); 
        jadeProperties.setProperty(Profile.MAIN_HOST, getString(R.string.jade_platform_host));
        jadeProperties.setProperty(Profile.MAIN_PORT, getString(R.string.jade_platform_port));
        //Get the phone number of my contact
        String numtel = SystemProperties.get("numtel");
		
		//if number is not available
		if (numtel.equals("")){
			myLogger.log(Logger.WARNING, "Cannot access the numtel! A random number shall be used!!!");
			numtel = getRandomNumber();
		}
       
        jadeProperties.setProperty(JICPProtocol.MSISDN_KEY, numtel);
        
        GeoNavigator.setLocationProviderName(getText(R.string.location_provider_name).toString());
        GeoNavigator.getInstance(this).startLocationUpdate();
        
        //try to get a JadeGateway
        try {
			JadeGateway.connect(MsnAgent.class.getName(), jadeProperties, this, this);
		} catch (Exception e) {
			//troubles during connection
			Toast.makeText(this, 
						   getString(R.string.error_msg_jadegw_connection), 
						   Integer.parseInt(getString(R.string.toast_duration))
						   ).show();
		}
    
    }

	
		
	protected void onDestroy() {
		
		super.onDestroy();
		myLogger.log(Logger.INFO, "onDestroy called ...");
		GeoNavigator.getInstance(this).stopLocationUpdate();
		
		TilabMsnApplication myApp = (TilabMsnApplication) getApplication(); 
		
		UnsubscribeCommand unsubscribe = new UnsubscribeCommand(myApp.myBehaviour);
		
		if (gateway != null) {
			
			try {
				gateway.execute(unsubscribe);
				gateway.shutdownJADE();
			} catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ControllerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gateway.disconnect(this);
		}
	}
	
	public void onConnected(JadeGateway arg0) {
		this.gateway = arg0;
	
		myLogger.log(Logger.INFO, "onConnected(): SUCCESS!");

		try {
				//FIXME: this code is needed to start JADE and to put online MyContact.
				//I cannot find another way to be sure that the agent is up!!!
				GetAIDCommandBehaviour getAIDBh = new GetAIDCommandBehaviour();
				gateway.execute(getAIDBh);
				
				
				//If agent is up
				if (getAIDBh.isSuccess()){
					//put my contact online
					ContactManager.getInstance().getMyContact().setAgentContact(((AID) getAIDBh.getCommandResult()).getName());
					TilabMsnApplication myApp =  (TilabMsnApplication) getApplication();
					gateway.execute(myApp.myBehaviour);
				} else {
					Toast.makeText(this, "Error during agent startup", 2000);
				}
			
				
						
		} catch(Exception e){
			Toast.makeText(this, e.toString(), 1000).show();
		}
	}


	protected void onResume() {
		super.onResume();
		myLogger.log(Logger.INFO, "onResume was called");
	
	}

	
	
	protected void onPause() {
		super.onPause();
		myLogger.log(Logger.INFO, "onPause was called");
	
	//	GeoNavigator.getInstance(this).pauseLocationUpdate();
	}

	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	

	protected void onFreeze(Bundle outState) {
		// TODO Auto-generated method stub
		myLogger.log(Logger.INFO, "onFreeze was called");
		super.onFreeze(outState);
	}




	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENUITEM_ID_EXIT, R.string.menuitem_exit);
		return true;
	}
	
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);
		
		switch(item.getId()) {
			case MENUITEM_ID_EXIT:
				finish();
			break;			
		}
		return true;
	}

	public boolean onContextItemSelected(Item item) {
		
		switch(item.getId()) {
			case CONTEXT_MENU_ITEM_CALL:
				break;
			case CONTEXT_MENU_ITEM_CHAT:
				Contact ct = (Contact) contactsListView.getSelectedItem();
				ArrayList parts = new ArrayList();
				parts.add(ct);
				((TilabMsnApplication)getApplication()).myBehaviour.setContactsUpdater(null);
				Intent it = new Intent();
				it.putExtra(OTHER_PARTICIPANTS, parts);
				it.setClass(this, ChatActivity.class);
				startActivity(it);
				break;
			case CONTEXT_MENU_ITEM_SMS:
				break;
			default:
		}
		return false;
	}
	
	private void refreshContactList(){
		if (ContactManager.getInstance().updateIsOngoing()) {
			contactsAdapter.updateAdapter(ContactManager.getInstance().getMyContact().getLocation(), ContactManager.getInstance().getOtherContactList());
			contactsListView.setAdapter(contactsAdapter);
		}
	}
	
	/**
	 * This class perform the GUI update
	 * @author s.semeria
	 *
	 */

	private class ContactListUpdater extends ContactsUIUpdater{

		public ContactListUpdater(Activity act) {
			super(act);
			// TODO Auto-generated constructor stub
		}

		protected void handleUpdate() {
			// TODO Auto-generated method stub
					
			refreshContactList();
		
		}
		
	}
	
	
	private class MapUpdater extends ContactsUIUpdater{

		public MapUpdater(Activity act) {
			super(act);
			// TODO Auto-generated constructor stub
		}

			
			
			protected void handleUpdate() {
				// TODO Auto-generated method stub
				if (ContactManager.getInstance().updateIsOngoing()){
					MapView mapView = (MapView) activity.findViewById(R.id.myMapView);
					mapView.invalidate();
				}
			}
			
	}
}