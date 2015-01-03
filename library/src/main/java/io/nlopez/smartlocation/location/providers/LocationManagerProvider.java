package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.Utils;

/**
 * Created by nacho on 12/23/14.
 */
public class LocationManagerProvider implements LocationProvider, LocationListener {
    private static final String PROVIDER_FILE = "LOCATIONMANAGERPROVIDER_PREFS";
    private static final String LAST_ID = "last";

    private LocationManager locationManager;
    private Criteria criteria;
    private boolean oneFix;
    private LocationParams params;
    private SmartLocation.OnLocationUpdatedListener listener;
    private SharedPreferences sharedPreferences;
    private Logger logger;

    @Override
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate, Logger logger) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.params = params;
        this.criteria = getProvider(params);
        this.oneFix = singleUpdate;
        this.listener = listener;
        this.logger = logger;

        sharedPreferences = context.getSharedPreferences(PROVIDER_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public void start() {
        if (oneFix) {
            locationManager.requestSingleUpdate(criteria, this, Looper.getMainLooper());
        } else {
            locationManager.requestLocationUpdates(params.getInterval(), params.getDistance(), criteria, this, Looper.getMainLooper());
        }
    }

    @Override
    public void stopUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public Location getLastLocation() {

        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(providerFromCriteria(criteria));
            if (location != null) {
                return location;
            }
        }

        Location location = Utils.getLocationFromPreferences(sharedPreferences, LAST_ID);
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
        if (sharedPreferences != null) {
            logger.d("Stored in SharedPreferences");
            Utils.storeLocationInPreferences(sharedPreferences, location, LAST_ID);
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
