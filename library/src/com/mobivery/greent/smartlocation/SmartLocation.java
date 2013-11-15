package com.mobivery.greent.smartlocation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

    public static final String DEFAULT_PACKAGE = "com.mobivery.smartlocation.greent";

    /**
     * Detected location key for the intent bundle. Returns a Location object.
     */
    public static final String DETECTED_LOCATION_KEY = "LOCATION";

    /**
     * Detected activity for the intent bundle. Returns a DetectedActivity integer.
     */
    public static final String DETECTED_ACTIVITY_KEY = "ACTIVITY";

    private boolean isServiceBound = false;
    private boolean isServiceConnected = false;
    private SmartLocationService boundService;
    private SmartLocationOptions smartLocationOptions;

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
     * Initializes the smart location
     *
     * @param context
     */
    public void start(Context context) {
        start(context, new SmartLocationOptions());
    }

    /**
     * Initializes the smart location with custom options
     *
     * @param context
     */
    public void start(Context context, SmartLocationOptions options) {
        setOptions(options);
        if (isServiceBound) {
            boundService.startLocation(smartLocationOptions);
        } else {
            bindService(context);
        }
    }

    /**
     * Stops the location retrival
     *
     * @param context
     */
    public void stop(Context context) {
        if (isServiceConnected) {
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

        if (options == null)
            throw new IllegalArgumentException("options value can't be null");

        smartLocationOptions = options;
        if (isServiceConnected) {
            boundService.setOptions(options);
        }
    }

    private boolean bindService(Context context) {
        if (!isServiceBound) {
            Intent serviceIntent = new Intent(context, SmartLocationService.class);
            isServiceBound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        return isServiceBound;
    }

    private void unbindService(Context context) {
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection);
                isServiceBound = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            createServiceConnection(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            destroyServiceConnection();
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

}
