package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;

import java.util.HashMap;
import java.util.Map;

import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.ActivityGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
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

    public LocationControl location() {
        return new LocationControl(this);
    }

    public ActivityRecognitionControl activityRecognition() {
        return new ActivityRecognitionControl(this);
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

        private static final Map<Context, LocationProvider> MAPPING = new HashMap<>();

        private final SmartLocation smartLocation;
        private LocationParams params;
        private LocationProvider provider;
        private boolean oneFix;

        public LocationControl(SmartLocation smartLocation) {
            this.smartLocation = smartLocation;
            params = LocationParams.BEST_EFFORT;
            oneFix = false;

            if (!MAPPING.containsKey(smartLocation.context)) {
                MAPPING.put(smartLocation.context, new LocationGooglePlayServicesProvider());
            }
            provider = MAPPING.get(smartLocation.context);
            provider.init(smartLocation.context, smartLocation.logger);

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
            MAPPING.put(smartLocation.context, newProvider);
            provider.init(smartLocation.context, smartLocation.logger);
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

        public void start(OnLocationUpdatedListener listener) {
            provider.start(listener, params, oneFix);
        }

        public void stop() {
            provider.stop();
        }
    }


    public static class ActivityRecognitionControl {
        private static final Map<Context, ActivityProvider> MAPPING = new HashMap<>();

        private final SmartLocation smartLocation;
        private ActivityParams params;
        private ActivityProvider provider;

        public ActivityRecognitionControl(SmartLocation smartLocation) {
            this.smartLocation = smartLocation;
            params = ActivityParams.NORMAL;

            if (!MAPPING.containsKey(smartLocation.context)) {
                MAPPING.put(smartLocation.context, new ActivityGooglePlayServicesProvider());
            }
            provider = MAPPING.get(smartLocation.context);

            provider.init(smartLocation.context, smartLocation.logger);
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
            MAPPING.put(smartLocation.context, newProvider);
            provider.init(smartLocation.context, smartLocation.logger);
            return this;
        }

        public DetectedActivity getLastActivity() {
            return provider.getLastActivity();
        }

        public ActivityRecognitionControl get() {
            return this;
        }

        public void start(OnActivityUpdatedListener listener) {
            provider.start(listener, params);
        }

        public void stop() {
            provider.stop();
        }

    }


}
