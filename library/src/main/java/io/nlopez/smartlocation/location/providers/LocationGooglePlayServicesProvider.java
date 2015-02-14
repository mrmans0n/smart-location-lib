package io.nlopez.smartlocation.location.providers;

import android.app.Activity;
import android.content.Context;
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

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.GooglePlayServicesListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public class LocationGooglePlayServicesProvider implements LocationProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    public static final int RESULT_CODE = 10001;
    private static final String GMS_ID = "GMS";

    private GoogleApiClient client;
    private Logger logger;
    private OnLocationUpdatedListener listener;
    private boolean shouldStart = false;
    private LocationStore locationStore;
    private LocationRequest locationRequest;
    private Context context;
    private final GooglePlayServicesListener googlePlayServicesListener;

    public LocationGooglePlayServicesProvider() {
        this(null);
    }

    public LocationGooglePlayServicesProvider(GooglePlayServicesListener playServicesListener) {
        googlePlayServicesListener = playServicesListener;
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
        } else {
            shouldStart = true;
            logger.d("still not connected - scheduled start when connection is ok");
        }
    }

    private void startUpdating(LocationRequest request) {
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this).setResultCallback(this);
        } else {
            logger.w("startUpdated executed without the GoogleApiClient being connected!!");
        }
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
        logger.d("onConnectionFailed");
        if (googlePlayServicesListener != null) {
            googlePlayServicesListener.onConnectionFailed(connectionResult);
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
                    "Unable to register, but we can solve this - will startActivityForResult expecting result code " + RESULT_CODE + " (if received, please try again)");

            try {
                status.startResolutionForResult((Activity) context, RESULT_CODE);
            } catch (IntentSender.SendIntentException e) {
                logger.e(e, "problem with startResolutionForResult");
            }
        } else {
            // No recovery. Weep softly or inform the user.
            logger.e("Registering failed: " + status.getStatusMessage());
        }
    }

}
