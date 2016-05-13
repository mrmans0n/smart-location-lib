package io.nlopez.smartlocation.location.providers;

import android.support.annotation.NonNull;

import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.ServiceLocationProvider;
import io.nlopez.smartlocation.utils.ServiceConnectionListener;

/**
 * Created by findyr-akaplan on 5/12/16.
 */
class FallbackListenerWrapper implements ServiceConnectionListener {

    private final ServiceConnectionListener listener;
    private final MultiFallbackProvider fallbackProvider;
    private final ServiceLocationProvider childProvider;


    public FallbackListenerWrapper(@NonNull MultiFallbackProvider parentProvider, ServiceLocationProvider childProvider) {
        this.fallbackProvider = parentProvider;
        this.childProvider = childProvider;
        this.listener = childProvider.getServiceListener();
    }

    @Override
    public void onConnected() {
        if (listener != null) {
            listener.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended() {
        if (listener != null) {
            listener.onConnectionSuspended();
        }
        runFallback();

    }

    @Override
    public void onConnectionFailed() {
        if (listener != null) {
            listener.onConnectionFailed();
        }
        runFallback();
    }

    private void runFallback() {
        LocationProvider current = fallbackProvider.getCurrentProvider();
        if (current != null && current.equals(childProvider)) {
            fallbackProvider.fallbackProvider();
        }
    }
}
