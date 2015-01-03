package io.nlopez.smartlocation.activity.providers;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.ActivityStore;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 3/1/15.
 */
public class GooglePlayServicesActivityProvider implements ActivityProvider, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String GMS_ID = "GMS";
    private static final String BROADCAST_INTENT_ACTION = GooglePlayServicesActivityProvider.class.getCanonicalName() + ".DETECTED_ACTIVITY";
    private static final String DETECTED_ACTIVITY_EXTRA_ID = "activity";

    private GoogleApiClient client;
    private Logger logger;
    private SmartLocation.OnActivityUpdatedListener listener;
    private ActivityStore activityStore;
    private Context context;
    private boolean shouldStart = false;
    private PendingIntent pendingIntent;
    private ActivityParams activityParams;

    @Override
    public void init(@NonNull Context context, Logger logger) {
        this.context = context;
        this.logger = logger;

        activityStore = new ActivityStore(context);

        IntentFilter intentFilter = new IntentFilter(BROADCAST_INTENT_ACTION);
        context.registerReceiver(activityReceiver, intentFilter);

        if (!shouldStart) {
            this.client = new GoogleApiClient.Builder(context)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            client.connect();
        } else {
            logger.d("already started");
        }
    }

    @Override
    public void start(SmartLocation.OnActivityUpdatedListener listener, @NonNull ActivityParams params) {
        this.activityParams = params;
        this.listener = listener;

        if (client.isConnected()) {
            startUpdating(params);
        } else {
            shouldStart = true;
            logger.d("still not connected - scheduled start when connection is ok");
        }
    }

    private void startUpdating(ActivityParams params) {
        pendingIntent = PendingIntent.getService(context, 0, new Intent(context, ActivityRecognitionService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(client, params.getInterval(), pendingIntent);
    }

    @Override
    public void stop() {
        logger.d("stop");
        if (client.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(client, pendingIntent);
            client.disconnect();
        }
        try {
            context.unregisterReceiver(activityReceiver);
        } catch (IllegalArgumentException e) {
            logger.d("Silenced 'receiver not registered' stuff (calling stop more times than necessary did this)");
        }
        shouldStart = false;
    }

    @Override
    public DetectedActivity getLastActivity() {
        if (activityStore != null) {
            return activityStore.get(GMS_ID);
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        logger.d("onConnected");
        if (shouldStart) {
            startUpdating(activityParams);
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

    private void notifyActivity(final DetectedActivity detectedActivity) {
        if (listener != null) {
            listener.onActivityUpdated(detectedActivity);
        }
        if (activityStore != null) {
            activityStore.put(GMS_ID, detectedActivity);
        }
    }


    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BROADCAST_INTENT_ACTION.equals(intent.getAction()) && intent.hasExtra(DETECTED_ACTIVITY_EXTRA_ID)) {
                logger.d("sending new activity");
                DetectedActivity detectedActivity = intent.getParcelableExtra(DETECTED_ACTIVITY_EXTRA_ID);
                notifyActivity(detectedActivity);
            }
        }
    };

    public static class ActivityRecognitionService extends IntentService {

        public ActivityRecognitionService() {
            super(ActivityRecognitionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();

                // Broadcast an intent containing the activity
                Intent activityIntent = new Intent(BROADCAST_INTENT_ACTION);
                activityIntent.putExtra(DETECTED_ACTIVITY_EXTRA_ID, mostProbableActivity);
                sendBroadcast(activityIntent);
            }
        }
    }

}
