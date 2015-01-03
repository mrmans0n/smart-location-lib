package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.GooglePlayServicesActivityProvider;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.GooglePlayServicesLocationProvider;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocation {

    private Context context;
    private Logger logger;

    private SmartLocation(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    public static SmartLocation with(Context context) {
        return new Builder(context).build();
    }

    public LocationControl location(OnLocationUpdatedListener listener) {
        return new LocationControl(this, listener);
    }

    public ActivityRecognitionControl activityRecognition(OnActivityUpdatedListener listener) {
        return new ActivityRecognitionControl(this, listener);
    }

    public static class Builder {
        private final Context context;
        private boolean loggingEnabled;

        public Builder(@NonNull Context context) {
            this.context = context.getApplicationContext();
            this.loggingEnabled = false;
        }

        public Builder logging(boolean enabled) {
            this.loggingEnabled = enabled;
            return this;
        }

        public SmartLocation build() {
            return new SmartLocation(context, LoggerFactory.buildLogger(loggingEnabled));
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
            this.params = LocationParams.BEST_EFFORT;
            this.oneFix = false;
            this.listener = listener;
            this.smartLocation = smartLocation;
            provider.init(smartLocation.context, listener, smartLocation.logger);

        }

        public LocationControl config(@NonNull LocationParams params) {
            this.params = params;
            return this;
        }

        public LocationControl provider(@NonNull LocationProvider newProvider) {
            if (newProvider.getClass().equals(provider.getClass())) {
                smartLocation.logger.w("Creating a new provider that has the same class as before. Are you sure you want to do this?");
            }
            provider = newProvider;
            provider.init(smartLocation.context, listener, smartLocation.logger);
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
            return provider.getLastLocation();
        }

        public LocationControl get() {
            return this;
        }

        public void start() {
            provider.start(params, oneFix);
        }

        public void stop() {
            provider.stop();
        }
    }

    public interface OnLocationUpdatedListener {
        public void onLocationUpdated(Location location);
    }

    public static class ActivityRecognitionControl {
        private final SmartLocation smartLocation;
        private final OnActivityUpdatedListener listener;
        private ActivityParams params;
        private ActivityProvider provider;

        public ActivityRecognitionControl(SmartLocation smartLocation, OnActivityUpdatedListener listener) {
            this.listener = listener;
            this.smartLocation = smartLocation;
            this.provider = new GooglePlayServicesActivityProvider();
            this.params = ActivityParams.NORMAL;
            provider.init(smartLocation.context, listener, smartLocation.logger);
        }

        public ActivityRecognitionControl config(@NonNull ActivityParams params) {
            this.params = params;
            return this;
        }

        public ActivityRecognitionControl provider(@NonNull ActivityProvider newProvider) {
            if (newProvider.getClass().equals(provider.getClass())) {
                smartLocation.logger.w("Creating a new provider that has the same class as before. Are you sure you want to do this?");
            }
            provider = newProvider;
            provider.init(smartLocation.context, listener, smartLocation.logger);
            return this;
        }

        public DetectedActivity getLastActivity() {
            return provider.getLastActivity();
        }

        public ActivityRecognitionControl get() {
            return this;
        }

        public void start() {
            provider.start(params);
        }

        public void stop() {
            provider.stop();
        }
    }

    public interface OnActivityUpdatedListener {
        public void onActivityUpdated(DetectedActivity detectedActivity);
    }
}
