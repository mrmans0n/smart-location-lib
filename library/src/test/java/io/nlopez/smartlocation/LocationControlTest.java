package io.nlopez.smartlocation;

import android.content.Context;
import android.location.Location;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;

import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.rx.ObservableFactory;
import io.nlopez.smartlocation.util.MockLocationProvider;
import io.nlopez.smartlocation.utils.Logger;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
public class LocationControlTest {

    private static final LocationParams DEFAULT_PARAMS = LocationParams.BEST_EFFORT;
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
        SmartLocation.LocationControl locationControl = smartLocation.location(mockProvider);
        verifyZeroInteractions(mockProvider);

        smartLocation = new SmartLocation.Builder(context).logging(false).build();
        locationControl = smartLocation.location(mockProvider);
        verify(mockProvider).init(eq(context), any(Logger.class));
    }

    @Test
    public void test_location_control_start_defaults() {
        SmartLocation.LocationControl locationControl = createLocationControl();

        locationControl.start(locationUpdatedListener);
        verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, DEFAULT_SINGLE_UPDATE);
    }

    @Test
    public void test_location_control_start_only_once() {
        SmartLocation.LocationControl locationControl = createLocationControl();
        locationControl.oneFix();

        locationControl.start(locationUpdatedListener);
        verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, true);
    }

    @Test
    public void test_location_control_start_continuous() {
        SmartLocation.LocationControl locationControl = createLocationControl();
        locationControl.oneFix();
        locationControl.continuous();
        locationControl.start(locationUpdatedListener);
        verify(mockProvider).start(locationUpdatedListener, DEFAULT_PARAMS, false);
    }

    @Test
    public void test_location_control_start_navigation() {
        SmartLocation.LocationControl locationControl = createLocationControl();
        locationControl.config(LocationParams.NAVIGATION);

        locationControl.start(locationUpdatedListener);
        verify(mockProvider).start(eq(locationUpdatedListener), eq(LocationParams.NAVIGATION), anyBoolean());
    }

    @Test
    public void test_location_control_get_last_location() {
        SmartLocation.LocationControl locationControl = createLocationControl();
        locationControl.getLastLocation();

        verify(mockProvider).getLastLocation();
    }

    @Test
    public void test_location_control_stop() {
        SmartLocation.LocationControl locationControl = createLocationControl();
        locationControl.stop();

        verify(mockProvider).stop();
    }

    @Test
    public void test_observable_location() {
        TestSubscriber<Location> testSubscriber = new TestSubscriber<>();
        MockLocationProvider provider = new MockLocationProvider();
        Observable<Location> locationObservable = ObservableFactory.from(
                SmartLocation.with(RuntimeEnvironment.application.getApplicationContext())
                .location(provider)
        );
        locationObservable.subscribe(testSubscriber);

        Location location = new Location("bleh");
        provider.fakeEmitLocation(location);
        testSubscriber.assertNoErrors();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(location));
    }

    private SmartLocation.LocationControl createLocationControl() {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SmartLocation smartLocation = new SmartLocation.Builder(context).logging(false).preInitialize(false).build();
        SmartLocation.LocationControl locationControl = smartLocation.location(mockProvider);
        return locationControl;
    }

}
