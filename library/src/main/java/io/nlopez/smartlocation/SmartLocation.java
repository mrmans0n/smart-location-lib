package io.nlopez.smartlocation;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.GeofencingRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.geocoding.GeocodingController;
import io.nlopez.smartlocation.geocoding.GeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingController;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.providers.android.AndroidGeocodingProviderFactory;
import io.nlopez.smartlocation.geofencing.GeofencingAddController;
import io.nlopez.smartlocation.geofencing.GeofencingBaseController;
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
import io.nlopez.smartlocation.utils.Nulls;

import static io.nlopez.smartlocation.common.OnAllProvidersFailed.EMPTY;
import static io.nlopez.smartlocation.utils.Nulls.orDefault;

/**
 * Managing class for SmartLocation library.
 */
@SuppressWarnings("UnusedReturnValue")
public class SmartLocation {

    @NonNull private Context context;
    @NonNull private Logger logger;

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
        return new LocationBuilder(this, new LocationController.Factory(), Arrays.asList(providerFactories));
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
        return new GeofencingBuilder(this,
                new GeofencingBaseController.Factory(),
                Arrays.asList(geofencingProviderFactories));
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
        return new GeocodingBuilder(this,
                new GeocodingController.Factory(),
                new ReverseGeocodingController.Factory(),
                Arrays.asList(providerFactories));
    }

    public static class Builder {
        @NonNull private final Context context;
        @Nullable private Logger mLogger;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        public Builder logger(@Nullable Logger logger) {
            mLogger = logger;
            return this;
        }

        @NonNull
        public SmartLocation build() {
            return new SmartLocation(context, Nulls.orDefault(mLogger, LoggerFactory.get()));
        }

    }

    public static class LocationBuilder {

        @NonNull static final Map<Context, LocationController> CONTROLLER_MAPPING = new WeakHashMap<>();

        @NonNull private final SmartLocation mParent;
        @NonNull private final LocationController.Factory mLocationControllerFactory;
        @NonNull private LocationProviderParams mParams;
        @NonNull private List<LocationProviderFactory> mProviderFactoryList;
        @Nullable private LocationController mProviderController;
        private long mTimeout = LocationController.NO_TIMEOUT;

        public LocationBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull LocationController.Factory locationControllerFactory,
                @NonNull List<LocationProviderFactory> locationProviders) {
            mParent = smartLocation;
            mLocationControllerFactory = locationControllerFactory;
            mParams = LocationProviderParams.BEST_EFFORT;
            mProviderFactoryList = locationProviders;
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
            mProviderController = mLocationControllerFactory.create(
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

        @NonNull private final SmartLocation mParent;
        @NonNull private final List<GeocodingProviderFactory> mGeocodingProviders;
        @NonNull private final GeocodingController.Factory mGeocodingControllerFactory;
        @NonNull private final ReverseGeocodingController.Factory mReverseGeocodingControllerFactory;
        private int mMaxResults = DEFAULT_MAX_RESULTS;

        public GeocodingBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull GeocodingController.Factory geocodingControllerFactory,
                @NonNull ReverseGeocodingController.Factory reverseGeocodingControllerFactory,
                @NonNull List<GeocodingProviderFactory> geocodingProviders) {
            mParent = smartLocation;
            mGeocodingControllerFactory = geocodingControllerFactory;
            mReverseGeocodingControllerFactory = reverseGeocodingControllerFactory;
            mGeocodingProviders = geocodingProviders;
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
            return mReverseGeocodingControllerFactory.create(
                    mParent.context,
                    location,
                    mMaxResults,
                    listener,
                    listener,
                    mGeocodingProviders,
                    mParent.logger).start();
        }

        @NonNull
        public GeocodingController findLocationByName(
                @NonNull String name,
                @NonNull GeocodingUpdatedListener listener) {
            return mGeocodingControllerFactory.create(
                    mParent.context,
                    name,
                    mMaxResults,
                    listener,
                    listener,
                    mGeocodingProviders,
                    mParent.logger).start();
        }
    }

    public static class GeofencingBuilder {
        @NonNull private final SmartLocation mParent;
        @NonNull private final List<GeofencingProviderFactory> mGeofencingProviders;
        @NonNull private final GeofencingBaseController.Factory mGeofencingControllerFactory;
        @Nullable private OnAllProvidersFailed mProvidersFailed;

        public GeofencingBuilder(
                @NonNull SmartLocation smartLocation,
                @NonNull final GeofencingBaseController.Factory geofencingControllerFactory,
                @NonNull List<GeofencingProviderFactory> geofencingProviders) {
            mParent = smartLocation;
            mGeofencingControllerFactory = geofencingControllerFactory;
            mGeofencingProviders = geofencingProviders;
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
            return mGeofencingControllerFactory.createAddController(
                    mParent.context,
                    orDefault(mProvidersFailed, EMPTY),
                    mGeofencingProviders,
                    mParent.logger).addGeofences(request, pendingIntent);
        }

        @NonNull
        public GeofencingRemoveController removeGeofences(@NonNull List<String> geofenceIds) {
            return mGeofencingControllerFactory.createRemoveController(
                    mParent.context,
                    orDefault(mProvidersFailed, EMPTY),
                    mGeofencingProviders,
                    mParent.logger).removeGeofence(geofenceIds);
        }

        @NonNull
        public GeofencingRemoveController removeGeofences(@NonNull PendingIntent pendingIntent) {
            return mGeofencingControllerFactory.createRemoveController(
                    mParent.context,
                    orDefault(mProvidersFailed, EMPTY),
                    mGeofencingProviders,
                    mParent.logger).removeGeofence(pendingIntent);
        }
    }
}
