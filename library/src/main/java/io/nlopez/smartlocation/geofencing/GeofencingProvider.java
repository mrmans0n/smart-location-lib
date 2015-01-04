package io.nlopez.smartlocation.geofencing;

import android.content.Context;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface GeofencingProvider {
    public void init(Context context, Logger logger);

    public void start(OnGeofencingTransitionListener listener);

    public void addGeofence(GeofenceModel geofence);

    public void removeGeofence(String geofenceId);

    public void stop();

}
