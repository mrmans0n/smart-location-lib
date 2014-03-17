package com.mobivery.greent.smartlocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
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
public class SmartLocationService extends Service implements LocationListener, android.location.LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String PREFERENCES_FILE = "SMART_LOCATION_CACHE_PREFERENCES";
    public static final String LAST_LOCATION_LATITUDE_KEY = "LAST_LOCATION_LATITUDE";
    public static final String LAST_LOCATION_LONGITUDE_KEY = "LAST_LOCATION_LONGITUDE";
    public static final String LAST_LOCATION_UPDATED_AT_KEY = "LAST_LOCATION_UPDATED_AT";

    private final IBinder localBinder = new LocalBinder();
    private final Handler handler = new Handler();

    private SharedPreferences sharedPreferences;

    private String callerPackage;
    private DetectedActivity currentActivity = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
    private SmartLocationOptions smartLocationOptions;

    private Location lastLocation;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    private ActivityDetectionRequester detectionRequester;
    private ActivityDetectionRemover detectionRemover;

    private LocationManager locationManager;

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
        return localBinder;
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
        this.smartLocationOptions = options;
        if (options.getPackageName() != null) {
            callerPackage = options.getPackageName();
        }

        if (!locationClient.isConnected()) {
            locationClient.connect();
            handler.postDelayed(runOldSchoolLocation, options.getSecondsUntilFallback() * 1000);
            detectionRequester.requestUpdates();
        }
    }

    private Runnable runOldSchoolLocation = new Runnable() {
        @Override
        public void run() {
            if (smartLocationOptions.isDebugging()) {
                Log.v(SmartLocationService.class.getSimpleName(), "[OLD-LOCATION] runOldSchoolLocation");
            }
            startOldSchoolLocation();
        }
    };

    private void startOldSchoolLocation() {
        locationManager = (LocationManager) SmartLocationService.this.getSystemService(LOCATION_SERVICE);

        UpdateStrategy updateStrategy = smartLocationOptions.getDefaultUpdateStrategy();
        String provider = updateStrategy.getProvider();
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[OLD-LOCATION] Trying with " + provider);
        }

        // Fallback's fallback - if our desired provider isn't working we try the other one
        if (!locationManager.isProviderEnabled(provider)) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
                if (smartLocationOptions.isDebugging()) {
                    Log.v(getClass().getSimpleName(), "[OLD-LOCATION] Trying with " + LocationManager.GPS_PROVIDER);
                }
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
                if (smartLocationOptions.isDebugging()) {
                    Log.v(getClass().getSimpleName(), "[OLD-LOCATION] Trying with " + LocationManager.NETWORK_PROVIDER);
                }
            } else {
                provider = null;
                if (smartLocationOptions.isDebugging()) {
                    Log.v(getClass().getSimpleName(), "[OLD-LOCATION] no providers found, aborting location stuff");
                }
            }
        }

        if (provider != null) {
            locationManager.requestLocationUpdates(
                    provider,
                    updateStrategy.getFastestInterval(),
                    updateStrategy.getMinDistance(),
                    this);
            locationClient.removeLocationUpdates(this);
        }
    }

    /**
     * Stores an updated version of the options bundle
     *
     * @param options
     */
    public void setOptions(SmartLocationOptions options) {
        this.smartLocationOptions = options;
        setLocationRequestValues(options.getDefaultUpdateStrategy());
    }

    private void setLocationRequestValues(UpdateStrategy strategy) {
        locationRequest
                .setPriority(strategy.getLocationRequestPriority())
                .setInterval(strategy.getUpdateInterval())
                .setFastestInterval(strategy.getFastestInterval());
    }

    private void continueStartLocation() {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[LOCATION] continueStartLocation");
        }
        if (locationClient.isConnected()) {
            locationClient.requestLocationUpdates(locationRequest, this);
        }
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
        if (locationManager != null) {
            locationManager.removeUpdates(this);
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
            int confidence = intent.getIntExtra(ActivityRecognitionConstants.ACTIVITY_CONFIDENCE_KEY, 0);
            if (smartLocationOptions.isDebugging()) {
                Log.v(getClass().getSimpleName(), "[ACTIVITY] new activity detected = " + activityType + " with confidence of " + confidence + "%");
            }
            currentActivity = new DetectedActivity(activityType, confidence);


            if (lastLocation != null) {
                processLocation(lastLocation);
            }

            UpdateStrategy strategy = smartLocationOptions.getOnActivityRecognizerUpdatedNewStrategy().getUpdateStrategyForActivity(currentActivity);
            setLocationRequestValues(strategy);
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        handler.removeCallbacks(runOldSchoolLocation);

        lastLocation = location;
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[LOCATION] Received location " + location);
        }
        processLocation(location);
    }

    private void processLocation(Location location) {
        String intentName = getLocationUpdatedIntentName();
        if (smartLocationOptions.isDebugging()) {
            if (locationManager == null) {
                Log.v(getClass().getSimpleName(), "[LOCATION] Broadcasting new location intent " + intentName + " (fused)");
            } else {
                Log.v(getClass().getSimpleName(), "[LOCATION] Broadcasting new location intent " + intentName + " (LocationManager)");
            }
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(intentName);
        broadcastIntent.putExtra(SmartLocation.DETECTED_ACTIVITY_KEY, currentActivity.getType());
        broadcastIntent.putExtra(SmartLocation.DETECTED_ACTIVITY_CONFIDENCE_KEY, currentActivity.getConfidence());
        broadcastIntent.putExtra(SmartLocation.DETECTED_LOCATION_KEY, location);
        getApplicationContext().sendBroadcast(broadcastIntent);

        storeLastLocation(location);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[LOCATION] connected");
        }
        continueStartLocation();
    }

    @Override
    public void onDisconnected() {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[LOCATION] disconnected");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[LOCATION] connectionFailed");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[OLD-LOCATION] onStatusChanged " + provider + " (" + status + ")");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[OLD-LOCATION] onProviderEnabled " + provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(getClass().getSimpleName(), "[OLD-LOCATION] onProviderDisabled " + provider);
        }
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