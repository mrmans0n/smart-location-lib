package io.nlopez.smartlocation.activity.config;

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
public class ActivityParamsTest {

    private static final long INTERVAL = 1000;

    @Test
    public void test_activity_params_builder() {
        ActivityParams activityParams = new ActivityParams.Builder().setInterval(INTERVAL).build();

        Assert.assertEquals(activityParams.getInterval(), INTERVAL);
    }
}
