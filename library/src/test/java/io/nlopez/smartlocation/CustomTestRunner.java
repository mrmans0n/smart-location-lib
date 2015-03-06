package io.nlopez.smartlocation;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

/**
 * Created by mrm on 9/1/15.
 */
public class CustomTestRunner extends RobolectricTestRunner {

    public CustomTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }


}