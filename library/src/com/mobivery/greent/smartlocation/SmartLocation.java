package com.mobivery.greent.smartlocation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by MVY11 on 17/06/13.
 */
public class SmartLocation {

    /**
     * Intent action name. The rest of the package can be configured.
     * The default action name is com.mobivery.smartlocation.LOCATION_UPDATED
     */
    public static final String LOCATION_BROADCAST_INTENT_TRAIL = ".LOCATION_UPDATED";

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
        start(context, null);
    }

    /**
     * Initializes the smart location with custom options
     *
     * @param context
     */
    public void start(Context context, SmartLocationOptions options) {
        smartLocationOptions = options;
        bindService(context);
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

    private boolean bindService(Context context) {
        if (!isServiceBound) {
            Intent serviceIntent = new Intent(context, SmartLocationService.class);
            isServiceBound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        return isServiceBound;
    }

    private void unbindService(Context context) {
        if (isServiceBound) {
            context.unbindService(serviceConnection);
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

        String packageName = null;
        if (smartLocationOptions != null) {
            packageName = smartLocationOptions.getPackageName();
        }
        boundService.startLocation(packageName);
    }

    private void destroyServiceConnection() {
        boundService.stopLocation();
        isServiceConnected = false;
    }

}
