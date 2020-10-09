package io.nlopez.smartlocation.location.providers.legacy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.Store;
import io.nlopez.smartlocation.location.LocationPermissionsManager;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Location provider that uses Android's LocationManager as a source for updates
 */
public class LocationManagerProvider implements LocationProvider, LocationListener {
    private static final String LOCATIONMANAGERPROVIDER_ID = "LMP";
    @NonNull
    private final StatusListener mStatusListener;
    @NonNull
    private final LocationPermissionsManager mPermissionsManager;

    private LocationManager mLocationManager;
    private OnLocationUpdatedListener mListener;
    private Store<Location> mLocationStore;
    private Logger mLogger;
    private Context mContext;

    public LocationManagerProvider(
            @NonNull Context context,
            @NonNull StatusListener statusListener,
            @NonNull Store<Location> locationStore,
            @NonNull Logger logger,
            @NonNull LocationPermissionsManager permissionsManager) {
        mContext = context;
        mStatusListener = statusListener;
        mLocationStore = locationStore;
        mLogger = logger;
        mPermissionsManager = permissionsManager;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start(@NonNull OnLocationUpdatedListener listener, @NonNull LocationProviderParams params) {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            mStatusListener.onProviderFailed(this);
            return;
        }
        mListener = listener;
        if (!mPermissionsManager.permissionsEnabledOrRequestPermissions(mContext)) {
            mLogger.d("Permissions were not enabled. If the context was part of an activity, a permission request dialog would have been shown already.");
            return;

        }
        final Criteria criteria = getProvider(params);

        if (params.runOnlyOnce) {
            mLocationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());
        } else {
            mLocationManager.requestLocationUpdates(
                    params.interval, params.distance, criteria, this, Looper.getMainLooper());
        }
    }

    @Override
    public void stop() {
        mLocationManager.removeUpdates(this);
    }

    @Override
    public Location getLastLocation() {
        if (mLocationManager != null) {
            if (!mPermissionsManager.permissionsEnabledOrRequestPermissions(mContext)) {
                return null;
            }

            @SuppressLint("MissingPermission") final Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                return location;
            }
        }
        return mLocationStore.get(LOCATIONMANAGERPROVIDER_ID);
    }

    private Criteria getProvider(LocationProviderParams params) {
        final LocationAccuracy accuracy = params.accuracy;
        final Criteria criteria = new Criteria();
        switch (accuracy) {
            case HIGH:
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                break;
            case MEDIUM:
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setBearingAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                break;
            case LOW:
            case LOWEST:
            default:
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_LOW);
                criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_LOW);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
        return criteria;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLogger.d("onLocationChanged", location);
        if (mListener != null) {
            mListener.onLocationUpdated(location);
        }
        mLocationStore.put(LOCATIONMANAGERPROVIDER_ID, location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void release() {
        mListener = null;
    }
}
