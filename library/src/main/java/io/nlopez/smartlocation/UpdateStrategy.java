package io.nlopez.smartlocation;

import android.location.LocationManager;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by Nacho L. on 17/07/13.
 */
public enum UpdateStrategy {
    /**
     * Update strategy best for navigation, with fast paced accurate fixes. Fallback: GPS_PROVIDER
     */
    NAVIGATION(1000, 1000, LocationRequest.PRIORITY_HIGH_ACCURACY, LocationManager.GPS_PROVIDER, 150),
    /**
     * Update strategy best for most situations, with relatively fast updates and using the best
     * available method for location, WiFi - network - satellite. Fallback: NETWORK_PROVIDER
     */
    BEST_EFFORT(10000, 5000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, LocationManager.NETWORK_PROVIDER, 250),
    /**
     * Update strategy for the minimum battery consumption, it will piggyback to other application
     * requests. Fallback: PASSIVE_PROVIDER
     */
    LAZY(30000, 30000, LocationRequest.PRIORITY_NO_POWER, LocationManager.PASSIVE_PROVIDER, 1000);

    private final long updateInterval;
    private final long fastestInterval;
    private final int locationRequestPriority;
    private final String provider;
    private final int minDistance;

    UpdateStrategy(long secondInterval, long fastestInterval, int locationRequestPriority, String provider, int minDistance) {
        this.updateInterval = secondInterval;
        this.fastestInterval = fastestInterval;
        this.locationRequestPriority = locationRequestPriority;
        this.provider = provider;
        this.minDistance = minDistance;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public long getFastestInterval() {
        return fastestInterval;
    }

    public int getLocationRequestPriority() {
        return locationRequestPriority;
    }

    public String getProvider() {
        return provider;
    }

    public int getMinDistance() {
        return minDistance;
    }
}
