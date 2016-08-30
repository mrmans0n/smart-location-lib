package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.ActivityGooglePlayServicesProvider;
import io.nlopez.smartlocation.geocoding.GeocodingProvider;
import io.nlopez.smartlocation.geocoding.providers.AndroidGeocodingProvider;
import io.nlopez.smartlocation.geofencing.GeofencingProvider;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import io.nlopez.smartlocation.location.utils.LocationState;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

/**
 * Managing class for SmartLocation library.
 */
public class SmartLocation {

    private Context context;
    private Logger logger;
    private boolean preInitialize;

    /**
     * Creates the SmartLocation basic instance.
     *
     * @param context       execution context
     * @param logger        logger interface
     * @param preInitialize TRUE (default) if we want to instantiate directly the default providers. FALSE if we want to initialize them ourselves.
     */
    private SmartLocation(Context context, Logger logger, boolean preInitialize) {
        this.context = context;
        this.logger = logger;
        this.preInitialize = preInitialize;
    }

    public static SmartLocation with(Context context) {
        return new Builder(context).build();
    }

    /**
     * @return request handler for location operations
     */
    public LocationControl location() {
        return location(new LocationGooglePlayServicesWithFallbackProvider(context));
    }

    /**
     * @param provider location provider we want to use
     * @return request handler for location operations
     */
    public LocationControl location(LocationProvider provider) {
        return new LocationControl(this, provider);
    }

    /**
     * Builder for activity recognition. Use activity() instead.
     *
     * @return builder for activity recognition.
     * @deprecated
     */
    @Deprecated
    public ActivityRecognitionControl activityRecognition() {
        return activity();
    }

    /**
     * @return request handler for activity recognition
     */
    public ActivityRecognitionControl activity() {
        return activity(new ActivityGooglePlayServicesProvider());
    }

    /**
     * @param activityProvider activity provider we want to use
     * @return request handler for activity recognition
     */
    public ActivityRecognitionControl activity(ActivityProvider activityProvider) {
        return new ActivityRecognitionControl(this, activityProvider);
    }

    /**
     * @return request handler for geofencing operations
     */
    public GeofencingControl geofencing() {
        return geofencing(new GeofencingGooglePlayServicesProvider());
    }

    /**
     * @param geofencingProvider geofencing provider we want to use
     * @return request handler for geofencing operations
     */
    public GeofencingControl geofencing(GeofencingProvider geofencingProvider) {
        return new GeofencingControl(this, geofencingProvider);
    }

    /**
     * @return request handler for geocoding operations
     */
    public GeocodingControl geocoding() {
        return geocoding(new AndroidGeocodingProvider());
    }

    /**
     * @param geocodingProvider geocoding provider we want to use
     * @return request handler for geocoding operations
     */
    public GeocodingControl geocoding(GeocodingProvider geocodingProvider) {
        return new GeocodingControl(this, geocodingProvider);
    }

    public static class Builder {
        private final Context context;
        private boolean loggingEnabled;
        private boolean preInitialize;

        public Builder(@NonNull Context context) {
            this.context = context;
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

        private static final Map<Context, LocationProvider> MAPPING = new WeakHashMap<>();

        private final SmartLocation smartLocation;
        private LocationParams params;
        private LocationProvider provider;
        private boolean oneFix;

        public LocationControl(@NonNull SmartLocation smartLocation, @NonNull LocationProvider locationProvider) {
            this.smartLocation = smartLocation;
            params = LocationParams.BEST_EFFORT;
            oneFix = false;

            if (!MAPPING.containsKey(smartLocation.context)) {
                MAPPING.put(smartLocation.context, locationProvider);
            }
            provider = MAPPING.get(smartLocation.context);

            if (smartLocation.preInitialize) {
                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public LocationControl config(@NonNull LocationParams params) {
            this.params = params;
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

        public LocationState state() {
            return LocationState.with(smartLocation.context);
        }

        @Nullable
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

    public static class GeocodingControl {

        private static final Map<Context, GeocodingProvider> MAPPING = new WeakHashMap<>();

        private final SmartLocation smartLocation;
        private GeocodingProvider provider;
        private boolean directAdded = false;
        private boolean reverseAdded = false;

        public GeocodingControl(@NonNull SmartLocation smartLocation, @NonNull GeocodingProvider geocodingProvider) {
            this.smartLocation = smartLocation;

            if (!MAPPING.containsKey(smartLocation.context)) {
                MAPPING.put(smartLocation.context, geocodingProvider);
            }
            provider = MAPPING.get(smartLocation.context);

            if (smartLocation.preInitialize) {
                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public GeocodingControl get() {
            return this;
        }

        public void reverse(@NonNull Location location, @NonNull OnReverseGeocodingListener reverseGeocodingListener) {
            add(location);
            start(reverseGeocodingListener);
        }

        public void direct(@NonNull String name, @NonNull OnGeocodingListener geocodingListener) {
            add(name);
            start(geocodingListener);
        }

        public GeocodingControl add(@NonNull Location location) {
            reverseAdded = true;
            provider.addLocation(location, 1);
            return this;
        }

        public GeocodingControl add(@NonNull Location location, int maxResults) {
            reverseAdded = true;
            provider.addLocation(location, maxResults);
            return this;
        }

        public GeocodingControl add(@NonNull String name) {
            directAdded = true;
            provider.addName(name, 1);
            return this;
        }

        public GeocodingControl add(@NonNull String name, int maxResults) {
            directAdded = true;
            provider.addName(name, maxResults);
            return this;
        }

        public void start(OnGeocodingListener geocodingListener) {
            start(geocodingListener, null);
        }

        public void start(OnReverseGeocodingListener reverseGeocodingListener) {
            start(null, reverseGeocodingListener);
        }

        /**
         * Starts the geocoder conversions, for either direct geocoding (name to location) and reverse geocoding (location to address).
         *
         * @param geocodingListener        will be called for name to location queries
         * @param reverseGeocodingListener will be called for location to name queries
         */
        public void start(OnGeocodingListener geocodingListener, OnReverseGeocodingListener reverseGeocodingListener) {
            if (provider == null) {
                throw new RuntimeException("A provider must be initialized");
            }
            if (directAdded && geocodingListener == null) {
                smartLocation.logger.w("Some places were added for geocoding but the listener was not specified!");
            }
            if (reverseAdded && reverseGeocodingListener == null) {
                smartLocation.logger.w("Some places were added for reverse geocoding but the listener was not specified!");
            }

            provider.start(geocodingListener, reverseGeocodingListener);
        }

        /**
         * Cleans up after the geocoder calls. Will be needed for avoiding possible leaks in registered receivers.
         */
        public void stop() {
            provider.stop();
        }
    }


    public static class ActivityRecognitionControl {
        private static final Map<Context, ActivityProvider> MAPPING = new WeakHashMap<>();

        private final SmartLocation smartLocation;
        private ActivityParams params;
        private ActivityProvider provider;

        public ActivityRecognitionControl(@NonNull SmartLocation smartLocation, @NonNull ActivityProvider activityProvider) {
            this.smartLocation = smartLocation;
            params = ActivityParams.NORMAL;

            if (!MAPPING.containsKey(smartLocation.context)) {
                MAPPING.put(smartLocation.context, activityProvider);
            }
            provider = MAPPING.get(smartLocation.context);

            if (smartLocation.preInitialize) {
                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public ActivityRecognitionControl config(@NonNull ActivityParams params) {
            this.params = params;
            return this;
        }

        @Nullable
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
        private static final Map<Context, GeofencingProvider> MAPPING = new WeakHashMap<>();

        private final SmartLocation smartLocation;
        private GeofencingProvider provider;

        public GeofencingControl(@NonNull SmartLocation smartLocation, @NonNull GeofencingProvider geofencingProvider) {
            this.smartLocation = smartLocation;

            if (!MAPPING.containsKey(smartLocation.context)) {
                MAPPING.put(smartLocation.context, geofencingProvider);
            }
            provider = MAPPING.get(smartLocation.context);

            if (smartLocation.preInitialize) {
                provider.init(smartLocation.context, smartLocation.logger);
            }
        }

        public GeofencingControl add(@NonNull GeofenceModel geofenceModel) {
            provider.addGeofence(geofenceModel);
            return this;
        }

        public GeofencingControl remove(@NonNull String geofenceId) {
            provider.removeGeofence(geofenceId);
            return this;
        }

        public GeofencingControl addAll(@NonNull List<GeofenceModel> geofenceModelList) {
            provider.addGeofences(geofenceModelList);
            return this;
        }

        public GeofencingControl removeAll(@NonNull List<String> geofenceIdsList) {
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
