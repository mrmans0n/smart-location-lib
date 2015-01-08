package io.nlopez.smartlocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SmartLocationTest {


    @Test(expected = NullPointerException.class)
    public void test_smartlocation_null_context() {
        SmartLocation.with(null);
    }

}
