package com.mobivery.smartlocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by mrm on 13/06/13.
 */
public class SmartLocationService extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private static final int IN_VEHICLE_UPDATE_INTERVAL_IN_SECONDS = 2;
    private static final int STILL_UPDATE_INTERVAL_IN_SECONDS = 5;

    private static final int MILLISECONDS_PER_SECOND = 1000;

    private static final long VEHICLE_INTERVAL =
            MILLISECONDS_PER_SECOND * IN_VEHICLE_UPDATE_INTERVAL_IN_SECONDS;
    private static final long STILL_INTERVAL = STILL_UPDATE_INTERVAL_IN_SECONDS * MILLISECONDS_PER_SECOND;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private final IBinder mBinder = new LocalBinder();

    private String callerPackage;
    private int currentActivity;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    private ActivityDetectionRequester detectionRequester;
    private ActivityDetectionRemover detectionRemover;

    public class LocalBinder extends Binder {
        public SmartLocationService getService() {
            return SmartLocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initPackageName();
        initLocation();
        initActivityRecognition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
    }

    private void initPackageName() {
        callerPackage = getClass().getPackage().getName();
    }

    private void initLocation() {
        locationClient = new LocationClient(this, this, this);
        locationRequest = LocationRequest.create();

        setNonMovingSettings();
    }

    private void setInVehicleSettings() {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(VEHICLE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private void setNonMovingSettings() {
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(STILL_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }


    private void initActivityRecognition() {
        detectionRequester = new ActivityDetectionRequester(this);
        detectionRemover = new ActivityDetectionRemover(this);
    }

    /**
     * Starts the process in which we will locate the user using fused location updates
     *
     * @param packageName caller package's name
     */
    public void startLocation(String packageName) {
        if (packageName == null) {
            initPackageName();
        } else {
            callerPackage = packageName;
        }

        if (!locationClient.isConnected()) {
            locationClient.connect();
            detectionRequester.requestUpdates();
        }
    }

    private void continueStartLocation() {
        Log.i(getClass().getSimpleName(), "[LOCATION] continueStartLocation");
        locationClient.requestLocationUpdates(locationRequest, this);
        IntentFilter intentFilterActivityUpdates = new IntentFilter(ActivityRecognitionConstants.ACTIVITY_CHANGED_INTENT);
        registerReceiver(activityUpdatesReceiver, intentFilterActivityUpdates);
    }

    /**
     * Stops the location process
     */
    public void stopLocation() {
        if (locationClient.isConnected()) {
            unregisterReceiver(activityUpdatesReceiver);
            detectionRemover.removeUpdates(detectionRequester.getRequestPendingIntent());
            locationClient.removeLocationUpdates(this);
            locationClient.disconnect();
        }

    }

    /**
     * Returns the name of the intent that will be launched when
     *
     * @return
     */
    public String getLocationUpdatedIntentName() {
        return callerPackage + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;
    }

    private BroadcastReceiver activityUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int activityType = intent.getIntExtra(ActivityRecognitionConstants.ACTIVITY_KEY, DetectedActivity.UNKNOWN);
            Log.i(getClass().getSimpleName(), "[ACTIVITY] new activity detected = " + activityType);
            currentActivity = activityType;

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    setInVehicleSettings();
                    break;
                default:
                    setNonMovingSettings();
                    break;
            }

        }
    };

    @Override
    public void onLocationChanged(Location location) {
        processLocation(location);

    }

    private void processLocation(Location location) {
        String intentName = getLocationUpdatedIntentName();
        Log.i(getClass().getSimpleName(), "Broadcasting new location intent " + intentName);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(intentName);
        broadcastIntent.putExtra(SmartLocation.DETECTED_ACTIVITY_KEY, currentActivity);
        broadcastIntent.putExtra(SmartLocation.DETECTED_LOCATION_KEY, location);
        getApplicationContext().sendBroadcast(broadcastIntent);
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.v(getClass().getSimpleName(), "[LOCATION] connected");

        continueStartLocation();
    }

    @Override
    public void onDisconnected() {
        Log.v(getClass().getSimpleName(), "[LOCATION] disconnected");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(getClass().getSimpleName(), "[LOCATION] connectionFailed");
    }

}
