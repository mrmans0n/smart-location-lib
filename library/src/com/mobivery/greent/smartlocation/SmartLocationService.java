package com.mobivery.greent.smartlocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
 * Created by Nacho L. on 13/06/13.
 */
public class SmartLocationService extends Service implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String PREFERENCES_FILE = "SMART_LOCATION_CACHE_PREFERENCES";
    public static final String LAST_LOCATION_LATITUDE_KEY = "LAST_LOCATION_LATITUDE";
    public static final String LAST_LOCATION_LONGITUDE_KEY = "LAST_LOCATION_LONGITUDE";
    public static final String LAST_LOCATION_UPDATED_AT_KEY = "LAST_LOCATION_UPDATED_AT";

    private final IBinder mBinder = new LocalBinder();

    private SharedPreferences sharedPreferences;

    private String callerPackage;
    private int currentActivity = DetectedActivity.UNKNOWN;
    private SmartLocationOptions options;

    private Location lastLocation;

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
        callerPackage = SmartLocation.DEFAULT_PACKAGE;
    }

    private void initLocation() {
        locationClient = new LocationClient(this, this, this);
        locationRequest = LocationRequest.create();
    }

    private void initActivityRecognition() {
        detectionRequester = new ActivityDetectionRequester(this);
        detectionRemover = new ActivityDetectionRemover(this);
    }

    /**
     * Starts the process in which we will locate the user using fused location updates
     *
     * @param options caller options
     */
    public void startLocation(SmartLocationOptions options) {
        this.options = options;
        if (options.getPackageName() != null) {
            callerPackage = options.getPackageName();
        }

        if (!locationClient.isConnected()) {
            locationClient.connect();
            detectionRequester.requestUpdates();
        }
    }

    /**
     * Stores an updated version of the options bundle
     *
     * @param options
     */
    public void setOptions(SmartLocationOptions options) {
        this.options = options;
        setLocationRequestValues(options.getDefaultUpdateStrategy());
    }

    private void setLocationRequestValues(UpdateStrategy strategy) {
        locationRequest
                .setPriority(strategy.getLocationRequestPriority())
                .setInterval(strategy.getUpdateInterval())
                .setFastestInterval(strategy.getFastestInterval());
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

            if (lastLocation != null) {
                processLocation(lastLocation);
            }

            UpdateStrategy strategy = options.getOnActivityRecognizerUpdatedNewStrategy().getUpdateStrategyForActivity(activityType);
            setLocationRequestValues(strategy);
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        processLocation(location);

    }

    private void processLocation(Location location) {
        String intentName = getLocationUpdatedIntentName();
        Log.i(getClass().getSimpleName(), "[LOCATION] Broadcasting new location intent " + intentName);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(intentName);
        broadcastIntent.putExtra(SmartLocation.DETECTED_ACTIVITY_KEY, currentActivity);
        broadcastIntent.putExtra(SmartLocation.DETECTED_LOCATION_KEY, location);
        getApplicationContext().sendBroadcast(broadcastIntent);

        storeLastLocation(location);
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

    private void storeLastLocation(Location location) {
        if (sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_LOCATION_LATITUDE_KEY, Utils.getInt1E6FromDouble(location.getLatitude()));
        editor.putInt(LAST_LOCATION_LONGITUDE_KEY, Utils.getInt1E6FromDouble(location.getLongitude()));
        editor.putLong(LAST_LOCATION_UPDATED_AT_KEY, System.currentTimeMillis());
        editor.commit();
    }

}
