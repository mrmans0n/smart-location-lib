package io.nlopez.smartlocation.geofencing.providers;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.geofencing.GeofencingProvider;
import io.nlopez.smartlocation.geofencing.GeofencingStore;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 3/1/15.
 */
public class GeofencingGooglePlayServicesProvider implements GeofencingProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    public static final int RESULT_CODE = 10003;

    private static final String GMS_ID = "GMS";
    private static final String BROADCAST_INTENT_ACTION = GeofencingGooglePlayServicesProvider.class.getCanonicalName() + ".GEOFENCE_TRANSITION";
    private static final String GEOFENCES_EXTRA_ID = "geofences";

    private List<Geofence> geofencesToAdd = new ArrayList<>();
    private List<String> geofencesToRemove = new ArrayList<>();

    private GoogleApiClient client;
    private Logger logger;
    private OnGeofencingTransitionListener listener;
    private GeofencingStore geofencingStore;
    private Context context;
    private PendingIntent pendingIntent;

    @Override
    public void init(@NonNull Context context, Logger logger) {
        this.context = context;
        this.logger = logger;

        geofencingStore = new GeofencingStore(context);

        IntentFilter intentFilter = new IntentFilter(BROADCAST_INTENT_ACTION);
        context.registerReceiver(activityReceiver, intentFilter);

        this.client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        client.connect();
    }

    @Override
    public void addGeofence(GeofenceModel geofence) {
        geofencingStore.put(geofence.getRequestId(), geofence);

        if (client.isConnected()) {
            List<Geofence> geofenceList = new ArrayList<>();
            geofenceList.add(geofence.toGeofence());
            LocationServices.GeofencingApi.addGeofences(client, geofenceList, pendingIntent);
        } else {
            geofencesToAdd.add(geofence.toGeofence());
        }

    }

    @Override
    public void removeGeofence(String geofenceId) {
        geofencingStore.remove(geofenceId);

        if (client.isConnected()) {
            List<String> geofenceIdList = new ArrayList<>();
            geofenceIdList.add(geofenceId);
            LocationServices.GeofencingApi.removeGeofences(client, geofenceIdList);
        } else {
            geofencesToRemove.add(geofenceId);
        }
    }

    @Override
    public void start(OnGeofencingTransitionListener listener) {
        this.listener = listener;

        if (client.isConnected()) {
            //startUpdating();
        } else {
            logger.d("still not connected - scheduled start when connection is ok");
        }
    }

    /*
    private void startUpdating() {
        pendingIntent = PendingIntent.getService(context, 0, new Intent(context, ActivityRecognitionService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(client, params.getInterval(), pendingIntent).setResultCallback(this);
    }
    */

    @Override
    public void stop() {
        logger.d("stop");
        if (client.isConnected()) {
            client.disconnect();
        }
        try {
            context.unregisterReceiver(activityReceiver);
        } catch (IllegalArgumentException e) {
            logger.d("Silenced 'receiver not registered' stuff (calling stop more times than necessary did this)");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        logger.d("onConnected");
        // startUpdating();
        if (geofencesToAdd.size() > 0) {
            LocationServices.GeofencingApi.addGeofences(client, geofencesToAdd, pendingIntent);
            geofencesToAdd.clear();
        }

        if (geofencesToRemove.size() > 0) {
            LocationServices.GeofencingApi.removeGeofences(client, geofencesToRemove);
            geofencesToRemove.clear();
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

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BROADCAST_INTENT_ACTION.equals(intent.getAction()) && intent.hasExtra(GEOFENCES_EXTRA_ID)) {
                logger.d("sending new activity");
                //DetectedActivity detectedActivity = intent.getParcelableExtra(DETECTED_ACTIVITY_EXTRA_ID);
                //notifyActivity(detectedActivity);
            }
        }
    };

    public static class GeofencingService extends IntentService {

        public GeofencingService() {
            super(GeofencingService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            // TODO this - http://developer.android.com/training/location/geofencing.html
        }
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            logger.d("Geofencing update request successful");
        } else if (status.hasResolution() && context instanceof Activity) {
            logger.w("Unable to register, but we can solve this - will startActivityForResult expecting result code " + RESULT_CODE + " (if received, please try again)");

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
