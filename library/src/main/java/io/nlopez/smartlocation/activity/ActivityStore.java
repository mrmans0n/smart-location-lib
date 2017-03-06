package io.nlopez.smartlocation.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.common.Store;

/**
 * Stores activities
 */
public class ActivityStore implements Store<DetectedActivity> {

    private static final String PREFERENCES_FILE = "ACTIVITY_STORE";
    private static final String PREFIX_ID = ActivityStore.class.getCanonicalName() + ".KEY";
    private static final String ACTIVITY_ID = "ACTIVITY";
    private static final String CONFIDENCE_ID = "CONFIDENCE";

    private SharedPreferences preferences;

    public ActivityStore(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @VisibleForTesting
    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void put(String id, DetectedActivity activity) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getFieldKey(id, ACTIVITY_ID), activity.getType());
        editor.putInt(getFieldKey(id, CONFIDENCE_ID), activity.getConfidence());
        editor.apply();
    }

    @Override
    public DetectedActivity get(String id) {
        if (preferences != null && preferences.contains(getFieldKey(id, ACTIVITY_ID)) && preferences.contains(getFieldKey(id, CONFIDENCE_ID))) {
            int activity = preferences.getInt(getFieldKey(id, ACTIVITY_ID), DetectedActivity.UNKNOWN);
            int confidence = preferences.getInt(getFieldKey(id, CONFIDENCE_ID), 0);
            return new DetectedActivity(activity, confidence);
        } else {
            return null;
        }
    }

    @Override
    public void remove(String id) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getFieldKey(id, ACTIVITY_ID));
        editor.remove(getFieldKey(id, CONFIDENCE_ID));
        editor.apply();
    }

    private String getFieldKey(String id, String field) {
        return PREFIX_ID + "_" + id + "_" + field;
    }

}
