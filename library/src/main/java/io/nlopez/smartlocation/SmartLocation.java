package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.ActivityGooglePlayServicesProvider;
import io.nlopez.smartlocation.geofencing.GeofencingProvider;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;
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
    private boolean preInitialize;

    private SmartLocation(Context context, Logger logger, boolean preInitialize) {
        this.context = context;
        this.logger = logger;
        this.preInitialize = preInitialize;
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

    public GeofencingControl geofencing() {
        return new GeofencingControl(this);
    }

    public static class Builder {
        private final Context context;
        private boolean loggingEnabled;
        private boolean preInitialize;

        public Builder(@NonNull Context context) {
            this.context = context.getApplicationContext();
            this.loggingEnabled = false;
            this.preInitialize = true;
        }

        public Builder logging(boolean enabled) {
            this.loggingEnabled = enabled;
            return this;
        }

        public Builder preInitialize(boolean enabled) {
            this.preInitialize = enabled;
            return this;
        }

        public SmartLocation build() {
            return new SmartLocation(context, LoggerFactory.buildLogger(loggingEnabled), preInitialize);
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

            if (smartLocation.preInitialize) {
                if (!MAPPING.containsKey(smartLocation.context)) {
                    MAPPING.put(smartLocation.context, new LocationGooglePlayServicesProvider());
                }
                provider = MAPPING.get(smartLocation.context);
                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public LocationControl config(@NonNull LocationParams params) {
            this.params = params;
            return this;
        }

        public LocationControl provider(@NonNull LocationProvider newProvider) {
            if (provider != null && newProvider.getClass().equals(provider.getClass())) {
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
            if (provider == null) {
                throw new RuntimeException("A provider must be initialized");
            }
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
            if (smartLocation.preInitialize) {
                if (!MAPPING.containsKey(smartLocation.context)) {
                    MAPPING.put(smartLocation.context, new ActivityGooglePlayServicesProvider());
                }
                provider = MAPPING.get(smartLocation.context);

                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public ActivityRecognitionControl config(@NonNull ActivityParams params) {
            this.params = params;
            return this;
        }

        public ActivityRecognitionControl provider(@NonNull ActivityProvider newProvider) {
            if (provider != null && newProvider.getClass().equals(provider.getClass())) {
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
            if (provider == null) {
                throw new RuntimeException("A provider must be initialized");
            }
            provider.start(listener, params);
        }

        public void stop() {
            provider.stop();
        }

    }

    public static class GeofencingControl {
        private static final Map<Context, GeofencingProvider> MAPPING = new HashMap<>();

        private final SmartLocation smartLocation;
        private GeofencingProvider provider;

        public GeofencingControl(SmartLocation smartLocation) {
            this.smartLocation = smartLocation;
            if (smartLocation.preInitialize) {
                if (!MAPPING.containsKey(smartLocation.context)) {
                    MAPPING.put(smartLocation.context, new GeofencingGooglePlayServicesProvider());
                }
                provider = MAPPING.get(smartLocation.context);

                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public GeofencingControl provider(@NonNull GeofencingProvider newProvider) {
            if (provider != null &&newProvider.getClass().equals(provider.getClass())) {
                smartLocation.logger.w("Creating a new provider that has the same class as before. Are you sure you want to do this?");
            }
            provider = newProvider;
            MAPPING.put(smartLocation.context, newProvider);
            provider.init(smartLocation.context, smartLocation.logger);
            return this;
        }

        public GeofencingControl add(GeofenceModel geofenceModel) {
            provider.addGeofence(geofenceModel);
            return this;
        }

        public GeofencingControl remove(String geofenceId) {
            provider.removeGeofence(geofenceId);
            return this;
        }

        public GeofencingControl addAll(List<GeofenceModel> geofenceModelList) {
            provider.addGeofences(geofenceModelList);
            return this;
        }

        public GeofencingControl removeAll(List<String> geofenceIdsList) {
            provider.removeGeofences(geofenceIdsList);
            return this;
        }

        public void start(OnGeofencingTransitionListener listener) {
            if (provider == null) {
                throw new RuntimeException("A provider must be initialized");
            }
            provider.start(listener);
        }

        public void stop() {
            provider.stop();
        }
    }


}
