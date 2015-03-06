package io.nlopez.smartlocation;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mrm on 9/1/15.
 */
public class CustomTestRunner extends RobolectricTestRunner {

    public CustomTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    protected AndroidManifest getAppManifest(Config config) {
        String myAppPath = CustomTestRunner.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();
        String manifestPath = myAppPath + "../../../../../src/main/AndroidManifest.xml";
        String resPath = myAppPath + "../../../../../src/main/res";
        String assetPath = myAppPath + "../../../../../src/main/assets";
        return createAppManifest(Fs.fileFromPath(manifestPath), Fs.fileFromPath(resPath), Fs.fileFromPath(assetPath));
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
        return new MavenAndroidManifest(manifestFile, resDir, assetsDir);
    }

    public static class MavenAndroidManifest extends AndroidManifest {

        public MavenAndroidManifest(FsFile androidManifestFile, FsFile resDirectory, FsFile assetsDirectory) {
            super(androidManifestFile, resDirectory, assetsDirectory);
        }

        public MavenAndroidManifest(FsFile libraryBaseDir) {
            super(libraryBaseDir, null, null);
        }

        @Override
        protected List<FsFile> findLibraries() {
            // Try unpack folder from maven.
            FsFile unpack = getBaseDir().join("target/unpacked-libs");
            if (unpack.exists()) {
                FsFile[] libs = unpack.listFiles();
                if (libs != null) {
                    return Arrays.asList(libs);
                }
            }
            return Collections.emptyList();
        }

        @Override
        protected AndroidManifest createLibraryAndroidManifest(FsFile libraryBaseDir) {
            return new MavenAndroidManifest(libraryBaseDir);
        }
    }
}