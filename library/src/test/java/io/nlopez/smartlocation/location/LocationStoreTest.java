package io.nlopez.smartlocation.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.nlopez.smartlocation.CustomTestRunner;

/**
 * Created by nacho on 1/9/15.
 */
@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
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

    private final Location testLocation = new Location("test");

    @Before
    public void setup() {
        testLocation.setAccuracy(ACCURACY);
        testLocation.setAltitude(ALTITUDE);
        testLocation.setBearing(BEARING);
        testLocation.setLatitude(LATITUDE);
        testLocation.setLongitude(LONGITUDE);
        testLocation.setSpeed(SPEED);
        testLocation.setTime(TIME);
    }

    @Test
    public void test_location_store_full_cycle() {
        LocationStore store = new LocationStore(RuntimeEnvironment.application.getApplicationContext());
        store.setPreferences(getSharedPreferences());

        Assert.assertNull(store.get(TEST_LOCATION_ID));

        store.put(TEST_LOCATION_ID, testLocation);
        Location storedLocation = store.get(TEST_LOCATION_ID);
        Assert.assertEquals(storedLocation.getAccuracy(), testLocation.getAccuracy(), DELTA);
        Assert.assertEquals(storedLocation.getAltitude(), testLocation.getAltitude(), DELTA);
        Assert.assertEquals(storedLocation.getBearing(), testLocation.getBearing(), DELTA);
        Assert.assertEquals(storedLocation.getLatitude(), testLocation.getLatitude(), DELTA);
        Assert.assertEquals(storedLocation.getLongitude(), testLocation.getLongitude(), DELTA);
        Assert.assertEquals(storedLocation.getSpeed(), testLocation.getSpeed(), DELTA);
        Assert.assertEquals(storedLocation.getTime(), testLocation.getTime());

        store.remove(TEST_LOCATION_ID);
        Assert.assertNull(store.get(TEST_LOCATION_ID));
    }

    private SharedPreferences getSharedPreferences() {
        return RuntimeEnvironment.application.getApplicationContext().getSharedPreferences("test_prefs",
                                                                                           Context.MODE_PRIVATE);
    }
}
