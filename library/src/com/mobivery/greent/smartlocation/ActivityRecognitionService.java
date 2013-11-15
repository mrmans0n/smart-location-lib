package com.mobivery.greent.smartlocation;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by Nacho L. on 12/06/13.
 */
public class ActivityRecognitionService extends IntentService {

    public static final String PREFERENCES_FILE = "SMART_LOCATION_ACTIVITY_RECOGNITION_PREFERENCES";
    public static final String LAST_ACTIVITY_KEY = "LAST_ACTIVITY";
    public static final String LAST_ACTIVITY_CONFIDENCE_KEY = "LAST_ACTIVITY_CONFIDENCE";
    public static final String LAST_ACTIVITY_UPDATED_AT_KEY = "LAST_ACTIVITY_UPDATED_AT";

    private SharedPreferences sharedPreferences;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();

            if (getLastActivityType() != activityType && confidence >= ActivityRecognitionConstants.MINIMUM_ACTIVITY_CONFIDENCY) {
                broadcastNewActivity(activityType);
                storeLastActivityType(mostProbableActivity);
            }

        }
    }

    private void broadcastNewActivity(int activityType) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ActivityRecognitionConstants.ACTIVITY_CHANGED_INTENT);
        broadcastIntent.putExtra(ActivityRecognitionConstants.ACTIVITY_KEY, activityType);
        getApplicationContext().sendBroadcast(broadcastIntent);
    }

    private int getLastActivityType() {
        if (sharedPreferences == null) {
            sharedPreferences = getApplicationContext().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getInt(LAST_ACTIVITY_KEY, DetectedActivity.UNKNOWN);
    }

    private void storeLastActivityType(DetectedActivity activity) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_ACTIVITY_KEY, activity.getType());
        editor.putInt(LAST_ACTIVITY_CONFIDENCE_KEY, activity.getConfidence());
        editor.putLong(LAST_ACTIVITY_UPDATED_AT_KEY, System.currentTimeMillis());
        editor.commit();
    }
}
