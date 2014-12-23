package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationAccuracy;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by nacho on 12/23/14.
 */
public class LocationManagerProvider implements LocationProvider, LocationListener {

    private LocationManager locationManager;
    private String provider;
    private boolean oneFix;
    private SmartLocation.OnLocationUpdatedListener listener;

    @Override
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, boolean oneFix,
                     LocationAccuracy accuracy, Logger loggingEnabled) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.provider = getProvider(accuracy);
        this.oneFix = oneFix;
        this.listener = listener;
    }

    @Override
    public void start() {
        if (oneFix) {
            locationManager.requestSingleUpdate(provider, this, Looper.getMainLooper());
        } else {
            locationManager.requestLocationUpdates(provider, 1000, 100, this);
        }
    }

    @Override
    public void stopUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public Location getLastLocation() {
        return locationManager.getLastKnownLocation(provider);
    }

    private String getProvider(LocationAccuracy accuracy) {
        // TODO select depending on accuracy
        return LocationManager.GPS_PROVIDER;
    }

    @Override
    public void onLocationChanged(Location location) {
        listener.onLocationUpdated(location);
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
}
