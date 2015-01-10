package io.nlopez.smartlocation;

import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

import static com.kageiit.robojava.RobojavaConfig.ASSETS;
import static com.kageiit.robojava.RobojavaConfig.MANIFEST;
import static com.kageiit.robojava.RobojavaConfig.RESOURCES;

/**
 * Created by mrm on 9/1/15.
 */
public class CustomTestRunner extends RobolectricTestRunner {

    private static final int TARGET_SDK_VERSION = 18;
    private static final AndroidManifest ANDROID_MANIFEST = getAndroidManifest();

    public CustomTestRunner(final Class<?> testClass) throws Exception {
        super(testClass);
    }

    @Override
    public AndroidManifest getAppManifest(Config config) {
        return ANDROID_MANIFEST;
    }

    private static AndroidManifest getAndroidManifest() {
        return new AndroidManifest(Fs.fileFromPath(MANIFEST),
                Fs.fileFromPath(RESOURCES),
                Fs.fileFromPath(ASSETS)) {
            @Override
            public int getTargetSdkVersion() {
                return TARGET_SDK_VERSION;
            }
        };
    }
}