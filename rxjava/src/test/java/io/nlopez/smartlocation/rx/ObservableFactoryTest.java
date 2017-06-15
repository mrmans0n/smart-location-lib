package io.nlopez.smartlocation.rx;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;
import io.reactivex.observers.TestObserver;

/**
 * Tests {@link ObservableFactory}
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ObservableFactoryTest {

    private TestActivityProvider mActivityProvider;
    private TestLocationProvider mLocationProvider;

    @Before
    public void setup() {
        mActivityProvider = new TestActivityProvider();
        mLocationProvider = new TestLocationProvider();
    }

    @Test
    public void test_observable_activity() {
        TestObserver<DetectedActivity> testObserver = ObservableFactory.from(
                SmartLocation.with(RuntimeEnvironment.application.getApplicationContext())
                        .activity(mActivityProvider)
        ).test();

        DetectedActivity detectedActivity = new DetectedActivity(DetectedActivity.UNKNOWN,100);
        mActivityProvider.fakeEmit(detectedActivity);
        testObserver.assertNoErrors();
        testObserver.assertValue(detectedActivity);
    }

    @Test
    public void test_observable_location() {
        TestObserver<Location> testObserver = ObservableFactory.from(
                SmartLocation.with(RuntimeEnvironment.application.getApplicationContext())
                        .location(mLocationProvider)
        ).test();

        Location location = new Location("bleh");
        mLocationProvider.fakeEmit(location);
        testObserver.assertNoErrors();
        testObserver.assertValue(location);
    }

    class TestActivityProvider implements ActivityProvider {

        private OnActivityUpdatedListener mListener;

        @Override
        public void init(Context context, Logger logger) {

        }

        @Override
        public void start(OnActivityUpdatedListener listener, ActivityParams params) {
            mListener = listener;
        }

        @Override
        public void stop() {

        }

        @Override
        public DetectedActivity getLastActivity() {
            return null;
        }

        public void fakeEmit(DetectedActivity activity) {
            mListener.onActivityUpdated(activity);
        }
    }

    class TestLocationProvider implements LocationProvider {

        private OnLocationUpdatedListener mListener;

        @Override
        public void init(Context context, Logger logger) {

        }

        @Override
        public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
            mListener = listener;
        }

        @Override
        public void stop() {

        }

        @Override
        public Location getLastLocation() {
            return null;
        }

        public void fakeEmit(Location location) {
            mListener.onLocationUpdated(location);
        }
    }
}
