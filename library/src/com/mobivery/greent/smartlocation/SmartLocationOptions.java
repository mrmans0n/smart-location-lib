package com.mobivery.greent.smartlocation;

/**
 * Created by Nacho L. on 17/06/13.
 */
public class SmartLocationOptions {

    private String packageName = SmartLocation.DEFAULT_PACKAGE;
    private UpdateStrategy defaultUpdateStrategy = UpdateStrategy.BEST_EFFORT;
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

    public interface OnActivityRecognizerUpdated {
        public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity);
    }
}
