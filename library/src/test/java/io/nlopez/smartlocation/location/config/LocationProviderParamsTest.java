package io.nlopez.smartlocation.location.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import io.nlopez.smartlocation.CustomTestRunner;

@RunWith(CustomTestRunner.class)
@Config(manifest = Config.NONE)
public class LocationProviderParamsTest {

    private static final LocationAccuracy ACCURACY = LocationAccuracy.HIGH;
    private static final long INTERVAL = 1000;
    private static final float DISTANCE = 1000f;
    private static final double DELTA = 1e-7;

    @Test
    public void test_location_params_builder() {
        LocationProviderParams locationProviderParams = new LocationProviderParams.Builder()
                .accuracy(ACCURACY).interval(INTERVAL).distance(DISTANCE).build();

        Assert.assertEquals(locationProviderParams.accuracy, ACCURACY);
        Assert.assertEquals(locationProviderParams.distance, DISTANCE, DELTA);
        Assert.assertEquals(locationProviderParams.interval, INTERVAL);
    }
}
