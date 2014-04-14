package io.nlopez.smartlocation;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocationOptions {

    private static final long ONE_HOUR_IN_MILLISECONDS = 60 * 60 * 1000;
    private static final int DEFAULT_SECONDS_UNTIL_FALLBACK = 10;

    private String packageName = SmartLocation.DEFAULT_PACKAGE;
    private UpdateStrategy defaultUpdateStrategy = UpdateStrategy.BEST_EFFORT;
    private long locationCacheValidity = ONE_HOUR_IN_MILLISECONDS;
    private long activityCacheValidity = ONE_HOUR_IN_MILLISECONDS;
    private int secondsUntilFallback = DEFAULT_SECONDS_UNTIL_FALLBACK;
    private OnActivityRecognizerUpdated onActivityRecognizerUpdatedNewStrategy;

    private boolean showDebugging = true;

    public SmartLocationOptions() {
        onActivityRecognizerUpdatedNewStrategy = new OnActivityRecognizerUpdated() {
            @Override
            public UpdateStrategy getUpdateStrategyForActivity(DetectedActivity detectedActivity) {
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
     * @return
     */
    public SmartLocationOptions setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
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
     * @return
     */
    public SmartLocationOptions setDefaultUpdateStrategy(UpdateStrategy defaultUpdateStrategy) {
        this.defaultUpdateStrategy = defaultUpdateStrategy;
        return this;
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
     * @return
     */
    public SmartLocationOptions setOnActivityRecognizerUpdatedNewStrategy(OnActivityRecognizerUpdated onActivityRecognizerUpdatedNewStrategy) {
        this.onActivityRecognizerUpdatedNewStrategy = onActivityRecognizerUpdatedNewStrategy;
        return this;
    }

    /**
     * Obtain the location cache validity in milliseconds
     *
     * @return
     */
    public long getLocationCacheValidity() {
        return locationCacheValidity;
    }

    /**
     * Set a new location cache validity in milliseconds
     *
     * @param locationCacheValidity
     * @return
     */
    public SmartLocationOptions setLocationCacheValidity(long locationCacheValidity) {
        this.locationCacheValidity = locationCacheValidity;
        return this;
    }

    /**
     * Obtain the activity cache validity in milliseconds
     *
     * @return
     */
    public long getActivityCacheValidity() {
        return activityCacheValidity;
    }

    /**
     * Set a new activity cache validity in milliseconds
     *
     * @param activityCacheValidity
     * @return
     */
    public SmartLocationOptions setActivityCacheValidity(long activityCacheValidity) {
        this.activityCacheValidity = activityCacheValidity;
        return this;
    }

    /**
     * Activate or deactivate the debugging flag
     *
     * @param showDebugging
     * @return
     */
    public SmartLocationOptions setDebugging(boolean showDebugging) {
        this.showDebugging = showDebugging;
        return this;
    }

    /**
     * Get the current status of the showDebugging flag
     *
     * @return
     */
    public boolean isDebugging() {
        return showDebugging;
    }

    /**
     * Gets the number of seconds to wait for the Fused Location Provider
     *
     * @return
     */
    public int getSecondsUntilFallback() {
        return secondsUntilFallback;
    }

    /**
     * Sets the number of seconds to wait for the Fused Location Provider
     *
     * @param secondsUntilFallback
     * @return
     */
    public SmartLocationOptions setSecondsUntilFallback(int secondsUntilFallback) {
        this.secondsUntilFallback = secondsUntilFallback;
        return this;
    }

    /**
     * Interface for setting the current strategy based on the detected activity
     */
    public interface OnActivityRecognizerUpdated {
        public UpdateStrategy getUpdateStrategyForActivity(DetectedActivity detectedActivity);
    }

}
