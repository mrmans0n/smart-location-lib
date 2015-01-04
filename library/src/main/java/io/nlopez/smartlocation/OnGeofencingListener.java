package io.nlopez.smartlocation;

import com.google.android.gms.location.Geofence;

/**
 * Created by mrm on 4/1/15.
 */
public interface OnGeofencingListener {
    public void onGeofenceTransition(Geofence geofence, int transitionType);
}