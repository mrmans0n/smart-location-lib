package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.Utils;

/**
 * Created by mrm on 20/12/14.
 */
public class GooglePlayServicesLocationProvider implements LocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {
    private static final String PROVIDER_FILE = "GMSPROVIDER_PREFS";
    private static final String LAST_ID = "last";

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Logger logger;
    private SmartLocation.OnLocationUpdatedListener listener;
    private boolean shouldStart = false;
    private SharedPreferences sharedPreferences;

    @Override
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate, Logger logger) {
        if (!shouldStart) {
            this.client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            client.connect();
            this.listener = listener;
            this.locationRequest = createRequest(params, singleUpdate);
            this.logger = logger;

            sharedPreferences = context.getSharedPreferences(PROVIDER_FILE, Context.MODE_PRIVATE);

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
        }

        if (singleUpdate) {
            request.setNumUpdates(1);
        }

        return request;
    }

    @Override
    public void start() {
        if (client.isConnected()) {
            startUpdating();
        } else {
            shouldStart = true;
            logger.d("still not connected - scheduled start when connection is ok");
        }
    }

    private void startUpdating() {
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this).setResultCallback(this);
    }

    @Override
    public void stopUpdates() {
        logger.d("stopUpdates");
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }
        shouldStart = false;
    }

    @Override
    public Location getLastLocation() {
        if (client != null && client.isConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(client);
        }
        Location location = Utils.getLocationFromPreferences(sharedPreferences, LAST_ID);
        if (location != null) {
            return location;
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        // ??
        logger.d("onConnected");
        if (shouldStart) {
            startUpdating();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        logger.d("onConnectionSuspended " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        logger.d("onConnectionFailed");

    }

    @Override
    public void onLocationChanged(Location location) {
        logger.d("onLocationChanged", location);

        listener.onLocationUpdated(location);
        if (sharedPreferences != null) {
            logger.d("Stored in SharedPreferences");
            Utils.storeLocationInPreferences(sharedPreferences, location, LAST_ID);
        }
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            logger.d("Locations update request successful");

        } else if (status.hasResolution()) {
            // TODO this
            logger.d("Unable to register, but we can solve this");
            /*
            status.startResolutionForResult(
                    context,     // your current activity used to receive the result
                    RESULT_CODE); // the result code you'll look for in your
            // onActivityResult method to retry registering
            */
        } else {
            // No recovery. Weep softly or inform the user.
            logger.e("Registering failed: " + status.getStatusMessage());
        }
    }
}
