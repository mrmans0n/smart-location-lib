package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by mrm on 20/12/14.
 */
public class GooglePlayServicesLocationProvider implements LocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    private static final String TAG = GooglePlayServicesLocationProvider.class.getSimpleName();
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private boolean loggingEnabled;
    private SmartLocation.OnLocationUpdatedListener listener;
    private LocationProviderCallback callback;

    @Override
    public void init(Context context, LocationProviderCallback callback, SmartLocation.OnLocationUpdatedListener listener, LocationStrategy strategy, boolean loggingEnabled) {
        this.client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.listener = listener;
        this.callback = callback;

        this.locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        this.loggingEnabled = loggingEnabled;
    }

    @Override
    public void startForRecurrence(LocationRecurrence recurrence) {
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this).setResultCallback(this);
    }

    @Override
    public void stopUpdates() {
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public Location getLastLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(client);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // ??
    }

    @Override
    public void onConnectionSuspended(int i) {
        callback.onProviderError();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        callback.onProviderError();
    }

    @Override
    public void onLocationChanged(Location location) {
        listener.onLocationUpdated(location);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Google Api Client connection successful");
            callback.onProviderReady();
        } else if (status.hasResolution()) {
            callback.onProviderError();
            // TODO this
            Log.e(TAG, "Unable to register, but we can solve this");
            /*
            status.startResolutionForResult(
                    context,     // your current activity used to receive the result
                    RESULT_CODE); // the result code you'll look for in your
            // onActivityResult method to retry registering
            */
        } else {
            // No recovery. Weep softly or inform the user.
            Log.e(TAG, "Registering failed: " + status.getStatusMessage());
            callback.onProviderError();
        }
    }
}
