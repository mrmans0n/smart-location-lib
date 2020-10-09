package io.nlopez.smartlocation.geofencing.providers.playservices;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.geofencing.GeofencingProvider;
import io.nlopez.smartlocation.location.LocationPermissionsManager;
import io.nlopez.smartlocation.location.providers.playservices.GooglePlayServicesLocationSettingsManager;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Geofencing provider that uses Google Play Services.
 */
public class GooglePlayServicesGeofencingProvider
        implements GeofencingProvider,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GooglePlayServicesLocationSettingsManager.Listener,
        OnSuccessListener<Void>,
        OnFailureListener {

    @NonNull private final LocationPermissionsManager mPermissionsManager;
    @NonNull private final Logger mLogger;
    @NonNull private final Provider.StatusListener mStatusListener;
    @NonNull private final GeofencingApiProxy mApiProxy;
    @NonNull private Context mContext;
    @Nullable private GoogleApiClient mClient;
    @NonNull private LinkedList<Runnable> mActionQueue;

    public GooglePlayServicesGeofencingProvider(
            @NonNull Context context,
            @NonNull Provider.StatusListener statusListener,
            @NonNull Logger logger,
            @NonNull LocationPermissionsManager permissionsManager) {
        this(context, statusListener, logger, permissionsManager, new GeofencingApiProxy());
    }

    @VisibleForTesting
    GooglePlayServicesGeofencingProvider(
            @NonNull Context context,
            @NonNull Provider.StatusListener statusListener,
            @NonNull Logger logger,
            @NonNull LocationPermissionsManager permissionsManager,
            @NonNull GeofencingApiProxy apiProxy) {
        mActionQueue = new LinkedList<>();
        mContext = context;
        mStatusListener = statusListener;
        mLogger = logger;
        mPermissionsManager = permissionsManager;
        mApiProxy = apiProxy;
    }

    @Override
    public void addGeofences(@NonNull GeofencingRequest request, @NonNull PendingIntent pendingIntent) {
        initGoogleApiClient();
        runOrEnqueueAction(new AddGeofencesAction(request, pendingIntent));
    }

    @Override
    public void removeGeofences(@NonNull List<String> geofenceIds) {
        initGoogleApiClient();
        runOrEnqueueAction(new RemoveGeofencesIdListAction(geofenceIds));
    }

    @Override
    public void removeGeofences(@NonNull PendingIntent pendingIntent) {
        initGoogleApiClient();
        runOrEnqueueAction(new RemoveGeofencesIntentListAction(pendingIntent));
    }

    private void initGoogleApiClient() {
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        if (!mClient.isConnected()) {
            mClient.connect();
        }
    }

    private boolean validatePlayServices() {
        if (mClient == null) {
            mLogger.e("GoogleApiClient is null, something went very wrong");
            return false;
        }
        if (mContext == null) {
            mLogger.e("Context is null, something went very wrong");
            return false;
        }
        if (!mPermissionsManager.permissionsEnabledOrRequestPermissions(mContext)) {
            mLogger.d("Permissions were not enabled. If the context was part of an activity, a permission request dialog would have been shown already.");
            return false;
        }
        if (!mClient.isConnected()) {
            mLogger.e("GoogleApiClient is not connected. This should not happen.");
            return false;
        }
        return true;
    }

    private void runOrEnqueueAction(@NonNull Runnable action) {
        if (mClient != null && mClient.isConnected()) {
            action.run();
        } else {
            mActionQueue.add(action);
        }
    }

    private void runEnqueuedActions() {
        if (validatePlayServices()) {
            // Run scheduled actions to happen when the client is connected
            while (!mActionQueue.isEmpty()) {
                final Runnable action = mActionQueue.poll();
                action.run();
            }
        } else {
            mLogger.e("Did not run stored actions due to validation failure.");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLogger.d("GoogleApiClient onConnected");
        runEnqueuedActions();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mLogger.d("GoogleApiClient onConnectionSuspended " + i);
        mStatusListener.onProviderFailed(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mLogger.d("GoogleApiClient onConnectionFailed " + connectionResult.toString());
        mStatusListener.onProviderFailed(this);
    }

    @Override
    public void release() {
        if (mClient != null && mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    public void onSettingsSuccess() {
        runEnqueuedActions();
    }

    @Override
    public void onSettingsFailed() {
        mStatusListener.onProviderFailed(this);
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        mLogger.e("GeofencingClient  operation failed", e);
        mStatusListener.onProviderFailed(this);
    }

    @Override
    public void onSuccess(Void aVoid) {
        mLogger.d("GeofencingClient onSuccess");
    }

    class AddGeofencesAction implements Runnable {
        private final GeofencingRequest mRequest;
        private final PendingIntent mPendingIntent;

        AddGeofencesAction(
                @NonNull GeofencingRequest request,
                @NonNull PendingIntent pendingIntent) {
            mRequest = request;
            mPendingIntent = pendingIntent;
        }

        @Override
        public void run() {
            mApiProxy.addGeofences(
                    mContext,
                    mRequest,
                    mPendingIntent,
                    GooglePlayServicesGeofencingProvider.this,
                    GooglePlayServicesGeofencingProvider.this);
        }
    }

    class RemoveGeofencesIdListAction implements Runnable {
        private final List<String> mGeofenceIds;

        RemoveGeofencesIdListAction(@NonNull List<String> geofenceIds) {
            mGeofenceIds = geofenceIds;
        }

        @Override
        public void run() {
            mApiProxy.removeGeofences(
                    mContext,
                    mGeofenceIds,
                    GooglePlayServicesGeofencingProvider.this,
                    GooglePlayServicesGeofencingProvider.this);
        }
    }

    class RemoveGeofencesIntentListAction implements Runnable {
        private final PendingIntent mPendingIntent;

        RemoveGeofencesIntentListAction(@NonNull PendingIntent pendingIntent) {
            mPendingIntent = pendingIntent;
        }

        @Override
        public void run() {
            mApiProxy.removeGeofences(
                    mContext,
                    mPendingIntent,
                    GooglePlayServicesGeofencingProvider.this,
                    GooglePlayServicesGeofencingProvider.this);
        }
    }

    static class GeofencingApiProxy {
        @SuppressLint("MissingPermission")
        void addGeofences(
                @NonNull Context context,
                @NonNull GeofencingRequest request,
                @NonNull PendingIntent pendingIntent,
                @NonNull OnSuccessListener<Void> successListener,
                @NonNull OnFailureListener failureListener) {
            LocationServices.getGeofencingClient(context)
                    .addGeofences(request, pendingIntent)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        }

        void removeGeofences(
                @NonNull Context context,
                @NonNull List<String> geofenceIds,
                @NonNull OnSuccessListener<Void> successListener,
                @NonNull OnFailureListener failureListener) {
            LocationServices.getGeofencingClient(context)
                    .removeGeofences(geofenceIds)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        }

        void removeGeofences(
                @NonNull Context context,
                @NonNull PendingIntent pendingIntent,
                @NonNull OnSuccessListener<Void> successListener,
                @NonNull OnFailureListener failureListener) {
            LocationServices.getGeofencingClient(context)
                    .removeGeofences(pendingIntent)
                    .addOnSuccessListener(successListener)
                    .addOnFailureListener(failureListener);
        }
    }
}