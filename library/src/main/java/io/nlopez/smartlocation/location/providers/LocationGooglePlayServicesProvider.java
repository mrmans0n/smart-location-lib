package io.nlopez.smartlocation.location.providers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.location.ServiceLocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.GooglePlayServicesListener;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.ServiceConnectionListener;

/**
 * Created by mrm on 20/12/14.
 */
public class LocationGooglePlayServicesProvider implements ServiceLocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    public static final int REQUEST_START_LOCATION_FIX = 10001;
    public static final int REQUEST_CHECK_SETTINGS = 20001;
    private static final String GMS_ID = "GMS";

    private GoogleApiClient client;
    private Logger logger;
    private OnLocationUpdatedListener listener;
    private boolean shouldStart = false;
    private boolean stopped = false;
    private LocationStore locationStore;
    private LocationRequest locationRequest;
    private Context context;
    private GooglePlayServicesListener googlePlayServicesListener;
    private ServiceConnectionListener serviceListener;
    private boolean checkLocationSettings;
    private boolean fulfilledCheckLocationSettings;
    private boolean alwaysShow = true;

    public LocationGooglePlayServicesProvider() {
        checkLocationSettings =  false;
        fulfilledCheckLocationSettings = false;
    }

    public LocationGooglePlayServicesProvider(GooglePlayServicesListener playServicesListener) {
        this();
        googlePlayServicesListener = playServicesListener;
    }

    public LocationGooglePlayServicesProvider(ServiceConnectionListener serviceListener) {
        this();
        this.serviceListener = serviceListener;
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

    private LocationRequest createRequest(LocationParams params, boolean singleUpdate) {
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(params.getInterval())
                .setInterval(params.getInterval())
                .setSmallestDisplacement(params.getDistance());

        switch (params.getAccuracy()) {
            case HIGH:
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            case MEDIUM:
                request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case LOW:
                request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            case LOWEST:
                request.setPriority(LocationRequest.PRIORITY_NO_POWER);
                break;
        }

        if (singleUpdate) {
            request.setNumUpdates(1);
        }

        return request;
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.listener = listener;
        if (listener == null) {
            logger.d("Listener is null, you sure about this?");
        }
        locationRequest = createRequest(params, singleUpdate);

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

    private void startUpdating(LocationRequest request) {
        // TODO wait until the connection is done and retry
        if (checkLocationSettings && !fulfilledCheckLocationSettings) {
            logger.d("startUpdating wont be executed for now, as we have to test the location settings before");
            checkLocationSettings();
            return;
        }
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this).setResultCallback(this);
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
            Location location =  LocationServices.FusedLocationApi.getLastLocation(client);
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
    public ServiceConnectionListener getServiceListener() {
        return serviceListener;
    }

    @Override
    public void setServiceListener(ServiceConnectionListener listener) {
        serviceListener = listener;
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
        if (serviceListener != null) {
            serviceListener.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        logger.d("onConnectionSuspended " + i);
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionSuspended(i);
        }
        if (serviceListener != null) {
            serviceListener.onConnectionSuspended();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        logger.d("onConnectionFailed " + connectionResult.toString());
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionFailed(connectionResult);
        }
        if (serviceListener != null) {
            serviceListener.onConnectionFailed();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        logger.d("onLocationChanged", location);

        if (listener != null) {
            listener.onLocationUpdated(location);
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
