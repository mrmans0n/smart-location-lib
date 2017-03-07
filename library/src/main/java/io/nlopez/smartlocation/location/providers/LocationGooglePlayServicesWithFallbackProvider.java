package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.config.ScheduledOnLocationUpdateListener;
import io.nlopez.smartlocation.utils.GooglePlayServicesListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public class LocationGooglePlayServicesWithFallbackProvider implements LocationProvider, GooglePlayServicesListener {

    private Logger logger;
    private Context context;

    private LocationProvider provider;
    private List<ScheduledOnLocationUpdateListener> listeners = new ArrayList<>();

    public LocationGooglePlayServicesWithFallbackProvider(Context context) {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            provider = new LocationGooglePlayServicesProvider(this);
        } else {
            provider = new LocationManagerProvider();
        }
    }

    @Override
    public void init(Context context, Logger logger) {
        this.logger = logger;
        this.context = context;

        logger.d("Currently selected provider = " + provider.getClass().getSimpleName());

        provider.init(context, logger);
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.listeners = new ArrayList<>();
        addListener(listener, params, singleUpdate);
    }

    @Override
    public void start(List<ScheduledOnLocationUpdateListener> scheduledListeners) {
        provider.start(scheduledListeners);
    }

    @Override
    public void addListener(OnLocationUpdatedListener listener, LocationParams params,
                            boolean singleUpdate) {
        if (listener != null) {
            this.listeners.add(
                    new ScheduledOnLocationUpdateListener(listener, params, singleUpdate));
        }
        provider.addListener(listener, params, singleUpdate);
    }

    @Override
    public boolean removeListener(OnLocationUpdatedListener listener) {
        ScheduledOnLocationUpdateListener scheduledListenerToRemove = null;

        for (ScheduledOnLocationUpdateListener scheduledListener : listeners) {
            if (scheduledListener.getListener() == listener) {
                scheduledListenerToRemove = scheduledListener;
                break;
            }
        }

        if (scheduledListenerToRemove != null) {
            listeners.remove(scheduledListenerToRemove);
            return provider.removeListener(listener);
        } else {
            return false;
        }
    }

    @Override
    public void stop() {
        provider.stop();
        listeners = new ArrayList<>();
    }

    @Override
    public Location getLastLocation() {
        return provider.getLastLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Nothing to do here
    }

    @Override
    public void onConnectionSuspended(int i) {
        fallbackToLocationManager();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        fallbackToLocationManager();
    }

    private void fallbackToLocationManager() {
        logger.d("FusedLocationProvider not working, falling back and using LocationManager");
        provider = new LocationManagerProvider();
        provider.init(context, logger);
        if (listeners.size() >= 0) {
            provider.start(listeners);
        }
    }
}
