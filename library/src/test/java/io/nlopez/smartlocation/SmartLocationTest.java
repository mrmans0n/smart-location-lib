package io.nlopez.smartlocation;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.GeofencingRequest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.geocoding.GeocodingController;
import io.nlopez.smartlocation.geocoding.GeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingController;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geofencing.GeofencingAddController;
import io.nlopez.smartlocation.geofencing.GeofencingBaseController;
import io.nlopez.smartlocation.geofencing.GeofencingProviderFactory;
import io.nlopez.smartlocation.geofencing.GeofencingRemoveController;
import io.nlopez.smartlocation.location.LocationController;
import io.nlopez.smartlocation.location.LocationProviderFactory;
import io.nlopez.smartlocation.location.LocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Logger;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SmartLocation}
 */
@RunWith(RobolectricTestRunner.class)
public class SmartLocationTest {
    private static final int TIMEOUT = 1000;
    private static final int MAX_RESULTS = 10;
    private static final String GEOCODING_NAME = "somewhere";
    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Mock private Context mContext;
    @Mock private Logger mLogger;

    @Mock private LocationProviderFactory mLocationProviderFactory;
    @Mock private LocationController.Factory mLocationControllerFactory;
    @Mock private LocationController mLocationController;
    @Mock private LocationProviderParams mLocationProviderParams;
    @Mock private LocationUpdatedListener mLocationUpdatedListener;

    @Mock private GeocodingProviderFactory mGeocodingProviderFactory;
    @Mock private GeocodingController.Factory mGeocodingControllerFactory;
    @Mock private GeocodingController mGeocodingController;
    @Mock private ReverseGeocodingController.Factory mReverseGeocodingControllerFactory;
    @Mock private ReverseGeocodingController mReverseGeocodingController;
    @Mock private GeocodingUpdatedListener mGeocodingUpdatedListener;
    @Mock private ReverseGeocodingUpdatedListener mReverseGeocodingUpdatedListener;
    @Mock private Location mLocation;

    @Mock private GeofencingProviderFactory mGeofencingProviderFactory;
    @Mock private GeofencingBaseController.Factory mGeofencingControllerFactory;
    @Mock private GeofencingAddController mGeofencingAddController;
    @Mock private GeofencingRemoveController mGeofencingRemoveController;
    @Mock private PendingIntent mPendingIntent;
    @Mock private GeofencingRequest mGeofencingRequest;
    @Mock private OnAllProvidersFailed mOnAllProvidersFailed;

    private SmartLocation mSmartLocation;

    @Before
    public void setup() {
        mSmartLocation = new SmartLocation.Builder(mContext).logger(mLogger).build();
    }

    @Test
    public void testSmartLocationLocationReturnsLocationBuilder() {
        assertThat(mSmartLocation.location()).isInstanceOf(SmartLocation.LocationBuilder.class);
        assertThat(mSmartLocation.location(mLocationProviderFactory)).isInstanceOf(SmartLocation.LocationBuilder.class);
    }

    @Test
    public void testLocationStartCreatesNewLocationControllerWithProperArguments() {
        final SmartLocation.LocationBuilder locationBuilder = createLocationBuilderDefault();

        locationBuilder.start(mLocationUpdatedListener);
        verify(mLocationController).start();
    }

    @Test
    public void testLocationBuilderGetReturnsLocationBuilder() {
        final SmartLocation.LocationBuilder locationBuilder = createLocationBuilderDefault();
        assertThat(locationBuilder.get()).isEqualTo(locationBuilder);
    }

    @Test
    public void testDifferentLocationBuilderStopsStartedController() {
        final SmartLocation.LocationBuilder locationBuilder = createLocationBuilderDefault();
        locationBuilder.start(mLocationUpdatedListener);

        createLocationBuilderDefault().stop();
        verify(mLocationController).stop();
    }

    @NonNull
    private SmartLocation.LocationBuilder createLocationBuilderDefault() {
        return createLocationBuilder(createLocationProviderList());
    }

    @NonNull
    private List<LocationProviderFactory> createLocationProviderList() {
        final ArrayList<LocationProviderFactory> providers = new ArrayList<>();
        providers.add(mLocationProviderFactory);
        return providers;
    }

    @NonNull
    private SmartLocation.LocationBuilder createLocationBuilder(@NonNull List<LocationProviderFactory> providerList) {
        final SmartLocation.LocationBuilder locationBuilder =
                new SmartLocation.LocationBuilder(mSmartLocation, mLocationControllerFactory, providerList);
        locationBuilder.config(mLocationProviderParams);
        locationBuilder.timeout(TIMEOUT);

        when(mLocationControllerFactory.create(
                mContext,
                mLocationUpdatedListener,
                mLocationUpdatedListener,
                mLocationProviderParams,
                TIMEOUT,
                providerList,
                mLogger)).thenReturn(mLocationController);
        return locationBuilder;
    }

    @Test
    public void testSmartLocationGeocodingReturnsGeocodingBuilder() {
        assertThat(mSmartLocation.geocoding()).isInstanceOf(SmartLocation.GeocodingBuilder.class);
        assertThat(mSmartLocation.geocoding(mGeocodingProviderFactory)).isInstanceOf(SmartLocation.GeocodingBuilder.class);
    }

    @Test
    public void testGeocodingFindLocationByNameLaunchedWithProperArguments() {
        final List<GeocodingProviderFactory> providers = createGeocodingProvider();
        final SmartLocation.GeocodingBuilder geocodingBuilder = new SmartLocation.GeocodingBuilder(
                mSmartLocation,
                mGeocodingControllerFactory,
                mReverseGeocodingControllerFactory,
                providers);
        geocodingBuilder.maxResults(MAX_RESULTS);
        when(mGeocodingControllerFactory.create(
                mContext,
                GEOCODING_NAME,
                MAX_RESULTS,
                mGeocodingUpdatedListener,
                mGeocodingUpdatedListener,
                providers,
                mLogger)).thenReturn(mGeocodingController);
        geocodingBuilder.findLocationByName(GEOCODING_NAME, mGeocodingUpdatedListener);
        verify(mGeocodingController).start();
    }

    @Test
    public void testGeocodingFindNameByLocationLaunchedWithProperArguments() {
        final List<GeocodingProviderFactory> providers = createGeocodingProvider();
        final SmartLocation.GeocodingBuilder geocodingBuilder = new SmartLocation.GeocodingBuilder(
                mSmartLocation,
                mGeocodingControllerFactory,
                mReverseGeocodingControllerFactory,
                providers);
        geocodingBuilder.maxResults(MAX_RESULTS);
        when(mReverseGeocodingControllerFactory.create(
                mContext,
                mLocation,
                MAX_RESULTS,
                mReverseGeocodingUpdatedListener,
                mReverseGeocodingUpdatedListener,
                providers,
                mLogger)).thenReturn(mReverseGeocodingController);
        geocodingBuilder.findNameByLocation(mLocation, mReverseGeocodingUpdatedListener);
        verify(mReverseGeocodingController).start();
    }

    @NonNull
    private List<GeocodingProviderFactory> createGeocodingProvider() {
        final ArrayList<GeocodingProviderFactory> providers = new ArrayList<>();
        providers.add(mGeocodingProviderFactory);
        return providers;
    }

    @Test
    public void testSmartLocationGeofencingReturnsGeofencingBuilder() {
        assertThat(mSmartLocation.geofencing()).isInstanceOf(SmartLocation.GeofencingBuilder.class);
        assertThat(mSmartLocation.geofencing(mGeofencingProviderFactory)).isInstanceOf(SmartLocation.GeofencingBuilder.class);
    }

    @Test
    public void testAddGeofencesStartedWithProperArguments() {
        final List<GeofencingProviderFactory> geofencingProviders = createGeofencingProviders();
        final SmartLocation.GeofencingBuilder geofencingBuilder =
                new SmartLocation.GeofencingBuilder(mSmartLocation, mGeofencingControllerFactory, geofencingProviders);
        geofencingBuilder.failureListener(mOnAllProvidersFailed);
        when(mGeofencingControllerFactory.createAddController(mContext, mOnAllProvidersFailed, geofencingProviders, mLogger)).thenReturn(mGeofencingAddController);
        geofencingBuilder.addGeofences(mGeofencingRequest, mPendingIntent);
        verify(mGeofencingAddController).addGeofences(mGeofencingRequest, mPendingIntent);
    }

    @Test
    public void testRemoveGeofencesStartedWithProperArguments() {
        final List<GeofencingProviderFactory> geofencingProviders = createGeofencingProviders();
        final SmartLocation.GeofencingBuilder geofencingBuilder =
                new SmartLocation.GeofencingBuilder(mSmartLocation, mGeofencingControllerFactory, geofencingProviders);
        geofencingBuilder.failureListener(mOnAllProvidersFailed);
        when(mGeofencingControllerFactory.createRemoveController(mContext, mOnAllProvidersFailed, geofencingProviders, mLogger)).thenReturn(mGeofencingRemoveController);
        final List<String> idList = new ArrayList<>();
        geofencingBuilder.removeGeofences(idList);
        verify(mGeofencingRemoveController).removeGeofence(idList);
        geofencingBuilder.removeGeofences(mPendingIntent);
        verify(mGeofencingRemoveController).removeGeofence(mPendingIntent);
    }

    @NonNull
    private List<GeofencingProviderFactory> createGeofencingProviders() {
        final ArrayList<GeofencingProviderFactory> providers = new ArrayList<>();
        providers.add(mGeofencingProviderFactory);
        return providers;
    }
}