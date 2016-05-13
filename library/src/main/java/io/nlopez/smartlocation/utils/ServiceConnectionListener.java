package io.nlopez.smartlocation.utils;

/**
 * Created by findyr-akaplan on 5/12/16.
 */
public interface ServiceConnectionListener {

    void onConnected();

    void onConnectionSuspended();

    void onConnectionFailed();
}
