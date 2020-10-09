package io.nlopez.smartlocation.location.providers.playservices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.Store;
import io.nlopez.smartlocation.location.LocationPermissionsManager;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Location provider that uses Google Play Services.
 */
public class GooglePlayServicesLocationProvider
        implements LocationProvider,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status>,
        GooglePlayServicesLocationSettingsManager.Listener {

    private static final String GMS_ID = "GMS";

    @NonNull
    private final LocationPermissionsManager mPermissionsManager;
    @NonNull
    private final Logger mLogger;
    @NonNull
    private final StatusListener mStatusListener;
    @NonNull
    private final Store<Location> mLocationStore;
    @NonNull
    private final FusedLocationApiProxy mFusedLocationApiProxy;
    @NonNull
    private final GooglePlayServicesLocationSettingsManager mLocationSettingsManager;
    @NonNull
    private Context mContext;
    @Nullable
    private GoogleApiClient mClient;
    @Nullable
    private OnLocationUpdatedListener mListener;
    @Nullable
    private LocationRequest mLocationRequest;

    @Nullable
    private LocationProviderParams mParams;

    public GooglePlayServicesLocationProvider(
            @NonNull Context context,
            @NonNull StatusListener statusListener,
            @NonNull Store<Location> locationStore,
            @NonNull Logger logger,
            @NonNull LocationPermissionsManager permissionsManager) {
        this(context, statusListener, locationStore, logger, permissionsManager, GooglePlayServicesLocationSettingsManager.get(), new FusedLocationApiProxy());
    }

    @VisibleForTesting
    GooglePlayServicesLocationProvider(
            @NonNull Context context,
            @NonNull StatusListener statusListener,
            @NonNull Store<Location> locationStore,
            @NonNull Logger logger,
            @NonNull LocationPermissionsManager permissionsManager,
            @NonNull GooglePlayServicesLocationSettingsManager locationSettingsManager,
            @NonNull FusedLocationApiProxy fusedLocationApiProxy) {
        mContext = context;
        mStatusListener = statusListener;
        mLocationStore = locationStore;
        mLogger = logger;
        mPermissionsManager = permissionsManager;
        mLocationSettingsManager = locationSettingsManager;
        mFusedLocationApiProxy = fusedLocationApiProxy;
        mLocationSettingsManager.setListener(this);
    }

    @Override
    public void start(@NonNull OnLocationUpdatedListener listener, @NonNull LocationProviderParams params) {
        mListener = listener;
        mParams = params;
        mLocationRequest = createRequestFromParams(params);
        mClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mClient.connect();
    }

    private LocationRequest createRequestFromParams(@NonNull LocationProviderParams params) {
        final LocationRequest request = LocationRequest.create()
                .setFastestInterval(params.interval)
                .setInterval(params.interval)
                .setSmallestDisplacement(params.distance);

        switch (params.accuracy) {
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

        if (params.runOnlyOnce) {
            request.setNumUpdates(1);
        }

        return request;
    }

    private void startUpdating(@Nullable LocationRequest request) {
        if (mClient == null) {
            mLogger.e("GoogleApiClient is null, something went very wrong");
            return;
        }
        if (mParams == null) {
            mLogger.e("LocationProviderParms is null, something went very wrong");
        }
        if (mContext == null) {
            mLogger.e("Context is null, something went very wrong");
            return;
        }
        if (mLocationSettingsManager.maybeShowLocationSettingsDialog(mClient, request, mContext)) {
            mLogger.d("Location settings check not successful. Will try to fix if possible.");
            return;
        }
        if (!mPermissionsManager.permissionsEnabledOrRequestPermissions(mContext)) {
            mLogger.d("Permissions were not enabled. If the context was part of an activity, a permission request dialog would have been shown already.");
            return;
        }
        if (!mClient.isConnected()) {
            mLogger.e("GoogleApiClient is not connected. This should not happen.");
            return;
        }
        mFusedLocationApiProxy.requestLocationUpdates(mClient, request, this, Looper.getMainLooper())
                .setResultCallback(this);
    }

    @Override
    public void stop() {
        mLogger.d("stop");
        if (mClient != null && mClient.isConnected()) {
            mFusedLocationApiProxy.removeLocationUpdates(mClient, this);
        }
    }

    @Override
    public Location getLastLocation() {
        if (mContext != null && mClient != null && mClient.isConnected()) {
            if (!mPermissionsManager.permissionsEnabledOrRequestPermissions(mContext)) {
                return null;
            }
            final Location location = mFusedLocationApiProxy.getLastLocation(mClient);
            if (location != null) {
                return location;
            }
        }
        return mLocationStore.get(GMS_ID);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLogger.d("GoogleApiClient onConnected");
        startUpdating(mLocationRequest);
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
    public void onLocationChanged(Location location) {
        mLogger.d("SmartLocation onLocationChanged ", location);

        if (mListener != null) {
            mListener.onLocationUpdated(location);
        }
        mLocationStore.put(GMS_ID, location);
    }

    @Override
    public void release() {
        if (mClient != null && mClient.isConnected()) {
            mClient.disconnect();
        }
        mListener = null;
    }

    @Override
    public void onSettingsSuccess() {
        startUpdating(mLocationRequest);
    }

    @Override
    public void onSettingsFailed() {
        mStatusListener.onProviderFailed(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        mLogger.e("requestLocationUpdates operation failed");
        mStatusListener.onProviderFailed(this);
    }

    static class FusedLocationApiProxy {
        @SuppressLint("MissingPermission")
        PendingResult<Status> requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener, Looper looper) {
            return LocationServices.FusedLocationApi.requestLocationUpdates(client, request, listener, looper);
        }

        void removeLocationUpdates(GoogleApiClient client, LocationListener listener) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, listener);
        }

        @SuppressLint("MissingPermission")
        Location getLastLocation(GoogleApiClient client) {
            return LocationServices.FusedLocationApi.getLastLocation(client);
        }
    }
}