package io.nlopez.smartlocation.utils;

/**
 * A callback interface to respond to connection events from 3rd party services.
 *
 * @author abkaplan07
 */
public interface ServiceConnectionListener {

    /**
     * Callback when a successful connection to a 3rd party service is made
     */
    void onConnected();

    /**
     * Callback when the connection to a 3rd party service is interrupted (network failure,
     * temporary outage, etc.)
     */
    void onConnectionSuspended();

    /**
     * Callback when the connection to a 3rd party service fails (missing libraries, bad API key,
     * etc.)
     */
    void onConnectionFailed();
}
