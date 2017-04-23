package io.nlopez.smartlocation;

import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;

public interface OnGeofencingTransitionListener {
    void onGeofenceTransition(TransitionGeofence transitionGeofence);
}