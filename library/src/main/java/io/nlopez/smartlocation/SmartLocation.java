package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.GooglePlayServicesLocationProvider;
import io.nlopez.smartlocation.utils.Blah;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocation {

    private Context context;
    private Logger logger;
    private OnActivityUpdatedListener activityUpdatedListener;

    private SmartLocation(Context context, Blah logger) {
        this.context = context;
        this.logger = logger;
    }

    public static SmartLocation with(Context context) {
        return new Builder(context).build();
    }

    public LocationControl location(OnLocationUpdatedListener listener) {
        return new LocationControl(this, listener);
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
            return new SmartLocation(context, new Blah(loggingEnabled));
        }

    }

    public static class LocationControl {

        private final SmartLocation smartLocation;
        private final OnLocationUpdatedListener listener;
        private LocationParams params;
        private LocationProvider provider;
        private boolean oneFix;

        public LocationControl(SmartLocation smartLocation, OnLocationUpdatedListener listener) {
            // Default values
            this.provider = new GooglePlayServicesLocationProvider();
            this.params = LocationParams.PRESET_MEDIUM;
            this.oneFix = false;
            this.listener = listener;
            this.smartLocation = smartLocation;
        }

        public LocationControl accuracy(LocationParams params) {
            this.params = params;
            return this;
        }

        public LocationControl provider(LocationProvider provider) {
            this.provider = provider;
            return this;
        }

        public LocationControl oneFix() {
            this.oneFix = true;
            return this;
        }

        public LocationControl continuous() {
            this.oneFix = false;
            return this;
        }

        public Location getLastLocation() {
            // TODO change for a persistent cache?
            return provider.getLastLocation();
        }

        public LocationControl get() {
            return this;
        }

        public void start() {
            provider.init(smartLocation.context, listener, params, oneFix, smartLocation.logger);
            provider.start();
        }

        public void stop() {
            provider.stopUpdates();
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
