package io.nlopez.smartlocation.location.config;

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
public class LocationParamsTest {

    private static final LocationAccuracy ACCURACY = LocationAccuracy.HIGH;
    private static final long INTERVAL = 1000;
    private static final float DISTANCE = 1000f;
    private static final double DELTA = 1e-7;

    @Test
    public void test_location_params_builder() {
        LocationParams locationParams = new LocationParams.Builder()
                .setAccuracy(ACCURACY).setInterval(INTERVAL).setDistance(DISTANCE).build();

        Assert.assertEquals(locationParams.getAccuracy(), ACCURACY);
        Assert.assertEquals(locationParams.getDistance(), DISTANCE, DELTA);
        Assert.assertEquals(locationParams.getInterval(), INTERVAL);
    }
}
