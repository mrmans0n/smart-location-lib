package io.nlopez.smartlocation.location.providers.playservices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.lang.ref.WeakReference;

import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

import static io.nlopez.smartlocation.utils.Nulls.orDefault;

/**
 * Handles permissions related to location settings dialog
 */
public class GooglePlayServicesLocationSettingsManager {
    private static GooglePlayServicesLocationSettingsManager sInstance;
    public static final int REQUEST_START_LOCATION_FIX = 10001;
    public static final int CHECK_SETTINGS_REQUEST_CODE = 20001;

    @NonNull private final Logger mLogger;
    @NonNull private final SettingsApiProxy mSettingsApiProxy;
    @NonNull private Listener mListener = Listener.EMPTY;

    private boolean mShouldCheckSettings = true;
    private boolean mAllowNever;
    private boolean mFulfilledCheckSettings;

    @NonNull
    public static GooglePlayServicesLocationSettingsManager get() {
        if (sInstance == null) {
            sInstance = new GooglePlayServicesLocationSettingsManager(LoggerFactory.get(), new SettingsApiProxy());
        }
        return sInstance;
    }

    private GooglePlayServicesLocationSettingsManager(@NonNull Logger logger, @NonNull SettingsApiProxy settingsApiProxy) {
        mLogger = logger;
        mSettingsApiProxy = settingsApiProxy;
    }

    public boolean maybeShowLocationSettingsDialog(@NonNull GoogleApiClient client, @NonNull LocationRequest locationRequest, @NonNull Context context) {
        final boolean result = mShouldCheckSettings && !mFulfilledCheckSettings;
        if (result) {
            checkLocationSettings(client, locationRequest, context);
        }
        return result;
    }

    private void checkLocationSettings(@NonNull GoogleApiClient client, @NonNull LocationRequest locationRequest, @NonNull Context context) {
        final LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .setAlwaysShow(mAllowNever)
                .addLocationRequest(locationRequest)
                .build();
        mSettingsApiProxy.checkLocationSettings(client, request)
                .setResultCallback(new SettingsResultCallback(context, mLogger, mListener));
    }

    /**
     * Should be called from the containing {@link Activity} in its onActivityResult method.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_SETTINGS_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    mLogger.i("User agreed to make required location settings changes.");
                    mFulfilledCheckSettings = true;
                    mListener.onSettingsSuccess();
                    break;
                case Activity.RESULT_CANCELED:
                    mLogger.i("User chose not to make required location settings changes.");
                    mListener.onSettingsFailed();
                    break;
            }
        } else if (requestCode == REQUEST_START_LOCATION_FIX) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    mLogger.i("User fixed the problem.");
                    mListener.onSettingsSuccess();
                    break;
                case Activity.RESULT_CANCELED:
                    mLogger.i("User chose not to fix the problem.");
                    mListener.onSettingsFailed();
                    break;
            }
        }
    }

    /**
     * Whether or not we should check Android's location settings dialog if we want to make sure
     * that we have location enabled in the device.
     * Default is TRUE.
     */
    public void setShouldCheckSettings(boolean shouldCheckSettings) {
        mShouldCheckSettings = shouldCheckSettings;
    }

    /**
     * Whether to allow the "NEVER" button to be shown in the location enable dialog.
     * Default is FALSE.
     */
    public void setAllowNeverButton(boolean allowNever) {
        mAllowNever = allowNever;
    }

    public void setListener(@Nullable Listener listener) {
        mListener = orDefault(listener, Listener.EMPTY);
    }

    @VisibleForTesting
    static class SettingsApiProxy {
        PendingResult<LocationSettingsResult> checkLocationSettings(@NonNull GoogleApiClient client, @NonNull LocationSettingsRequest request) {
            return LocationServices.SettingsApi.checkLocationSettings(client, request);
        }
    }

    static class SettingsResultCallback implements ResultCallback<LocationSettingsResult> {

        @NonNull private final WeakReference<Context> mContextRef;
        @NonNull private final Logger mLogger;
        @NonNull private final Listener mListener;

        public SettingsResultCallback(@NonNull Context context, @NonNull Logger logger, @NonNull Listener listener) {
            mLogger = logger;
            mContextRef = new WeakReference<>(context);
            mListener = listener;
        }

        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
            final Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    mLogger.d("All location settings are satisfied.");
                    mListener.onSettingsSuccess();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    mLogger.w("Location settings are not satisfied. Show the user a dialog to " +
                            "upgrade location settings. You should hook into the Activity onActivityResult and call this provider's onActivityResult method for continuing this call flow. ");
                    final Context context = mContextRef.get();
                    if (context == null || context instanceof Activity) {
                        // This is the only instance here where we will not emit judgement on settings results,
                        // we will be launching an activity to try to fix it and its result will decide what to do.
                        mLogger.w(
                                "Unable to register, but we can solve this - will startActivityForResult. You should hook into the Activity onActivityResult and call this provider's onActivityResult method for continuing this call flow.");
                        try {
                            status.startResolutionForResult((Activity) context, REQUEST_START_LOCATION_FIX);
                        } catch (IntentSender.SendIntentException e) {
                            mLogger.e(e, "problem with startResolutionForResult");
                            mListener.onSettingsFailed();
                        }
                    } else {
                        mLogger.e("Couldnt use context or context was not an activity to launch the resolution needed for settings.");
                        mListener.onSettingsFailed();
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    mLogger.i("Location settings are inadequate, and cannot be fixed here. Dialog " +
                            "not created.");
                    mListener.onSettingsFailed();
                    break;
            }
        }
    }

    public interface Listener {
        void onSettingsSuccess();

        void onSettingsFailed();

        Listener EMPTY = new Listener() {
            @Override
            public void onSettingsSuccess() {

            }

            @Override
            public void onSettingsFailed() {

            }
        };
    }
}
