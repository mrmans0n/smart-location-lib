package com.mobivery.greent.smartlocation;

/**
 * Created by MVY11 on 17/06/13.
 */
public class SmartLocationOptions {

    private String packageName = SmartLocation.DEFAULT_PACKAGE + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;
    private UpdateStrategy defaultUpdateStrategy = UpdateStrategy.BEST_EFFORT;
    private OnLocationUpdated onLocationUpdatedNewStrategy;

    public SmartLocationOptions() {
        onLocationUpdatedNewStrategy = new OnLocationUpdated() {
            @Override
            public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity) {
                // Default behavior: ignore the activity recognition
                return UpdateStrategy.BEST_EFFORT;
            }
        };
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIntentActionString() {
        return getPackageName() + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;
    }

    public UpdateStrategy getDefaultUpdateStrategy() {
        return defaultUpdateStrategy;
    }

    public void setDefaultUpdateStrategy(UpdateStrategy defaultUpdateStrategy) {
        this.defaultUpdateStrategy = defaultUpdateStrategy;
    }

    public OnLocationUpdated getOnLocationUpdatedNewStrategy() {
        return onLocationUpdatedNewStrategy;
    }

    public void setOnLocationUpdatedNewStrategy(OnLocationUpdated onLocationUpdatedNewStrategy) {
        this.onLocationUpdatedNewStrategy = onLocationUpdatedNewStrategy;
    }

    public interface OnLocationUpdated {
        public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity);
    }
}
