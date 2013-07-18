package com.mobivery.greent.smartlocation;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by Nacho L. on 17/07/13.
 */
public enum UpdateStrategy {
    /**
     * Update strategy best for navigation, with fast paced accurate fixes
     */
    NAVIGATION(1000, 1000, LocationRequest.PRIORITY_HIGH_ACCURACY),
    /**
     * Update strategy best for most situations, with relatively fast updates and using the best
     * available method for location, WiFi - network - satellite
     */
    BEST_EFFORT(10000, 5000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
    /**
     * Update strategy for the minimum battery consumption, it will piggyback to other application
     * requests.
     */
    LAZY(30000, 30000, LocationRequest.PRIORITY_NO_POWER);

    private final long updateInterval;
    private final long fastestInterval;
    private final int locationRequestPriority;

    UpdateStrategy(long secondInterval, long fastestInterval, int locationRequestPriority) {
        this.updateInterval = secondInterval;
        this.fastestInterval = fastestInterval;
        this.locationRequestPriority = locationRequestPriority;
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

}
