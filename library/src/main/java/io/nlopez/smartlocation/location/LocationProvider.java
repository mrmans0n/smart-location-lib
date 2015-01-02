package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface LocationProvider {
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate, Logger loggingEnabled);

    public void start();

    public void stopUpdates();

    public Location getLastLocation();

}
