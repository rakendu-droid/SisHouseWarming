package com.rrj.house.warming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateUtils;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class GeoFence implements ConnectionCallbacks, OnConnectionFailedListener,
OnAddGeofencesResultListener{

	

	private LocationClient mLocationClient;
	BroadcastReceiver mGeofenceBroadcastReceiver;
	IntentFilter mIntentFilter;
	String mGeofenceState;
	PendingIntent mGeofencePendingIntent=null;
	Geofence myGeofence;
	Context mContext;
	private List<Geofence> mCurrentGeofences;
	
	
	public GeoFence(Context context) {
		mContext =context;
		mLocationClient = new LocationClient(mContext, this, this);
		mIntentFilter = new IntentFilter();
		
		mIntentFilter.addAction("ACTION_GEOFENCES_ADDED");
		 
        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction("ACTION_GEOFENCES_REMOVED");
 
        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction("ACTION_GEOFENCE_ERROR");
 
        // All Location Services sample apps use this category
        mIntentFilter.addCategory("CATEGORY_LOCATION_SERVICES");
        Log.d("APPTAG","Geofence");
       // createGeofences();
        //mGeofenceState = "CAN_START_GEOFENCE";
        
        Boolean isConnected =servicesConnected();
        if(isConnected)
        	createGeofences();
	}
	
	private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status

            // Continue
            return true;

        // Google Play services was not available for some reason
        } else {

            // Display an error dialog
//            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
//            if (dialog != null) {
//                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
//                errorFragment.setDialog(dialog);
//                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
//            }

            return false;
        }
	}
        
        private void createGeofences() {
    		// TODO Auto-generated method stub
        	
            //12.908524,77.546766

            Log.d("APPTAG","Create Geofence");
        	myGeofence=new Geofence.Builder()
            .setRequestId("1")
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setCircularRegion(
                    12.920783,
                    77.58922,
                    200)//
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build();
        	mCurrentGeofences = new ArrayList<Geofence>();
        	
        	mCurrentGeofences.add(myGeofence);
            Log.d("APPTAG","Create Geofence 2");
        	//mLocationClient = new LocationClient(this, this, this);
        	requestConnection();
        	
        	
    	}
        private void requestConnection() {
            getLocationClient().connect();
           
        }
        private GooglePlayServicesClient getLocationClient() {
            if (mLocationClient == null) {

                mLocationClient = new LocationClient(mContext, this, this);
            }
            return mLocationClient;
        }
        
        
        private PendingIntent createRequestPendingIntent() {

            // If the PendingIntent already exists
            if (null != mGeofencePendingIntent) {

                // Return the existing intent
                return mGeofencePendingIntent;

            // If no PendingIntent exists
            } else {
                Log.d("APPTAB","Intent Service");
                // Create an Intent pointing to the IntentService
                Intent intent = new Intent(mContext, ReceiveTransitionsIntentService.class);
                /*
                 * Return a PendingIntent to start the IntentService.
                 * Always create a PendingIntent sent to Location Services
                 * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
                 * again updates the original. Otherwise, Location Services
                 * can't match the PendingIntent to requests made with it.
                 */
                return PendingIntent.getService(
                        mContext,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		// TODO Auto-generated method stub

		Intent broadcastIntent = new Intent();
		 //Temp storage for messages
	        String msg;

	        // If adding the geocodes was successful
	        if (LocationStatusCodes.SUCCESS == statusCode) {

	        	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	        	if(!sharedPreferences.contains("geoExistsFile")){
                    Log.d("APPTAG", "New Geofence ");
	        		Editor fileEditor = sharedPreferences.edit();
	        		fileEditor.putString("geoExistsFile", "True");
	        		fileEditor.commit();
	        	
	            // Create a message containing all the geofence IDs added.
	            msg = mContext.getString(R.string.add_geofences_result_success,
	                    Arrays.toString(geofenceRequestIds));

	            // In debug mode, log the result
	            Log.d(GeofenceUtils.APPTAG, msg);

	            // Create an Intent to broadcast to the app
	            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCES_ADDED)
	                           .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
	                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
	        	}
	        	else
	        	{
	        		Log.d("APPTAG", "Geofence exists");
	        	}
	        		
	        // If adding the geofences failed
	        } else {

	            /*
	             * Create a message containing the error code and the list
	             * of geofence IDs you tried to add
	             */
	            msg = mContext.getString(
	                    R.string.add_geofences_result_failure,
	                    statusCode,
	                    Arrays.toString(geofenceRequestIds)
	            );

	            // Log an error
	            //Log.e(GeofenceUtils.APPTAG, msg);

	            // Create an Intent to broadcast to the app
	            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
	                           .addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES)
	                           .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, msg);
	        }

	        // Broadcast whichever result occurred
	        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);

	        // Disconnect the location client
	        //requestDisconnection();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		Log.d("APPTAG", "mActivity.getString(R.string.connected)");
		mGeofencePendingIntent = createRequestPendingIntent();
		mLocationClient.addGeofences(mCurrentGeofences, mGeofencePendingIntent, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction(); 

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

            // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                    ||
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

            // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

            // The Intent contained an invalid action
            } else {
                //Log.e(GeofenceUtils.APPTAG, "getString(R.string.invalid_action_detail, action)");
                Toast.makeText(context, "R.string.invalid_action", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            //Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
	}

}
