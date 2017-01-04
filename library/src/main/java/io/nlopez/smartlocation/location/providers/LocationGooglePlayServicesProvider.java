package io.nlopez.smartlocation.location.providers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.config.ScheduledOnLocationUpdateListener;
import io.nlopez.smartlocation.utils.GooglePlayServicesListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public class LocationGooglePlayServicesProvider implements LocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    public static final int REQUEST_START_LOCATION_FIX = 10001;
    public static final int REQUEST_CHECK_SETTINGS = 20001;
    private static final String GMS_ID = "GMS";

    private GoogleApiClient client;
    private Logger logger;
    private Map<OnLocationUpdatedListener, Long> listenersIntervals = new HashMap<>();
    private int locationUpdateCounter = 0;
    private boolean shouldStart = false;
    private boolean stopped = false;
    private LocationStore locationStore;
    private LocationRequest locationRequest;
    boolean singleUpdate = true;
    private Context context;
    private final GooglePlayServicesListener googlePlayServicesListener;
    private boolean checkLocationSettings;
    private boolean fulfilledCheckLocationSettings;
    private boolean alwaysShow = true;

    public LocationGooglePlayServicesProvider() {
        this(null);
    }

    public LocationGooglePlayServicesProvider(GooglePlayServicesListener playServicesListener) {
        googlePlayServicesListener = playServicesListener;
        checkLocationSettings = false;
        fulfilledCheckLocationSettings = false;
    }

    @Override
    public void init(Context context, Logger logger) {
        this.logger = logger;
        this.context = context;

        locationStore = new LocationStore(context);

        if (!shouldStart) {
            this.client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            client.connect();
        } else {
            logger.d("already started");
        }
    }

    private void setMoreEffortRequest(LocationParams params, boolean singleUpdate) {
        long mostEffortInterval;
        float mostEffortDistance;
        int mostEffortPriority;

        int priorityFromParams = convertToPriority(params.getAccuracy());

        if (locationRequest == null){
            // First time the request is being built.
            mostEffortInterval = params.getInterval();
            mostEffortDistance = params.getDistance();
            mostEffortPriority = priorityFromParams;
        } else {
            // Reconfigure location request.
            if (singleUpdate) {
                mostEffortInterval = locationRequest.getInterval();
            } else {
                mostEffortInterval = Math.min(locationRequest.getInterval(),
                        params.getInterval());
            }
            mostEffortDistance = Math.min(locationRequest.getSmallestDisplacement(),
                    params.getDistance());
            // Lower int value of priority is used for higher priority.
            mostEffortPriority = Math.min(locationRequest.getPriority(), priorityFromParams);
        }

        if (!singleUpdate) {
            this.singleUpdate = false;
        }
        locationRequest = buildRequest(mostEffortInterval, mostEffortDistance,
                mostEffortPriority, this.singleUpdate);
    }

    private static LocationRequest buildRequest(long interval, float distance, int priority,
                                         boolean singleUpdate) {
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(interval)
                .setInterval(interval)
                .setSmallestDisplacement(distance)
                .setPriority(priority);

        if (singleUpdate) {
            request.setNumUpdates(1);
        }

        return request;
    }

    private static int convertToPriority(LocationAccuracy locationAccuracy) {
        switch (locationAccuracy) {
            case HIGH:
                return LocationRequest.PRIORITY_HIGH_ACCURACY;
            case MEDIUM:
                return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            case LOW:
                return LocationRequest.PRIORITY_LOW_POWER;
            case LOWEST:
                return LocationRequest.PRIORITY_NO_POWER;
            default:
                return -1;
        }
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.listenersIntervals = new HashMap<>();
        addListener(listener, params, singleUpdate);
    }

    @Override
    public void start(List<ScheduledOnLocationUpdateListener> scheduledListeners) {
        for (ScheduledOnLocationUpdateListener scheduledListener : scheduledListeners) {
            addListenerSoftly(scheduledListener.getListener(), scheduledListener.getParams(),
                    scheduledListener.isSingleUpdate());
        }
        restartUpdating();
    }

    @Override
    public void addListener(OnLocationUpdatedListener listener, LocationParams params,
                            boolean singleUpdate) {
        addListenerSoftly(listener, params, singleUpdate);
        restartUpdating();
    }

    private void addListenerSoftly(OnLocationUpdatedListener listener, LocationParams params,
                                   boolean singleUpdate) {
        if (listener == null) {
            logger.d("Listener is null, you sure about this?");
        } else {
            addListenerIntervalSoftly(listener, params.getInterval(), singleUpdate);
        }

        setMoreEffortRequest(params, singleUpdate);
    }

    private void addListenerIntervalSoftly(OnLocationUpdatedListener listener, long interval,
                                           boolean singleUpdate) {
        if (singleUpdate) {
            interval = -1L;
        }
        listenersIntervals.put(listener, interval);
    }

    private void restartUpdating() {
        if (client.isConnected()) {
            startUpdating(locationRequest);
        } else if (stopped) {
            shouldStart = true;
            client.connect();
            stopped = false;
        } else {
            shouldStart = true;
            logger.d("still not connected - scheduled start when connection is ok");
        }
    }

    @Override
    public boolean removeListener(OnLocationUpdatedListener listener) {
        return listenersIntervals.remove(listener) != null;
    }

    private void startUpdating(LocationRequest request) {
        // TODO wait until the connection is done and retry
        if (checkLocationSettings && !fulfilledCheckLocationSettings) {
            logger.d("startUpdating wont be executed for now, as we have to test the location settings before");
            checkLocationSettings();
            return;
        }
        if (client.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                logger.i("Permission check failed. Please handle it in your app before setting up location");
                // TODO: Consider calling ActivityCompat#requestPermissions here to request the
                // missing permissions, and then overriding onRequestPermissionsResult
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationUpdateCounter = 0;
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this, Looper.getMainLooper()).setResultCallback(this);
        } else {
            logger.w("startUpdating executed without the GoogleApiClient being connected!!");
        }
    }

    private void checkLocationSettings() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().setAlwaysShow(alwaysShow).addLocationRequest(locationRequest).build();
        LocationServices.SettingsApi.checkLocationSettings(client, request).setResultCallback(settingsResultCallback);
    }

    @Override
    public void stop() {
        logger.d("stop");
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }
        fulfilledCheckLocationSettings = false;
        shouldStart = false;
        stopped = true;
    }

    @Override
    public Location getLastLocation() {
        if (client != null && client.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location location = LocationServices.FusedLocationApi.getLastLocation(client);
            if (location != null) {
                return location;
            }
        }
        if (locationStore != null) {
            return locationStore.get(GMS_ID);
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        logger.d("onConnected");
        if (shouldStart) {
            startUpdating(locationRequest);
        }
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        logger.d("onConnectionSuspended " + i);
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        logger.d("onConnectionFailed " + connectionResult.toString());
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionFailed(connectionResult);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        logger.d("onLocationChanged", location);

        locationUpdateCounter++;
        long currentInterval = locationRequest.getInterval();

        List<OnLocationUpdatedListener> singleUpdateListeners = new ArrayList<>();
        for (Map.Entry<OnLocationUpdatedListener, Long> entry : listenersIntervals.entrySet()) {
            OnLocationUpdatedListener listener = entry.getKey();
            long listenerInterval = entry.getValue();
            boolean singleUpdate = listenerInterval == -1L;

            if (singleUpdate) {
                listener.onLocationUpdated(location);
                singleUpdateListeners.add(listener);
            } else if (currentInterval == 0) {
                listener.onLocationUpdated(location);
            } else {
                boolean timeToUpdate = locationUpdateCounter % (listenerInterval / currentInterval) == 0;

                if (timeToUpdate) {
                    listener.onLocationUpdated(location);
                }
            }
        }

        for (OnLocationUpdatedListener singleUpdateListener : singleUpdateListeners) {
            removeListener(singleUpdateListener);
        }

        if (locationStore != null) {
            logger.d("Stored in SharedPreferences");
            locationStore.put(GMS_ID, location);
        }
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            logger.d("Locations update request successful");

        } else if (status.hasResolution() && context instanceof Activity) {
            logger.w(
                    "Unable to register, but we can solve this - will startActivityForResult. You should hook into the Activity onActivityResult and call this provider onActivityResult method for continuing this call flow.");
            try {
                status.startResolutionForResult((Activity) context, REQUEST_START_LOCATION_FIX);
            } catch (IntentSender.SendIntentException e) {
                logger.e(e, "problem with startResolutionForResult");
            }
        } else {
            // No recovery. Weep softly or inform the user.
            logger.e("Registering failed: " + status.getStatusMessage());
        }
    }

    /**
     * @return TRUE if active, FALSE if the settings wont be checked before launching the location updates request
     */
    public boolean isCheckingLocationSettings() {
        return checkLocationSettings;
    }

    /**
     * Sets whether or not we should request (before starting updates) the availability of the
     * location settings and act upon it.
     *
     * @param allowingLocationSettings TRUE to show the dialog if needed, FALSE otherwise (default)
     */
    public void setCheckLocationSettings(boolean allowingLocationSettings) {
        this.checkLocationSettings = allowingLocationSettings;
    }


    /**
     * Sets whether or not we should show location settings dialog with NEVER button
     *
     * @param alwaysShow TRUE to show dialog without NEVER button, FALSE - with NEVER button (default)
     */
    public void setLocationSettingsAlwaysShow(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    /**
     * This method should be called in the onActivityResult of the calling activity whenever we are
     * trying to implement the Check Location Settings fix dialog.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    logger.i("User agreed to make required location settings changes.");
                    fulfilledCheckLocationSettings = true;
                    startUpdating(locationRequest);
                    break;
                case Activity.RESULT_CANCELED:
                    logger.i("User chose not to make required location settings changes.");
                    stop();
                    break;
            }
        } else if (requestCode == REQUEST_START_LOCATION_FIX) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    logger.i("User fixed the problem.");
                    startUpdating(locationRequest);
                    break;
                case Activity.RESULT_CANCELED:
                    logger.i("User chose not to fix the problem.");
                    stop();
                    break;
            }
        }
    }

    private ResultCallback<LocationSettingsResult> settingsResultCallback = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult locationSettingsResult) {
            final Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    logger.d("All location settings are satisfied.");
                    fulfilledCheckLocationSettings = true;
                    startUpdating(locationRequest);
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    logger.w("Location settings are not satisfied. Show the user a dialog to" +
                            "upgrade location settings. You should hook into the Activity onActivityResult and call this provider onActivityResult method for continuing this call flow. ");

                    if (context instanceof Activity) {
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            logger.i("PendingIntent unable to execute request.");
                        }

                    } else {
                        logger.w("Provided context is not the context of an activity, therefore we cant launch the resolution activity.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    logger.i("Location settings are inadequate, and cannot be fixed here. Dialog " +
                            "not created.");
                    stop();
                    break;
            }
        }
    };

}
