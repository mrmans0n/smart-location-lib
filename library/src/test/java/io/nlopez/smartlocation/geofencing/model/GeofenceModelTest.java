package io.nlopez.smartlocation.geofencing.model;

import com.google.android.gms.location.Geofence;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import io.nlopez.smartlocation.CustomTestRunner;

/**
 * Created by mrm on 10/1/15.
 */

@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
public class GeofenceModelTest {

    private static final double DELTA = 1e-7;
    private static final String GEOFENCE_ID = "id1";
    private static final int EXPIRATION = 1234;
    private static final double LATITUDE = 50.123456;
    private static final double LONGITUDE = -30.65312;
    private static final int LOITERING_DELAY = 100;
    private static final int RADIUS = 444;
    private static final int TRANSITION = Geofence.GEOFENCE_TRANSITION_EXIT;

    @Test
    public void test_geofence_model_creation() {
        final GeofenceModel model = new GeofenceModel.Builder(GEOFENCE_ID)
                .setExpiration(EXPIRATION)
                .setLatitude(LATITUDE)
                .setLongitude(LONGITUDE)
                .setRadius(RADIUS)
                .setTransition(TRANSITION)
                .setLoiteringDelay(LOITERING_DELAY)
                .build();

        Assert.assertEquals(model.getRequestId(), GEOFENCE_ID);
        Assert.assertEquals(model.getExpiration(), EXPIRATION);
        Assert.assertEquals(model.getLatitude(), LATITUDE, DELTA);
        Assert.assertEquals(model.getLongitude(), LONGITUDE, DELTA);
        Assert.assertEquals(model.getRadius(), RADIUS, DELTA);
        Assert.assertEquals(model.getTransition(), TRANSITION);
        Assert.assertEquals(model.getLoiteringDelay(), LOITERING_DELAY);
    }

    @Test
    public void test_geofence_model_to_geofence() {
        final GeofenceModel model = new GeofenceModel.Builder(GEOFENCE_ID)
                .setExpiration(EXPIRATION)
                .setLatitude(LATITUDE)
                .setLongitude(LONGITUDE)
                .setRadius(RADIUS)
                .setLoiteringDelay(LOITERING_DELAY)
                .setTransition(TRANSITION)
                .build();

        Geofence geofence = model.toGeofence();

        Assert.assertNotNull(geofence);
        Assert.assertEquals(geofence.getRequestId(), GEOFENCE_ID);
    }
}
