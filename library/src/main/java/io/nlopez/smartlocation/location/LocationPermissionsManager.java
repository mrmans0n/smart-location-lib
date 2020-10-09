package io.nlopez.smartlocation.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;

import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

/**
 * Handles permissions related to location
 */
public class LocationPermissionsManager {
    private static LocationPermissionsManager sInstance;

    public static final int PERMISSIONS_REQUEST_CODE = 11111;

    @NonNull
    private final Logger mLogger;

    @NonNull
    private final ActivityCompatProxy mActivityCompatProxy;

    public static LocationPermissionsManager get() {
        if (sInstance == null) {
            sInstance = new LocationPermissionsManager(LoggerFactory.get(), new ActivityCompatProxy());
        }
        return sInstance;
    }

    @VisibleForTesting
    LocationPermissionsManager(@NonNull Logger logger, @NonNull ActivityCompatProxy activityCompatProxy) {
        mLogger = logger;
        mActivityCompatProxy = activityCompatProxy;
    }

    /**
     * Launches the system permission request dialog. Any further geofencingAction when the permissions are
     * granted (or not) should be done in the ActivityCompat#onRequestPermissionsResult callback,
     * with the requestCode value specified in {@link LocationPermissionsManager#PERMISSIONS_REQUEST_CODE}.
     *
     * @return TRUE if the permissions were already enabled, FALSE if the permissions had to be requested.
     */
    public boolean permissionsEnabledOrRequestPermissions(@NonNull Context context) {
        if (permissionsEnabled(context)) {
            mLogger.d("All necessary permissions are enabled");
            return true;
        }
        if (!Activity.class.isInstance(context)) {
            mLogger.w("Permissions dialog cannot be shown if the context passed is not from an Activity");
            return false;
        }
        mActivityCompatProxy.requestPermissions(
                context,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSIONS_REQUEST_CODE);
        return false;
    }

    /**
     * Checks for all necessary permissions for location (FINE and COARSE)
     */
    public boolean permissionsEnabled(@NonNull Context context) {
        final boolean fineEnabled = fineEnabled(context);
        mLogger.d("Permission check: ACCESS_FINE_LOCATION " + fineEnabled);
        final boolean coarseEnabled = coarseEnabled(context);
        mLogger.d("Permission check: ACCESS_COARSE_LOCATION " + fineEnabled);
        return fineEnabled && coarseEnabled;
    }

    private boolean fineEnabled(@NonNull Context context) {
        return mActivityCompatProxy.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean coarseEnabled(@NonNull Context context) {
        return mActivityCompatProxy.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Convenience proxy for testability
     */
    static class ActivityCompatProxy {
        int checkSelfPermission(@NonNull Context context, @NonNull String permission) {
            return ActivityCompat.checkSelfPermission(context, permission);
        }

        void requestPermissions(@NonNull Context context, @NonNull String[] permissions, int requestCode) {
            ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
        }
    }
}
