package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.ActivityGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.config.ScheduledOnLocationUpdateListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public class LocationBasedOnActivityProvider implements LocationProvider, OnActivityUpdatedListener {

    private final ActivityGooglePlayServicesProvider activityProvider;
    private final LocationGooglePlayServicesProvider locationProvider;
    private final LocationBasedOnActivityListener locationBasedOnActivityListener;
    private List<ScheduledOnLocationUpdateListener> listeners = new ArrayList<>();

    public LocationBasedOnActivityProvider(
            @NonNull LocationBasedOnActivityListener locationBasedOnActivityListener) {
        activityProvider = new ActivityGooglePlayServicesProvider();
        locationProvider = new LocationGooglePlayServicesProvider();
        this.locationBasedOnActivityListener = locationBasedOnActivityListener;
    }

    @Override
    public void init(Context context, Logger logger) {
        locationProvider.init(context, logger);
        activityProvider.init(context, logger);
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        listeners = new ArrayList<>();

        addListenerSoftly(listener, params, singleUpdate);

        locationProvider.start(listener, params, false);
        startActivityProvider();
    }

    @Override
    public void start(List<ScheduledOnLocationUpdateListener> scheduledListeners) {
        this.listeners = scheduledListeners;
        restart();
    }

    private void restart() {
        locationProvider.start(listeners);
        startActivityProvider();
    }

    @Override
    public void addListener(OnLocationUpdatedListener listener, LocationParams params,
                            boolean singleUpdate) {
        addListenerSoftly(listener, params, singleUpdate);
        locationProvider.addListener(listener, params, false);
        startActivityProvider();
    }

    private void addListenerSoftly(OnLocationUpdatedListener listener, LocationParams params,
                            boolean singleUpdate) {
        if (singleUpdate) {
            throw new IllegalArgumentException("singleUpdate cannot be set to true");
        }
        this.listeners.add(new ScheduledOnLocationUpdateListener(listener, params, false));
    }

    private void startActivityProvider() {
        activityProvider.start(this, ActivityParams.NORMAL);
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
            return locationProvider.removeListener(listener);
        } else {
            return false;
        }
    }

    @Override
    public void stop() {
        locationProvider.stop();
        activityProvider.stop();
    }

    @Override
    public Location getLastLocation() {
        return locationProvider.getLastLocation();
    }

    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        LocationParams params = locationBasedOnActivityListener.locationParamsForActivity(detectedActivity);
        if (params != null) {
            if (listeners.size() == 1 && !listeners.get(0).getParams().equals(params)) {
                restart();
            } else if (listeners.size() != 1) {
                restart();
            }
        }
    }


    public interface LocationBasedOnActivityListener {
        public LocationParams locationParamsForActivity(DetectedActivity detectedActivity);
    }
}
