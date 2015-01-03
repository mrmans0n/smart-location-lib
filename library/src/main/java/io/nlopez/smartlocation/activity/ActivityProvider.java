package io.nlopez.smartlocation.activity;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 3/1/15.
 */
public interface ActivityProvider {
    public void init(Context context, SmartLocation.OnActivityUpdatedListener listener, Logger logger);

    public void start(ActivityParams params);

    public void stop();

    public DetectedActivity getLastActivity();
}
