package com.mobivery.greent.smartlocation;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocationOptions {

    private static final long ONE_HOUR_IN_MILLISECONDS = 60*60*1000;

    private String packageName = SmartLocation.DEFAULT_PACKAGE;
    private UpdateStrategy defaultUpdateStrategy = UpdateStrategy.BEST_EFFORT;
    private long locationCacheValidity = ONE_HOUR_IN_MILLISECONDS;
    private long activityCacheValidity = ONE_HOUR_IN_MILLISECONDS;
    private OnActivityRecognizerUpdated onActivityRecognizerUpdatedNewStrategy;

    public SmartLocationOptions() {
        onActivityRecognizerUpdatedNewStrategy = new OnActivityRecognizerUpdated() {
            @Override
            public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity) {
                // Default behavior: ignore the activity recognition
                return UpdateStrategy.BEST_EFFORT;
            }
        };
    }

    /**
     * Retrieve the package name that will be used as prefix to the intents the library will launch
     *
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Establish a new package name to work as prefix to the library intents
     *
     * @param packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Get the intent action name that will be launched
     *
     * @return
     */
    public String getIntentActionString() {
        return getPackageName() + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;
    }

    /**
     * Get the strategy that will be used for updating the location fix
     *
     * @return
     */
    public UpdateStrategy getDefaultUpdateStrategy() {
        return defaultUpdateStrategy;
    }

    /**
     * Change the strategy to follow for obtaining the location fixes
     *
     * @param defaultUpdateStrategy
     */
    public void setDefaultUpdateStrategy(UpdateStrategy defaultUpdateStrategy) {
        this.defaultUpdateStrategy = defaultUpdateStrategy;
    }

    /**
     * Get the callback that will be executed on each activity update
     *
     * @return
     */
    public OnActivityRecognizerUpdated getOnActivityRecognizerUpdatedNewStrategy() {
        return onActivityRecognizerUpdatedNewStrategy;
    }

    /**
     * Set a new callback that will be executed on each activity update
     *
     * @param onActivityRecognizerUpdatedNewStrategy
     */
    public void setOnActivityRecognizerUpdatedNewStrategy(OnActivityRecognizerUpdated onActivityRecognizerUpdatedNewStrategy) {
        this.onActivityRecognizerUpdatedNewStrategy = onActivityRecognizerUpdatedNewStrategy;
    }

    /**
     * Obtain the location cache validity in milliseconds
     * @return
     */
    public long getLocationCacheValidity() {
        return locationCacheValidity;
    }

    /**
     * Set a new location cache validity in milliseconds
     * @param locationCacheValidity
     */
    public void setLocationCacheValidity(long locationCacheValidity) {
        this.locationCacheValidity = locationCacheValidity;
    }

    /**
     * Obtain the activity cache validity in milliseconds
     * @return
     */
    public long getActivityCacheValidity() {
        return activityCacheValidity;
    }

    /**
     * Set a new activity cache validity in milliseconds
     * @param activityCacheValidity
     */
    public void setActivityCacheValidity(long activityCacheValidity) {
        this.activityCacheValidity = activityCacheValidity;
    }

    public interface OnActivityRecognizerUpdated {
        public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity);
    }
}
