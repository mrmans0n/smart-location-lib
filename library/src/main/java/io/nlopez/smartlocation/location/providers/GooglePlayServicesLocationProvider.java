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
import io.nlopez.smartlocation.location.LocationAccuracy;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public class GooglePlayServicesLocationProvider implements LocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Logger logger;
    private SmartLocation.OnLocationUpdatedListener listener;
    private boolean shouldStart = false;

    @Override
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, boolean oneFix,
                     LocationAccuracy accuracy, Logger logger) {
        if (!shouldStart) {
            this.client = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            client.connect();
            this.listener = listener;

            // TODO handle accuracy
            this.locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            if (oneFix) {
                this.locationRequest.setNumUpdates(1);
            }

            this.logger = logger;
        } else {
            logger.d("already started");
        }
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
        return client.isConnected() ? LocationServices.FusedLocationApi.getLastLocation(client) : null;
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
        listener.onLocationUpdated(location);
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
