package io.nlopez.smartlocation.activity.providers;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.ActivityProvider;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 3/1/15.
 */
public class GooglePlayServicesActivityProvider implements ActivityProvider {
    @Override
    public void init(Context context, SmartLocation.OnActivityUpdatedListener listener, Logger logger) {

    }

    @Override
    public void start(ActivityParams params) {

    }

    @Override
    public void stop() {

    }

    @Override
    public DetectedActivity getLastActivity() {
        return null;
    }
}
