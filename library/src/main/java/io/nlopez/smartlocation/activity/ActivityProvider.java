package io.nlopez.smartlocation.activity;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 3/1/15.
 */
public interface ActivityProvider {
    void init(Context context, Logger logger);

    void start(OnActivityUpdatedListener listener, ActivityParams params);

    void stop();

    DetectedActivity getLastActivity();
}
