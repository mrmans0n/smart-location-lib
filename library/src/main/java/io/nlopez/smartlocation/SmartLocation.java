package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.ActivityGooglePlayServicesProvider;
import io.nlopez.smartlocation.geocoding.GeocodingController;
import io.nlopez.smartlocation.geocoding.GeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingController;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.providers.android.AndroidGeocodingProviderFactory;
import io.nlopez.smartlocation.geofencing.GeofencingProvider;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.providers.GeofencingGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.LocationController;
import io.nlopez.smartlocation.location.LocationProviderFactory;
import io.nlopez.smartlocation.location.LocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.location.providers.legacy.LocationManagerProviderFactory;
import io.nlopez.smartlocation.location.providers.playservices.GooglePlayServicesLocationProviderFactory;
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
     * @param context execution context
     * @param logger  logger interface
     */
    private SmartLocation(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    public static SmartLocation with(Context context) {
        return new Builder(context).build();
    }

    /**
     * @return request handler for location operations
     */
    public LocationBuilder location() {
        return location(
                new GooglePlayServicesLocationProviderFactory(),
                new LocationManagerProviderFactory());
    }

    /**
     * @param providerFactories factories for the location provider we want to use, in order
     * @return request handler for location operations
     */
    public LocationBuilder location(LocationProviderFactory... providerFactories) {
        return new LocationBuilder(this, providerFactories);
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
    public GeocodingBuilder geocoding() {
        return geocoding(new AndroidGeocodingProviderFactory());
    }

    /**
     * @param providerFactories provider factories we want to use, in order
     * @return request handler for geocoding operations
     */
    public GeocodingBuilder geocoding(GeocodingProviderFactory... providerFactories) {
        return new GeocodingBuilder(this, providerFactories);
    }

    public static class Builder {
        private final Context context;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public SmartLocation build() {
            return new SmartLocation(context, LoggerFactory.get());
        }

    }

    public static class LocationBuilder {

        private static final Map<Context, LocationController> CONTROLLER_MAPPING = new WeakHashMap<>();

        private final SmartLocation mParent;
        private LocationProviderParams mParams;
        private List<LocationProviderFactory> mProviderFactoryList;
        private LocationController mProviderController;
        private long mTimeout = LocationController.NO_TIMEOUT;

        public LocationBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull LocationProviderFactory[] locationProviders) {
            mParent = smartLocation;
            mParams = LocationProviderParams.BEST_EFFORT;
            mProviderFactoryList = Arrays.asList(locationProviders);
        }

        public LocationBuilder config(@NonNull LocationProviderParams params) {
            mParams = params;
            return this;
        }

        public LocationBuilder timeout(long timeout) {
            mTimeout = timeout;
            return this;
        }

        public LocationBuilder get() {
            return this;
        }

        public LocationController start(@NonNull LocationUpdatedListener listener) {
            mProviderController = new LocationController(
                    mParent.context,
                    listener,
                    listener,
                    mParams,
                    mTimeout,
                    mProviderFactoryList,
                    mParent.logger);
            CONTROLLER_MAPPING.put(mParent.context, mProviderController);
            return mProviderController.start();
        }

        public void stop() {
            if (mProviderController != null) {
                mProviderController.stop();
            } else if (CONTROLLER_MAPPING.containsKey(mParent.context)) {
                final LocationController controller = CONTROLLER_MAPPING.get(mParent.context);
                controller.stop();
            } else {
                mParent.logger.d("Controller not found, nothing to stop. Please store the result of the start() method for accessing the rest of the controls");
            }
        }
    }

    public static class GeocodingBuilder {
        static final int DEFAULT_MAX_RESULTS = 5;

        private final SmartLocation mSmartLocation;
        private final List<GeocodingProviderFactory> mGeocodingProviders;
        private int mMaxResults = DEFAULT_MAX_RESULTS;

        public GeocodingBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull GeocodingProviderFactory[] geocodingProviders) {
            mSmartLocation = smartLocation;
            mGeocodingProviders = Arrays.asList(geocodingProviders);
        }

        public GeocodingBuilder maxResults(int maxResults) {
            mMaxResults = maxResults;
            return this;
        }

        public ReverseGeocodingController findNameByLocation(
                @NonNull Location location,
                @NonNull ReverseGeocodingUpdatedListener listener) {
            final ReverseGeocodingController controller = new ReverseGeocodingController(
                    mSmartLocation.context,
                    location,
                    mMaxResults,
                    listener,
                    listener,
                    mGeocodingProviders,
                    mSmartLocation.logger);
            return controller.start();
        }

        public GeocodingController findLocationByName(
                @NonNull String name,
                @NonNull GeocodingUpdatedListener listener) {
            final GeocodingController controller = new GeocodingController(
                    mSmartLocation.context,
                    name,
                    mMaxResults,
                    listener,
                    listener,
                    mGeocodingProviders,
                    mSmartLocation.logger);
            return controller.start();
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
