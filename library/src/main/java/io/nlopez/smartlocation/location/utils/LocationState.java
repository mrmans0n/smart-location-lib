package io.nlopez.smartlocation.location.utils;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Created by mrm on 9/4/15.
 */
public class LocationState {

    private static LocationState instance;
    private Context context;
    private LocationManager locationManager;

    private LocationState(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static LocationState with(Context context) {
        if (instance == null) {
            instance = new LocationState(context.getApplicationContext());
        }
        return instance;
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
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException ignored) {
                // This is ignored
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            String locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
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
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Indicates if location updates from mobile network signals are enabled.
     *
     * @return <code>true</code> if location can be determined from mobile network signals.
     */
    public boolean isNetworkAvailable() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Indicates if the "passive" location provider is enabled.
     *
     * @return <code>true</code> if location updates from other applications are enabled.
     */
    public boolean isPassiveAvailable() {
        return locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
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
        return !("0".equals(Settings.Secure.getString(context.getContentResolver(), Settings
                .Secure.ALLOW_MOCK_LOCATION)));
    }

}
