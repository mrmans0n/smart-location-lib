package com.mobivery.greent.smartlocation;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * Created by Nacho L. on 12/06/13.
 */
public class ActivityDetectionRemover implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private Context context;
    private PendingIntent pendingIntent;
    private ActivityRecognitionClient activityRecognitionClient;
    
    private SmartLocationOptions smartLocationOptions;

    public ActivityDetectionRemover(Context context) {
        this.context = context;

        if (smartLocationOptions == null) {
            smartLocationOptions = new SmartLocationOptions();
        }
        activityRecognitionClient = null;
    }

    public void removeUpdates(PendingIntent requestIntent) {
        pendingIntent = requestIntent;
        requestConnection();
    }

    private void requestConnection() {
        getActivityRecognitionClient().connect();
    }

    private void requestDisconnection() {
        getActivityRecognitionClient().disconnect();
        setActivityRecognitionClient(null);
    }

    private void continueRemoveUpdates() {
        getActivityRecognitionClient().removeActivityUpdates(pendingIntent);
        pendingIntent.cancel();
        requestDisconnection();
    }

    private ActivityRecognitionClient getActivityRecognitionClient() {
        if (activityRecognitionClient == null) {
            activityRecognitionClient = new ActivityRecognitionClient(context, this, this);
        }
        return activityRecognitionClient;
    }

    private void setActivityRecognitionClient(ActivityRecognitionClient client) {
        this.activityRecognitionClient = client;
    }

    @Override
    public void onConnected(Bundle bundle) {
    	if(smartLocationOptions.getDebugging()) {
    		Log.i(getClass().getSimpleName(), "connected");
    	}
        continueRemoveUpdates();
    }

    @Override
    public void onDisconnected() {
    	if(smartLocationOptions.getDebugging()) {
    		Log.i(getClass().getSimpleName(), "disconnected");
    	}
        setActivityRecognitionClient(null);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	if(smartLocationOptions.getDebugging()) {
    		Log.i(getClass().getSimpleName(), "connection failed");
    	}
    }
}