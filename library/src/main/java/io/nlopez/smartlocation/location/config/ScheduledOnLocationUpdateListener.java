package io.nlopez.smartlocation.location.config;

import io.nlopez.smartlocation.OnLocationUpdatedListener;

/**
 * Created by andstepko on 1/4/17.
 */

public class ScheduledOnLocationUpdateListener {

    private OnLocationUpdatedListener listener;
    private LocationParams params;
    private boolean singleUpdate;

    public ScheduledOnLocationUpdateListener(OnLocationUpdatedListener listener,
                                             LocationParams params, boolean singleUpdate) {
        this.listener = listener;
        this.params = params;
        this.singleUpdate = singleUpdate;
    }

    public OnLocationUpdatedListener getListener() {
        return listener;
    }

    public LocationParams getParams() {
        return params;
    }

    public boolean isSingleUpdate() {
        return singleUpdate;
    }
}
