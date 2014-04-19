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
    NAVIGATION(500, 100, LocationRequest.PRIORITY_HIGH_ACCURACY, LocationManager.GPS_PROVIDER, 0),
    /**
     * Update strategy best for most situations, with relatively fast updates and using the best
     * available method for location, WiFi - network - satellite. Fallback: NETWORK_PROVIDER
     */
    BEST_EFFORT(2500, 5000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, LocationManager.NETWORK_PROVIDER, 150),
    /**
     * Update strategy for the minimum battery consumption, it will piggyback to other application
     * requests. Fallback: PASSIVE_PROVIDER
     */
    LAZY(30000, 30000, LocationRequest.PRIORITY_NO_POWER, LocationManager.PASSIVE_PROVIDER, 500);

    private final long interval;
    private final long fastestInterval;
    private final int locationRequestPriority;
    private final String provider;
    private final int smallestDisplacement;

    UpdateStrategy(long interval, long fastestInterval, int locationRequestPriority, String provider, int minDistance) {
        this.interval = interval;
        this.fastestInterval = fastestInterval;
        this.locationRequestPriority = locationRequestPriority;
        this.provider = provider;
        this.smallestDisplacement = minDistance;
    }

    public long getInterval() {
        return interval;
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

    public int getSmallestDisplacement() {
        return smallestDisplacement;
    }
}
