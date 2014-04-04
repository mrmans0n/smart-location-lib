package io.nlopez.smartlocation;

/**
 * Created by Nacho L. on 12/06/13.
 */
public class ActivityRecognitionConstants {

    public static final String ACTIVITY_CHANGED_INTENT = "io.nlopez.smartlocation.ACTIVITY_CHANGED";
    public static final String ACTIVITY_KEY = "ACTIVITY";
    public static final String ACTIVITY_CONFIDENCE_KEY = "ACTIVITY_CONFIDENCE";

    // Detection interval between activities
    public static final int ACTIVITY_DETECTION_INTERVAL = 2000;

    // The percentage of confidence as threshold for a new activity detected
    public static final int MINIMUM_ACTIVITY_CONFIDENCY = 50;
}
