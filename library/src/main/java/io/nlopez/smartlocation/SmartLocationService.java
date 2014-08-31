package io.nlopez.smartlocation;

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

    private static final String TAG = SmartLocationService.class.getSimpleName();

    public static final String PREFERENCES_FILE = "SMART_LOCATION_CACHE_PREFERENCES";
    public static final String LAST_LOCATION_LATITUDE_KEY = "LAST_LOCATION_LATITUDE";
    public static final String LAST_LOCATION_LONGITUDE_KEY = "LAST_LOCATION_LONGITUDE";
    public static final String LAST_LOCATION_UPDATED_AT_KEY = "LAST_LOCATION_UPDATED_AT";

    private final IBinder localBinder = new LocalBinder();
    private final Handler handler = new Handler();

    private SharedPreferences sharedPreferences;

    private String callerPackage;
    private DetectedActivity currentActivity = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
    private SmartLocationOptions smartLocationOptions = new SmartLocationOptions();

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
        if (smartLocationOptions.isActivityRecognizer()) {
            detectionRequester = new ActivityDetectionRequester(this);
            detectionRemover = new ActivityDetectionRemover(this);
        }
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

        if (options.isFusedProvider()) {
            if (!locationClient.isConnected()) {
                if (smartLocationOptions.isDebugging()) {
                    Log.v(TAG, "Connecting to fused provider");
                }
                locationClient.connect();
                handler.postDelayed(runOldSchoolLocation, options.getSecondsUntilFallback() * 1000);
            }
        } else {
            if (locationClient.isConnected()) {
                locationClient.disconnect();
            }
            startOldSchoolLocation();
        }
        if (smartLocationOptions.isActivityRecognizer()) {
            detectionRequester.requestUpdates();
        }
    }

    private Runnable runOldSchoolLocation = new Runnable() {
        @Override
        public void run() {
            if (smartLocationOptions.isDebugging()) {
                Log.v(TAG, "fallback - runOldSchoolLocation");
            }
            startOldSchoolLocation();
        }
    };

    private void startOldSchoolLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) SmartLocationService.this.getSystemService(LOCATION_SERVICE);
        }

        UpdateStrategy updateStrategy = smartLocationOptions.getDefaultUpdateStrategy();
        String provider = updateStrategy.getProvider();
        String providerFallback = updateStrategy.getProviderFallback();
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "old - Trying with " + provider);
        }

        if (provider != null) {
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.requestLocationUpdates(
                        provider,
                        updateStrategy.getFastestInterval(),
                        updateStrategy.getSmallestDisplacement(),
                        this);
            } else {
                if (locationManager.isProviderEnabled(providerFallback)) {
                    locationManager.requestLocationUpdates(
                            providerFallback,
                            updateStrategy.getFastestInterval(),
                            updateStrategy.getSmallestDisplacement(),
                            this);
                } else {
                    Log.e(TAG, "there are no providers available");
                }
            }
            if (locationClient != null && locationClient.isConnected()) {
                locationClient.removeLocationUpdates(this);
            }
        }
    }

    /**
     * Stores an updated version of the options bundle
     *
     * @param options
     */

    public void setOptions(SmartLocationOptions options) {
        stopLocation();
        startLocation(options);
        setLocationRequestValues(options, null);
    }

    private void setLocationRequestValues(SmartLocationOptions options, UpdateStrategy strategyOverride) {

        UpdateStrategy strategy = (strategyOverride == null) ? options.getDefaultUpdateStrategy() : strategyOverride;

        int minDistance = (options.getSmallestDisplacement() == -1) ?
                strategy.getSmallestDisplacement() : options.getSmallestDisplacement();

        long updateInterval = (options.getInterval() == -1) ?
                strategy.getInterval() : options.getInterval();

        long fastestInterval = (options.getFastestInterval() == -1) ?
                strategy.getFastestInterval() : options.getFastestInterval();

        if (smartLocationOptions.isFusedProvider()) {
            locationRequest
                    .setPriority(strategy.getLocationRequestPriority())
                    .setInterval(updateInterval)
                    .setSmallestDisplacement(minDistance)
                    .setFastestInterval(fastestInterval);
        } else {
            locationManager.removeUpdates(this);
            if (locationManager.isProviderEnabled(strategy.getProvider())) {
                locationManager.requestLocationUpdates(
                        strategy.getProvider(),
                        updateInterval,
                        minDistance,
                        this);
            } else {
                if (strategy.getProviderFallback() != null && locationManager.isProviderEnabled(strategy.getProviderFallback())) {
                    locationManager.requestLocationUpdates(
                            strategy.getProvider(),
                            updateInterval,
                            minDistance,
                            this);
                } else {
                    Log.e(TAG, "there are no providers available");
                }
            }
        }
    }

    private void continueStartLocation() {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "fused - continueStartLocation");
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
            if (smartLocationOptions.isActivityRecognizer()) {
                unregisterReceiver(activityUpdatesReceiver);
                detectionRemover.removeUpdates(detectionRequester.getRequestPendingIntent());
            }
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
                Log.v(TAG, "new activity detected = " + activityType + " with confidence of " + confidence + "%");
            }
            currentActivity = new DetectedActivity(activityType, confidence);


            if (lastLocation != null) {
                processLocation(lastLocation);
            }

            UpdateStrategy strategy = smartLocationOptions.getOnActivityRecognizerUpdatedNewStrategy().getUpdateStrategyForActivity(currentActivity);
            setLocationRequestValues(smartLocationOptions, strategy);

        }
    };

    @Override
    public void onLocationChanged(Location location) {
        handler.removeCallbacks(runOldSchoolLocation);

        lastLocation = location;
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "Received location " + location);
        }
        processLocation(location);
    }

    private void processLocation(Location location) {
        String intentName = getLocationUpdatedIntentName();
        if (smartLocationOptions.isDebugging()) {
            if (locationManager == null) {
                Log.v(TAG, "Broadcasting new location intent " + intentName + " (fused)");
            } else {
                Log.v(TAG, "Broadcasting new location intent " + intentName + " (LocationManager)");
            }
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(intentName);
        if (smartLocationOptions.isActivityRecognizer()) {
            broadcastIntent.putExtra(SmartLocation.DETECTED_ACTIVITY_KEY, currentActivity.getType());
            broadcastIntent.putExtra(SmartLocation.DETECTED_ACTIVITY_CONFIDENCE_KEY, currentActivity.getConfidence());
        }
        broadcastIntent.putExtra(SmartLocation.DETECTED_LOCATION_KEY, location);
        getApplicationContext().sendBroadcast(broadcastIntent);

        storeLastLocation(location);
    }


    @Override
    public void onConnected(Bundle bundle) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "fused - connected");
        }
        continueStartLocation();
    }

    @Override
    public void onDisconnected() {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "fused - disconnected");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "fused - connectionFailed");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "old - onStatusChanged " + provider + " (" + status + ")");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "old - onProviderEnabled " + provider);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (smartLocationOptions.isDebugging()) {
            Log.v(TAG, "old - onProviderDisabled " + provider);
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
