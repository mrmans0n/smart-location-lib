package io.nlopez.smartlocation.location.providers;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.ServiceLocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.ServiceConnectionListener;

/**
 * Test double for a {@link ServiceLocationProvider}.
 *
 * @author abkaplan07
 */
public class TestServiceProvider implements ServiceLocationProvider {

    private ServiceConnectionListener listener;
    private int initCount;
    private int startCount;
    private int stopCount;
    private int lastLocCount;

    @Override
    public ServiceConnectionListener getServiceListener() {
        return listener;
    }

    @Override
    public void setServiceListener(ServiceConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void init(Context context, Logger logger) {
        initCount++;
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean
            singleUpdate) {
        startCount++;
    }

    @Override
    public void stop() {
        stopCount++;
    }

    @Override
    public Location getLastLocation() {
        lastLocCount++;
        return null;
    }

    public int getInitCount() {
        return initCount;
    }

    public int getStartCount() {
        return startCount;
    }

    public int getStopCount() {
        return stopCount;
    }

    public int getLastLocCount() {
        return lastLocCount;
    }

    /**
     * Simulate a service connection failure, and call
     * {@link ServiceConnectionListener#onConnectionFailed()}
     */
    public void simulateFailure() {
        if (listener != null) {
            listener.onConnectionFailed();
        }
    }
}
