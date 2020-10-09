package io.nlopez.smartlocation.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Tests {@link LocationStore}
 */
@RunWith(RobolectricTestRunner.class)
public class LocationStoreTest {

    private static final double DELTA = 1e-7;

    private static final String TEST_LOCATION_ID = "test_location_1";
    private static final float ACCURACY = 1.234f;
    private static final double ALTITUDE = 12.34;
    private static final float BEARING = 123f;
    private static final float SPEED = 321f;
    private static final double LATITUDE = -50.123456;
    private static final double LONGITUDE = 9.8765432;
    private static final int TIME = 987654321;

    private final Location testLocation = new Location(LocationStore.PROVIDER);
    private LocationStore mStore;

    @Before
    public void setup() {
        testLocation.setAccuracy(ACCURACY);
        testLocation.setAltitude(ALTITUDE);
        testLocation.setBearing(BEARING);
        testLocation.setLatitude(LATITUDE);
        testLocation.setLongitude(LONGITUDE);
        testLocation.setSpeed(SPEED);
        testLocation.setTime(TIME);
        mStore = new LocationStore(getSharedPreferences());
    }

    @Test
    public void testLocationStoreFullCircle() {
        assertThat(mStore.get(TEST_LOCATION_ID)).isNull();
        mStore.put(TEST_LOCATION_ID, testLocation);
        final Location location = mStore.get(TEST_LOCATION_ID);

        assertThat(location).isEqualToComparingFieldByField(testLocation);
        mStore.remove(TEST_LOCATION_ID);
        assertThat(mStore.get(TEST_LOCATION_ID)).isNull();
    }

    @NonNull
    private SharedPreferences getSharedPreferences() {
        return RuntimeEnvironment.application.getApplicationContext().getSharedPreferences("test_prefs",
                Context.MODE_PRIVATE);
    }
}
