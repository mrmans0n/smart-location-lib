package io.nlopez.smartlocation.location.providers;

import android.content.Context;
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
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public class GooglePlayServicesLocationProvider implements LocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {
    private static final String GMS_ID = "GMS";

    private GoogleApiClient client;
    private Logger logger;
    private SmartLocation.OnLocationUpdatedListener listener;
    private boolean shouldStart = false;
    private LocationStore locationStore;
    private LocationRequest locationRequest;

    @Override
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, Logger logger) {
        if (!shouldStart) {
            this.client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            client.connect();
            this.listener = listener;
            this.logger = logger;

            locationStore = new LocationStore(context);

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
    public void start(LocationParams params, boolean singleUpdate) {
        locationRequest = createRequest(params, singleUpdate);
        if (client.isConnected()) {
            startUpdating(locationRequest);
        } else {
            shouldStart = true;
            logger.d("still not connected - scheduled start when connection is ok");
        }
    }

    private void startUpdating(LocationRequest request) {
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this).setResultCallback(this);
    }

    @Override
    public void stop() {
        logger.d("stop");
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
        if (locationStore != null) {
            Location location = locationStore.get(GMS_ID);
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        // ??
        logger.d("onConnected");
        if (shouldStart) {
            startUpdating(locationRequest);
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
        if (locationStore != null) {
            logger.d("Stored in SharedPreferences");
            locationStore.put(GMS_ID, location);
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
