package io.nlopez.smartlocation.location;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.nlopez.smartlocation.BuildConfig;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LocationController}
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class LocationControllerTest {
    private static final int TIMEOUT = 1000;
    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Mock private Context mContext;
    @Mock private OnLocationUpdatedListener mOnLocationUpdatedListener;
    @Mock private OnAllProvidersFailed mOnAllProvidersFailed;
    @Mock private LocationProviderParams mLocationProviderParams;
    @Mock private Logger mLogger;
    @Mock private LocationProviderFactory mLocationProviderFactory;
    @Mock private LocationProvider mLocationProvider;
    @Captor private ArgumentCaptor<LocationController.TimeoutableLocationUpdateListener> mTimeoutableCaptor;
    private Handler mHandler;
    private Looper mLooper;
    private LocationController mController;

    @Before
    public void setup() {
        mLooper = ShadowLooper.getMainLooper();
        mHandler = new Handler(mLooper);
        when(mLocationProviderFactory.create(any(Context.class), any(Provider.StatusListener.class))).thenReturn(mLocationProvider);

        mController = createControllerForProviders(mLocationProviderFactory);
    }

    @Test
    public void testStartWithNoProvidersLaunchesOnAllProvidersFailed() {
        mController = createControllerForProviders();
        mController.start();
        verify(mOnAllProvidersFailed).onAllProvidersFailed();
    }

    @Test
    public void testGetLastLocationWithoutStarting() {
        assertThat(mController.getLastLocation()).isNull();
    }

    @Test
    public void testStartDoesStartCurrentProvider() {
        mController.start();
        verify(mLocationProvider).start(
                any(LocationController.TimeoutableLocationUpdateListener.class),
                eq(mLocationProviderParams));
    }

    @Test
    public void testGetLastLocationReturnsProvidersLastLocation() {
        mController.start();
        mController.getLastLocation();
        verify(mLocationProvider).getLastLocation();
    }

    @Test
    public void testGetProviderReturnsProviderOnlyAfterStart() {
        assertThat(mController.getCurrentProvider()).isNull();
        mController.start();
        assertThat(mController.getCurrentProvider()).isEqualTo(mLocationProvider);
    }

    @Test
    public void testReleaseReleasesProvider() {
        mController.start();
        mController.release();
        verify(mLocationProvider).release();
    }

    @Test
    public void testTimeoutableListenerTimesOut() {
        ShadowLooper.pauseLooper(mLooper);
        mController.start();
        ShadowLooper.idleMainLooper(TIMEOUT, TimeUnit.MILLISECONDS);
        ShadowLooper.unPauseLooper(mLooper);
        // as we only have 1 provider, after it times out it will be released and the next one
        // (null) will be invoked, hence causing the on all providers failed signal.
        verify(mLocationProvider).release();
        verify(mOnAllProvidersFailed).onAllProvidersFailed();
    }

    @NonNull
    private LocationController createControllerForProviders(@NonNull LocationProviderFactory... providers) {
        return new LocationController(
                mContext,
                mOnLocationUpdatedListener,
                mOnAllProvidersFailed,
                mLocationProviderParams,
                TIMEOUT,
                Arrays.asList(providers),
                mLogger,
                mHandler);
    }
}