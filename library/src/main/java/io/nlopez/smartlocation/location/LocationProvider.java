package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface LocationProvider {
    public void init(Context context, SmartLocation.OnLocationUpdatedListener listener, Logger logger);

    public void start(LocationParams params, boolean singleUpdate);

    public void stop();

    public Location getLastLocation();

}
