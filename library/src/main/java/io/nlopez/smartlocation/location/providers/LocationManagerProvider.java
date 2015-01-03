package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by nacho on 12/23/14.
 */
public class LocationManagerProvider implements LocationProvider, LocationListener {
    private static final String LOCATIONMANAGERPROVIDER_ID = "LMP";

    private LocationManager locationManager;
    private SmartLocation.OnLocationUpdatedListener listener;
    private LocationStore locationStore;
    private Logger logger;

    @Override
    public void init(Context context, Logger logger) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.logger = logger;

        locationStore = new LocationStore(context);
    }

    @Override
    public void start(SmartLocation.OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.listener = listener;
        Criteria criteria = getProvider(params);

        if (singleUpdate) {
            locationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());
        } else {
            locationManager.requestLocationUpdates(params.getInterval(), params.getDistance(), criteria, this, Looper.getMainLooper());
        }
    }

    @Override
    public void stop() {
        locationManager.removeUpdates(this);
    }

    @Override
    public Location getLastLocation() {

        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                return location;
            }
        }

        Location location = locationStore.get(LOCATIONMANAGERPROVIDER_ID);
        if (location != null) {
            return location;
        }

        return null;
    }

    private Criteria getProvider(LocationParams params) {
        final LocationAccuracy accuracy = params.getAccuracy();
        final Criteria criteria = new Criteria();
        switch (accuracy) {
            case HIGH:
                criteria.setAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                break;
            case MEDIUM:
                criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                break;
            case LOW:
                criteria.setAccuracy(Criteria.ACCURACY_LOW);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
        return criteria;
    }

    private String providerFromCriteria(Criteria criteria) {
        switch (criteria.getAccuracy()) {
            case Criteria.ACCURACY_HIGH:
                return LocationManager.GPS_PROVIDER;
            case Criteria.ACCURACY_MEDIUM:
                return LocationManager.NETWORK_PROVIDER;
            default:
                return LocationManager.NETWORK_PROVIDER;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        logger.d("onLocationChanged", location);
        listener.onLocationUpdated(location);
        if (locationStore != null) {
            logger.d("Stored in SharedPreferences");
            locationStore.put(LOCATIONMANAGERPROVIDER_ID, location);
        }
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
