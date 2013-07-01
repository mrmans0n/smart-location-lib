package com.mobivery.greent.smartlocation;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Created by MVY11 on 12/06/13.
 */
public class ActivityRecognitionService extends IntentService {

    private static final String PREFERENCES_FILE = "ACTIVITY_RECOGNITION_PREFERENCES";
    private static final String LAST_ACTIVITY_KEY = "LAST_ACTIVITY";

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
                storeLastActivityType(activityType);
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

    private void storeLastActivityType(int activityType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_ACTIVITY_KEY, activityType);
        editor.commit();
    }
}
