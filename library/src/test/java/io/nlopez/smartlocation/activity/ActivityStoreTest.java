package io.nlopez.smartlocation.activity;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.location.DetectedActivity;

import org.junit.Assert;
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
public class ActivityStoreTest {

    private static final DetectedActivity TEST_ACTIVITY = new DetectedActivity(DetectedActivity.UNKNOWN, 100);
    private static final String TEST_ACTIVITY_ID = "test_activity_1";

    @Test
    public void test_activity_store_full_cycle() {
        ActivityStore store = new ActivityStore(RuntimeEnvironment.application.getApplicationContext());
        store.setPreferences(getSharedPreferences());

        Assert.assertNull(store.get(TEST_ACTIVITY_ID));

        store.put(TEST_ACTIVITY_ID, TEST_ACTIVITY);
        DetectedActivity storedActivity = store.get(TEST_ACTIVITY_ID);
        Assert.assertEquals(storedActivity.getConfidence(), TEST_ACTIVITY.getConfidence());
        Assert.assertEquals(storedActivity.getType(), TEST_ACTIVITY.getType());
        Assert.assertEquals(storedActivity.getVersionCode(), TEST_ACTIVITY.getVersionCode());

        store.remove(TEST_ACTIVITY_ID);
        Assert.assertNull(store.get(TEST_ACTIVITY_ID));
    }

    private SharedPreferences getSharedPreferences() {
        return RuntimeEnvironment.application.getApplicationContext().getSharedPreferences("test_prefs",
                                                                                           Context.MODE_PRIVATE);
    }
}
