package io.nlopez.smartlocation.util;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 8/1/15.
 */
public class MockLocationProvider implements LocationProvider {

    private OnLocationUpdatedListener listener;

    @Override
    public void init(Context context, Logger logger) {

    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.listener = listener;
    }

    @Override
    public void stop() {

    }

    @Override
    public Location getLastLocation() {
        return null;
    }


    public void fakeEmitLocation(Location location) {
        listener.onLocationUpdated(location);
    }
}
