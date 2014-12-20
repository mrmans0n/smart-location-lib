package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface LocationProvider {
    enum LocationStrategy {NAVIGATION, BEST_EFFORT, LAZY}

    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, boolean oneFix, LocationStrategy strategy, Logger loggingEnabled);

    public void start();

    public void stopUpdates();

    public Location getLastLocation();

}
