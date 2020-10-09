package io.nlopez.smartlocation.location.util;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import static io.nlopez.smartlocation.utils.Nulls.notNull;

public class LocationHelpers {
    @NonNull private Context mContext;
    @NonNull private LocationManager mLocationManager;

    private LocationHelpers(@NonNull Context context) {
        mContext = context;
        mLocationManager = notNull((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }

    @NonNull
    public static LocationHelpers with(@NonNull Context context) {
        return new LocationHelpers(context.getApplicationContext());
    }

    /**
     * Indicates if location services are enabled for the device.
     *
     * @return <code>true</code> if the user has turned on location services.
     */
    public boolean locationServicesEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = Settings.Secure.LOCATION_MODE_OFF;

            try {
                locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException ignored) {
                // This is ignored
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            String locationProviders = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /**
     * Indicates if any <em>active</em> location provider is enabled.
     *
     * @return <code>true</code> if an active location provider (network, GPS) is enabled.
     */
    public boolean isAnyProviderAvailable() {
        return isGpsAvailable() || isNetworkAvailable();
    }

    /**
     * Indicates if GPS location updates are enabled.
     *
     * @return <code>true</code> if GPS location updates are enabled.
     */
    public boolean isGpsAvailable() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Indicates if location updates from mobile network signals are enabled.
     *
     * @return <code>true</code> if location can be determined from mobile network signals.
     */
    public boolean isNetworkAvailable() {
        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Indicates if the "passive" location provider is enabled.
     *
     * @return <code>true</code> if location updates from other applications are enabled.
     */
    public boolean isPassiveAvailable() {
        return mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }

    /**
     * Indicates if the device allows mock locations.
     *
     * @return <code>true</code> if mock locations are enabled for the entire device.
     * @deprecated use {@link android.location.Location#isFromMockProvider()} instead for Android
     * KitKat devices and higher.
     */
    @Deprecated
    public boolean isMockSettingEnabled() {
        return !("0".equals(Settings.Secure.getString(mContext.getContentResolver(), Settings
                .Secure.ALLOW_MOCK_LOCATION)));
    }

}
