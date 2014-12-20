package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by mrm on 20/12/14.
 */
public interface LocationProvider {

    enum LocationRecurrence {ONCE, CONTINUOUS}

    enum LocationStrategy {NAVIGATION, BEST_EFFORT, LAZY}

    public void init(Context context, LocationProviderCallback callback, SmartLocation.OnLocationUpdatedListener listener, LocationStrategy strategy, boolean loggingEnabled);

    public void startForRecurrence(LocationRecurrence recurrence);

    public void stopUpdates();

    public Location getLastLocation();

}
