package io.nlopez.smartlocation;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.GeofencingRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.activity.providers.ActivityGooglePlayServicesProvider;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.geocoding.GeocodingController;
import io.nlopez.smartlocation.geocoding.GeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingController;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.providers.android.AndroidGeocodingProviderFactory;
import io.nlopez.smartlocation.geofencing.GeofencingAddController;
import io.nlopez.smartlocation.geofencing.GeofencingProviderFactory;
import io.nlopez.smartlocation.geofencing.GeofencingRemoveController;
import io.nlopez.smartlocation.geofencing.providers.playservices.GooglePlayServicesGeofencingProviderFactory;
import io.nlopez.smartlocation.location.LocationController;
import io.nlopez.smartlocation.location.LocationProviderFactory;
import io.nlopez.smartlocation.location.LocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.location.providers.legacy.LocationManagerProviderFactory;
import io.nlopez.smartlocation.location.providers.playservices.GooglePlayServicesLocationProviderFactory;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

import static io.nlopez.smartlocation.common.OnAllProvidersFailed.EMPTY;
import static io.nlopez.smartlocation.utils.Nulls.orDefault;

/**
 * Managing class for SmartLocation library.
 */
public class SmartLocation {

    @NonNull
    private Context context;
    @NonNull
    private Logger logger;

    /**
     * Creates the SmartLocation basic instance.
     *
     * @param context execution context
     * @param logger  logger interface
     */
    private SmartLocation(@NonNull Context context, @NonNull Logger logger) {
        this.context = context;
        this.logger = logger;
    }

    @NonNull
    public static SmartLocation with(@NonNull Context context) {
        return new Builder(context).build();
    }

    /**
     * @return request handler for location operations
     */
    @NonNull
    public LocationBuilder location() {
        return location(
                new GooglePlayServicesLocationProviderFactory(),
                new LocationManagerProviderFactory());
    }

    /**
     * @param providerFactories factories for the location provider we want to use, in order
     * @return request handler for location operations
     */
    @NonNull
    public LocationBuilder location(@NonNull LocationProviderFactory... providerFactories) {
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
    @NonNull
    public GeofencingBuilder geofencing() {
        return geofencing(new GooglePlayServicesGeofencingProviderFactory());
    }

    /**
     * @param geofencingProviderFactories geofencing we want to use, in order
     * @return request handler for geofencing operations
     */
    @NonNull
    public GeofencingBuilder geofencing(@NonNull GeofencingProviderFactory... geofencingProviderFactories) {
        return new GeofencingBuilder(this, geofencingProviderFactories);
    }

    /**
     * @return request handler for geocoding operations
     */
    @NonNull
    public GeocodingBuilder geocoding() {
        return geocoding(new AndroidGeocodingProviderFactory());
    }

    /**
     * @param providerFactories provider factories we want to use, in order
     * @return request handler for geocoding operations
     */
    @NonNull
    public GeocodingBuilder geocoding(@NonNull GeocodingProviderFactory... providerFactories) {
        return new GeocodingBuilder(this, providerFactories);
    }

    public static class Builder {
        private final Context context;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
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

        @NonNull
        public LocationBuilder config(@NonNull LocationProviderParams params) {
            mParams = params;
            return this;
        }

        @NonNull
        public LocationBuilder timeout(long timeout) {
            mTimeout = timeout;
            return this;
        }

        @NonNull
        public LocationBuilder get() {
            return this;
        }

        @NonNull
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

        @NonNull
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

        private final SmartLocation mParent;
        private final List<GeocodingProviderFactory> mGeocodingProviders;
        private int mMaxResults = DEFAULT_MAX_RESULTS;

        public GeocodingBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull GeocodingProviderFactory[] geocodingProviders) {
            mParent = smartLocation;
            mGeocodingProviders = Arrays.asList(geocodingProviders);
        }

        @NonNull
        public GeocodingBuilder maxResults(int maxResults) {
            mMaxResults = maxResults;
            return this;
        }

        @NonNull
        public ReverseGeocodingController findNameByLocation(
                @NonNull Location location,
                @NonNull ReverseGeocodingUpdatedListener listener) {
            final ReverseGeocodingController controller = new ReverseGeocodingController(
                    mParent.context,
                    location,
                    mMaxResults,
                    listener,
                    listener,
                    mGeocodingProviders,
                    mParent.logger);
            return controller.start();
        }

        @NonNull
        public GeocodingController findLocationByName(
                @NonNull String name,
                @NonNull GeocodingUpdatedListener listener) {
            final GeocodingController controller = new GeocodingController(
                    mParent.context,
                    name,
                    mMaxResults,
                    listener,
                    listener,
                    mGeocodingProviders,
                    mParent.logger);
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

    public static class GeofencingBuilder {
        @NonNull
        private final SmartLocation mParent;
        @NonNull
        private final List<GeofencingProviderFactory> mGeofencingProviders;
        @Nullable
        private OnAllProvidersFailed mProvidersFailed;

        public GeofencingBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull GeofencingProviderFactory[] geofencingProviders) {
            mParent = smartLocation;
            mGeofencingProviders = Arrays.asList(geofencingProviders);
        }

        @NonNull
        public GeofencingBuilder failureListener(@NonNull OnAllProvidersFailed providersFailed) {
            mProvidersFailed = providersFailed;
            return this;
        }

        @NonNull
        public GeofencingAddController addGeofences(
                @NonNull GeofencingRequest request,
                @NonNull PendingIntent pendingIntent) {
            final GeofencingAddController controller = new GeofencingAddController(
                    mParent.context,
                    orDefault(mProvidersFailed, EMPTY),
                    mGeofencingProviders,
                    mParent.logger);
            return controller.addGeofences(request, pendingIntent);
        }

        @NonNull
        public GeofencingRemoveController removeGeofences(@NonNull List<String> geofenceIds) {
            final GeofencingRemoveController controller = new GeofencingRemoveController(
                    mParent.context,
                    orDefault(mProvidersFailed, EMPTY),
                    mGeofencingProviders,
                    mParent.logger);
            return controller.removeGeofence(geofenceIds);
        }

        @NonNull
        public GeofencingRemoveController removeGeofences(@NonNull PendingIntent pendingIntent) {
            final GeofencingRemoveController controller = new GeofencingRemoveController(
                    mParent.context,
                    orDefault(mProvidersFailed, EMPTY),
                    mGeofencingProviders,
                    mParent.logger);
            return controller.removeGeofence(pendingIntent);
        }
    }
}
