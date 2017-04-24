package io.nlopez.smartlocation;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.util.MockLocationProvider;
import io.nlopez.smartlocation.utils.Logger;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
public class LocationBuilderTest {

    private static final LocationProviderParams DEFAULT_PARAMS = LocationProviderParams.BEST_EFFORT;
    private static final boolean DEFAULT_SINGLE_UPDATE = false;

    private MockLocationProvider mockProvider;
    private OnLocationUpdatedListener locationUpdatedListener;

    @Before
    public void setup() {
        mockProvider = mock(MockLocationProvider.class);
        locationUpdatedListener = mock(OnLocationUpdatedListener.class);
    }

    @Test
    public void test_location_control_init() {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SmartLocation smartLocation = new SmartLocation.Builder(context).logging(false).preInitialize(false).build();
        SmartLocation.LocationBuilder locationBuilder = smartLocation.location(mockProvider);
        verifyZeroInteractions(mockProvider);

        smartLocation = new SmartLocation.Builder(context).logging(false).build();
        locationBuilder = smartLocation.location(mockProvider);
        verify(mockProvider).init(eq(context), any(Logger.class));
    }

    @Test
    public void test_location_control_start_defaults() {
        SmartLocation.LocationBuilder locationBuilder = createLocationControl();

        locationBuilder.start(locationUpdatedListener);
        verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, DEFAULT_SINGLE_UPDATE);
    }

    @Test
    public void test_location_control_start_only_once() {
        SmartLocation.LocationBuilder locationBuilder = createLocationControl();
        locationBuilder.once();

        locationBuilder.start(locationUpdatedListener);
        verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, true);
    }

    @Test
    public void test_location_control_start_continuous() {
        SmartLocation.LocationBuilder locationBuilder = createLocationControl();
        locationBuilder.once();
        locationBuilder.continuous();
        locationBuilder.start(locationUpdatedListener);
        verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, false);
    }

    @Test
    public void test_location_control_start_navigation() {
        SmartLocation.LocationBuilder locationBuilder = createLocationControl();
        locationBuilder.config(LocationProviderParams.NAVIGATION);

        locationBuilder.start(locationUpdatedListener);
        verify(mockProvider).start(eq(locationUpdatedListener), eq(LocationProviderParams.NAVIGATION), anyBoolean());
    }

    @Test
    public void test_location_control_get_last_location() {
        SmartLocation.LocationBuilder locationBuilder = createLocationControl();
        locationBuilder.getLastLocation();

        verify(mockProvider).getLastLocation();
    }

    @Test
    public void test_location_control_stop() {
        SmartLocation.LocationBuilder locationBuilder = createLocationControl();
        locationBuilder.stop();

        verify(mockProvider).stop();
    }

    private SmartLocation.LocationBuilder createLocationControl() {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SmartLocation smartLocation = new SmartLocation.Builder(context).logging(false).preInitialize(false).build();
        SmartLocation.LocationBuilder locationBuilder = smartLocation.location(mockProvider);
        return locationBuilder;
    }

}
