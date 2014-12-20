package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.location.GooglePlayServicesLocationProvider;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.LocationProviderCallback;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocation {

    private OnLocationUpdatedListener onLocationUpdatedListener;
    private Context context;
    private boolean loggingEnabled;
    private OnLocationUpdatedListener locationUpdatedListener;
    private OnActivityUpdatedListener activityUpdatedListener;
    private CentralControl centralControl;

    // Singleton stuff

    private SmartLocation(Context context, boolean loggingEnabled) {
        this.context = context;
        this.loggingEnabled = loggingEnabled;
    }

    public static SmartLocation with(Context context) {
        return new Builder(context).build();
    }

    public LocationControl location(OnLocationUpdatedListener listener) {
        this.locationUpdatedListener = listener;
        return new LocationControl(this);
    }

    public static class LocationControl implements LocationProviderCallback {

        private final SmartLocation smartLocation;
        private LocationProvider.LocationStrategy strategy;
        private LocationProvider provider;
        private LocationProvider.LocationRecurrence recurrence;

        public LocationControl(SmartLocation smartLocation) {
            // Default values
            this.provider = new GooglePlayServicesLocationProvider();
            this.strategy = LocationProvider.LocationStrategy.BEST_EFFORT;
            this.recurrence = LocationProvider.LocationRecurrence.CONTINUOUS;
            this.smartLocation = smartLocation;
        }

        public LocationControl strategy(LocationProvider.LocationStrategy type) {
            this.strategy = type;
            return this;
        }

        public LocationControl provider(LocationProvider provider) {
            this.provider = provider;
            return this;
        }

        public LocationControl recurrence(LocationProvider.LocationRecurrence recurrence) {
            this.recurrence = recurrence;
            return this;
        }

        public Location getLastLocation() {
            return provider.getLastLocation();
        }

        public LocationControl start() {
            provider.init(smartLocation.context, this, smartLocation.locationUpdatedListener, strategy, smartLocation.loggingEnabled);
            return this;
        }

        public void stop() {
            // TODO cancel requests
            provider.stopUpdates();
        }

        @Override
        public void onProviderReady() {
            provider.startForRecurrence(recurrence);
        }

        @Override
        public void onProviderError() {
            // TODO do something
        }
    }

    public static class CentralControl {
        private final SmartLocation smartLocation;

        public CentralControl(SmartLocation smartLocation) {
            this.smartLocation = smartLocation;
        }

        public void start() {
            // TODO starts the location
        }

        public void cancel() {
            // TODO stops the location
        }
    }

    public static class Builder {
        private final Context context;
        private boolean loggingEnabled;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder logging(boolean enabled) {
            this.loggingEnabled = enabled;
            return this;
        }

        public SmartLocation build() {
            return new SmartLocation(context, loggingEnabled);
        }

    }

    public interface OnLocationUpdatedListener {
        public void onLocationUpdated(Location location);
    }

    public interface OnActivityUpdatedListener {
        public void onActivityUpdated(DetectedActivity activity);
    }

    public interface OnLocationAndActivityUpdatedListener {
        public void onLocationAndActivityUpdated(Location location, DetectedActivity activity);
    }
}
