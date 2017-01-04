package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;

import java.util.List;
import java.util.Map;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.config.ScheduledOnLocationUpdateListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface LocationProvider {

    void init(Context context, Logger logger);

    void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate);

    void start(List<ScheduledOnLocationUpdateListener> scheduledListeners);

    void addListener(OnLocationUpdatedListener listener,
                     LocationParams params, boolean singleUpdate);

    boolean removeListener(OnLocationUpdatedListener listener);

    void stop();

    Location getLastLocation();

}
