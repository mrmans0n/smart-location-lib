package io.nlopez.smartlocation.activity.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.nlopez.smartlocation.CustomTestRunner;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

/**
 * Created by mrm on 10/1/15.
 */
@RunWith(CustomTestRunner.class)
public class ActivityParamsTest {

    private static final long INTERVAL = 1000;

    @Test
    public void test_activity_params_builder() {
        ActivityParams activityParams = new ActivityParams.Builder().setInterval(INTERVAL).build();

        Assert.assertEquals(activityParams.getInterval(), INTERVAL);
    }
}
