package com.mobivery.greent.smartlocation;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocation {

    /**
     * Intent action name. The rest of the package can be configured.
     * The default action name is com.mobivery.smartlocation.greent.LOCATION_UPDATED
     */
    public static final String LOCATION_BROADCAST_INTENT_TRAIL = ".LOCATION_UPDATED";

    /**
     * Default package which will be sending the location updates.
     */
    public static final String DEFAULT_PACKAGE = "com.mobivery.smartlocation.greent";

    /**
     * Detected location key for the intent bundle. Returns a Location object.
     */
    public static final String DETECTED_LOCATION_KEY = "LOCATION";

    /**
     * Detected activity for the intent bundle. Returns a DetectedActivity integer.
     */
    public static final String DETECTED_ACTIVITY_KEY = "ACTIVITY";

    /**
     * Detected activity confidence. Used to build the full DetectedActivity object.
     */
    public static final String DETECTED_ACTIVITY_CONFIDENCE_KEY = "ACTIVITY_CONFIDENCE";

    private boolean isServiceBound = false;
    private boolean isServiceConnected = false;
    private SmartLocationService boundService;
    private SmartLocationOptions smartLocationOptions;
    private OnLocationUpdatedListener onLocationUpdatedListener;
    private OnSmartLocationStatusChangedListener onSmartLocationStatusChangedListener;

    // Singleton stuff

    private SmartLocation() {
        if (smartLocationOptions == null) {
            smartLocationOptions = new SmartLocationOptions();
        }
    }

    private static class SmartLocationHolder {
        public static final SmartLocation instance = new SmartLocation();
    }

    public static SmartLocation getInstance() {
        return SmartLocationHolder.instance;
    }

    /**
     * Deprecated. Use {@link #start(android.content.Context, com.mobivery.greent.smartlocation.SmartLocation.OnLocationUpdatedListener)} instead.
     *
     * @param context
     */
    @Deprecated
    public void start(Context context) {
        start(context, new SmartLocationOptions());
    }

    /**
     * Deprecated. Use {@link #start(android.content.Context, SmartLocationOptions, com.mobivery.greent.smartlocation.SmartLocation.OnLocationUpdatedListener)} instead.
     *
     * @param context
     */
    @Deprecated
    public void start(Context context, SmartLocationOptions options) {
        start(context, options, null);
    }

    /**
     * Initializes the location process with default options
     *
     * @param context
     * @param listener
     */
    public void start(Context context, OnLocationUpdatedListener listener) {
        start(context, new SmartLocationOptions(), listener);
    }

    /**
     * Initializes the location process with custom options
     *
     * @param context
     * @param options
     * @param listener
     */
    public void start(Context context, SmartLocationOptions options, OnLocationUpdatedListener listener) {
        setOptions(options);
        captureIntent(context);
        setOnLocationUpdatedListener(listener);

        if (isServiceBound && boundService != null) {
            boundService.startLocation(smartLocationOptions);
        } else {
            isServiceBound = false;
            bindService(context);
        }
    }

    /**
     * Stops the location retrival
     *
     * @param context
     */
    public void stop(Context context) {
        if (isServiceConnected && boundService != null) {
            releaseIntent(context);
            boundService.stopLocation();
        }
    }

    /**
     * Perform necessary cleanup procedures if you don't want to use the smart location anymore
     *
     * @param context
     */
    public void cleanup(Context context) {
        unbindService(context);
    }

    /**
     * Set a new options bundle, to be updated immediatly if the service is working
     *
     * @param options
     */
    public void setOptions(SmartLocationOptions options) {

        if (options == null) {
            throw new IllegalArgumentException("options value can't be null");
        }

        smartLocationOptions = options;
        if (isServiceConnected && boundService != null) {
            boundService.setOptions(options);
        }
    }

    private boolean bindService(Context context) {
        if (!isServiceBound) {
            Intent serviceIntent = new Intent(context, SmartLocationService.class);
            isServiceBound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        if (onSmartLocationStatusChangedListener != null) {
            onSmartLocationStatusChangedListener.onServiceBoundFinished(isServiceBound);
        }
        return isServiceBound;
    }

    private void unbindService(Context context) {
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection);
                isServiceBound = false;
                if (onSmartLocationStatusChangedListener != null) {
                    onSmartLocationStatusChangedListener.onServiceUnbounded();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            createServiceConnection(iBinder);
            if (onSmartLocationStatusChangedListener != null) {
                onSmartLocationStatusChangedListener.onServiceConnected();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            destroyServiceConnection();
            if (onSmartLocationStatusChangedListener != null) {
                onSmartLocationStatusChangedListener.onServiceDisconnected();
            }
        }
    };

    private void createServiceConnection(IBinder iBinder) {
        boundService = ((SmartLocationService.LocalBinder) iBinder).getService();
        isServiceConnected = (boundService != null);

        if (boundService != null) {
            boundService.startLocation(smartLocationOptions);
        }
    }

    private void destroyServiceConnection() {
        boundService.stopLocation();
        isServiceConnected = false;
    }

    private void captureIntent(Context context) {
        try {
            IntentFilter locationUpdatesIntentFilter = new IntentFilter(smartLocationOptions.getIntentActionString());
            context.registerReceiver(locationUpdatesReceiver, locationUpdatesIntentFilter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void releaseIntent(Context context) {
        try {
            context.unregisterReceiver(locationUpdatesReceiver);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BroadcastReceiver locationUpdatesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (onLocationUpdatedListener != null) {
                Location location = intent.getParcelableExtra(SmartLocation.DETECTED_LOCATION_KEY);

                int activity = intent.getIntExtra(SmartLocation.DETECTED_ACTIVITY_KEY, DetectedActivity.UNKNOWN);
                int confidence = intent.getIntExtra(SmartLocation.DETECTED_ACTIVITY_CONFIDENCE_KEY, 0);
                DetectedActivity detectedActivity = new DetectedActivity(activity, confidence);

                onLocationUpdatedListener.onLocationUpdated(location, detectedActivity);
            }

        }
    };

    /**
     * Returns the last known location if it is in the validity period. If there is none or is not valid, returns null.
     *
     * @param context
     * @return
     */
    public Location getLastKnownLocation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SmartLocationService.PREFERENCES_FILE, Context.MODE_PRIVATE);
        int latitude1E6 = preferences.getInt(SmartLocationService.LAST_LOCATION_LATITUDE_KEY, 0);
        int longitude1E6 = preferences.getInt(SmartLocationService.LAST_LOCATION_LONGITUDE_KEY, 0);
        long lastUpdated = preferences.getLong(SmartLocationService.LAST_LOCATION_UPDATED_AT_KEY, 0);

        if (latitude1E6 == 0 && longitude1E6 == 0) {
            return null;
        }

        long now = System.currentTimeMillis();
        if (lastUpdated + smartLocationOptions.getLocationCacheValidity() < now) {
            return null;
        }

        Location location = new Location("Cached");
        location.setLatitude(Utils.getDoubleFrom1E6(latitude1E6));
        location.setLongitude(Utils.getDoubleFrom1E6(longitude1E6));
        return location;
    }

    /**
     * Returns the last known activity if it is in the validity period. If there is none or is not valid, returns null.
     *
     * @param context
     * @return
     */
    public DetectedActivity getLastKnownActivity(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(ActivityRecognitionService.PREFERENCES_FILE, Context.MODE_PRIVATE);
        long lastUpdated = preferences.getLong(ActivityRecognitionService.LAST_ACTIVITY_UPDATED_AT_KEY, 0);
        int lastActivity = preferences.getInt(ActivityRecognitionService.LAST_ACTIVITY_KEY, DetectedActivity.UNKNOWN);
        int lastConfidence = preferences.getInt(ActivityRecognitionService.LAST_ACTIVITY_CONFIDENCE_KEY, 0);

        long now = System.currentTimeMillis();
        if (lastUpdated + smartLocationOptions.getActivityCacheValidity() < now) {
            return null;
        }

        if (lastConfidence < ActivityRecognitionConstants.MINIMUM_ACTIVITY_CONFIDENCY) {
            return null;
        }

        return new DetectedActivity(lastActivity, lastConfidence);
    }

    /**
     * Gets the current listener for location updates
     *
     * @return
     */
    public OnLocationUpdatedListener getOnLocationUpdatedListener() {
        return onLocationUpdatedListener;
    }

    /**
     * Sets the current listener for location updates
     *
     * @param onLocationUpdatedListener
     */
    public void setOnLocationUpdatedListener(OnLocationUpdatedListener onLocationUpdatedListener) {
        this.onLocationUpdatedListener = onLocationUpdatedListener;
    }

    /**
     * Gets the current listener for status changes
     *
     * @return
     */
    public OnSmartLocationStatusChangedListener getOnSmartLocationStatusChangedListener() {
        return onSmartLocationStatusChangedListener;
    }

    /**
     * Sets the current listener for status changes
     *
     * @param onSmartLocationStatusChangedListener
     */
    public void setOnSmartLocationStatusChangedListener(OnSmartLocationStatusChangedListener onSmartLocationStatusChangedListener) {
        this.onSmartLocationStatusChangedListener = onSmartLocationStatusChangedListener;
    }

    /**
     * Listener for activity and location updates
     */
    public interface OnLocationUpdatedListener {
        public void onLocationUpdated(Location location, DetectedActivity detectedActivity);
    }

    /**
     * Listener for status changes
     */
    public interface OnSmartLocationStatusChangedListener {
        public void onServiceBoundFinished(boolean result);

        public void onServiceConnected();

        public void onServiceDisconnected();

        public void onServiceUnbounded();
    }
}
