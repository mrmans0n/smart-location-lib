package io.nlopez.smartlocation.utils;

/**
 * Created by mrm on 14/2/15.
 */
public interface GooglePlayServicesListener {
    void onConnected(android.os.Bundle bundle);

    void onConnectionSuspended(int i);

    void onConnectionFailed(com.google.android.gms.common.ConnectionResult connectionResult);

}
