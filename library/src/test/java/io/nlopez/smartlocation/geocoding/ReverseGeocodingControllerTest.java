package io.nlopez.smartlocation.geocoding;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

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

import java.util.Arrays;

import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.utils.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link GeocodingController}
 */
@RunWith(RobolectricTestRunner.class)
public class ReverseGeocodingControllerTest {
    private static final int MAX_RESULTS = 10;
    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Mock private GeocodingProviderFactory mGeocodingProviderFactory;
    @Mock private GeocodingProvider mGeocodingProvider;
    @Mock private OnReverseGeocodingListener mOnReverseGeocodingListener;
    @Mock private OnAllProvidersFailed mOnAllProvidersFailed;
    @Mock private Context mContext;
    @Mock private Logger mLogger;
    @Mock private Location mLocation;
    @Captor private ArgumentCaptor<Provider.StatusListener> mStatusListenerArgumentCaptor;

    private ReverseGeocodingController mController;

    @Before
    public void setup() {
        when(mGeocodingProviderFactory.create(any(Context.class), any(Provider.StatusListener.class))).thenReturn(mGeocodingProvider);
        mController = createControllerForProviders(mGeocodingProviderFactory);
    }

    @Test
    public void testStartWithNoProvidersLaunchesOnAllProvidersFailed() {
        mController = createControllerForProviders();
        mController.start();
        verify(mOnAllProvidersFailed).onAllProvidersFailed();
    }

    @Test
    public void testStartDoesStartCurrentProvider() {
        mController.start();
        verify(mGeocodingProvider).findNameByLocation(mLocation, mOnReverseGeocodingListener, MAX_RESULTS);
    }

    @Test
    public void testProviderFailsGoesToNext() {
        mController.start();
        verify(mGeocodingProviderFactory).create(any(Context.class), mStatusListenerArgumentCaptor.capture());
        final Provider.StatusListener listener = mStatusListenerArgumentCaptor.getValue();
        listener.onProviderFailed(mGeocodingProvider);
        verify(mGeocodingProvider).release();
        verify(mOnAllProvidersFailed).onAllProvidersFailed();
    }

    @NonNull
    private ReverseGeocodingController createControllerForProviders(@NonNull GeocodingProviderFactory... providers) {
        return new ReverseGeocodingController(mContext, mLocation, MAX_RESULTS, mOnReverseGeocodingListener, mOnAllProvidersFailed, Arrays.asList(providers), mLogger);
    }
}