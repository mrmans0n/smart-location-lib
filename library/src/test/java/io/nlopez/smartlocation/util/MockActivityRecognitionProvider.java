package io.nlopez.smartlocation.util;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by nacho on 1/9/15.
 */
public class MockActivityRecognitionProvider implements ActivityProvider {

    private OnActivityUpdatedListener listener;

    @Override
    public void init(Context context, Logger logger) {

    }

    @Override
    public void start(OnActivityUpdatedListener listener, ActivityParams params) {
        this.listener = listener;
    }

    @Override
    public void stop() {

    }

    @Override
    public DetectedActivity getLastActivity() {
        return new DetectedActivity(DetectedActivity.UNKNOWN, 100);
    }

    public void fakeEmitActivity(DetectedActivity detectedActivity) {
        listener.onActivityUpdated(detectedActivity);
    }
}
