package io.nlopez.smartlocation.geofencing;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.nlopez.smartlocation.CustomTestRunner;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;

/**
 * Created by nacho on 1/9/15.
 */
@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
public class GeofencingStoreTest {

    private static final double DELTA = 1e-7;

    private static final String TEST_GEOFENCE_ID = "test_geofence_1";

    private GeofenceModel testGeofence;

    @Before
    public void setup() {
        testGeofence = new GeofenceModel.Builder("a_test_geofence")
                .setExpiration(1234)
                .setLatitude(50.505050)
                .setLongitude(-40.4040)
                .setRadius(12.34f)
                .setLoiteringDelay(100)
                .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    @Test
    public void test_geofencing_store_full_cycle() {
        GeofencingStore store = new GeofencingStore(RuntimeEnvironment.application.getApplicationContext());
        store.setPreferences(getSharedPreferences());

        Assert.assertNull(store.get(TEST_GEOFENCE_ID));

        store.put(TEST_GEOFENCE_ID, testGeofence);
        GeofenceModel geofenceModel = store.get(TEST_GEOFENCE_ID);
        Assert.assertEquals(geofenceModel.getLatitude(), testGeofence.getLatitude(), DELTA);
        Assert.assertEquals(geofenceModel.getLongitude(), testGeofence.getLongitude(), DELTA);
        Assert.assertEquals(geofenceModel.getExpiration(), testGeofence.getExpiration());
        Assert.assertEquals(geofenceModel.getRadius(), testGeofence.getRadius(), DELTA);
        Assert.assertEquals(geofenceModel.getTransition(), testGeofence.getTransition());
        Assert.assertEquals(geofenceModel.getLoiteringDelay(), testGeofence.getLoiteringDelay());

        store.remove(TEST_GEOFENCE_ID);
        Assert.assertNull(store.get(TEST_GEOFENCE_ID));
    }

    private SharedPreferences getSharedPreferences() {
        return RuntimeEnvironment.application.getApplicationContext().getSharedPreferences("test_prefs",
                                                                                           Context.MODE_PRIVATE);
    }
}
