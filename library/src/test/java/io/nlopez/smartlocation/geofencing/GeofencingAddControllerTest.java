package io.nlopez.smartlocation.geofencing;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.location.GeofencingRequest;

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

import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.utils.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link GeofencingAddController}
 */
@RunWith(RobolectricTestRunner.class)
public class GeofencingAddControllerTest {
    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Mock private Context mContext;
    @Mock private OnAllProvidersFailed mOnAllProvidersFailed;
    @Mock private GeofencingProviderFactory mGeofencingProviderFactory;
    @Mock private GeofencingProvider mGeofencingProvider;
    @Mock private Logger mLogger;
    @Mock private GeofencingRequest mGeofencingRequest;
    @Mock private PendingIntent mPendingIntent;
    @Captor private ArgumentCaptor<Provider.StatusListener> mStatusListenerArgumentCaptor;

    private GeofencingAddController mController;

    @Before
    public void setup() {
        when(mGeofencingProviderFactory.create(any(Context.class), any(Provider.StatusListener.class))).thenReturn(mGeofencingProvider);
        mController = createControllerForProviders(mGeofencingProviderFactory);
    }

    @Test
    public void testStartWithNoProvidersLaunchesOnAllProvidersFailed() {
        mController = createControllerForProviders();
        mController.addGeofences(mGeofencingRequest, mPendingIntent);
        verify(mOnAllProvidersFailed).onAllProvidersFailed();
    }

    @Test
    public void testStartDoesStartCurrentProvider() {
        mController.addGeofences(mGeofencingRequest, mPendingIntent);
        verify(mGeofencingProvider).addGeofences(mGeofencingRequest, mPendingIntent);
    }

    @Test
    public void testProviderFailsGoesToNext() {
        mController.addGeofences(mGeofencingRequest, mPendingIntent);
        verify(mGeofencingProviderFactory).create(any(Context.class), mStatusListenerArgumentCaptor.capture());
        final Provider.StatusListener listener = mStatusListenerArgumentCaptor.getValue();
        listener.onProviderFailed(mGeofencingProvider);
        verify(mGeofencingProvider).release();
        verify(mOnAllProvidersFailed).onAllProvidersFailed();
    }

    @NonNull
    private GeofencingAddController createControllerForProviders(@NonNull GeofencingProviderFactory... providers) {
        return new GeofencingAddController(
                mContext,
                mOnAllProvidersFailed,
                Arrays.asList(providers),
                mLogger);
    }
}