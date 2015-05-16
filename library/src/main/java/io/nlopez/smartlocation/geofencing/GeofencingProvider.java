package io.nlopez.smartlocation.geofencing;

import android.content.Context;

import java.util.List;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface GeofencingProvider {
    void init(Context context, Logger logger);

    void start(OnGeofencingTransitionListener listener);

    void addGeofence(GeofenceModel geofence);

    void addGeofences(List<GeofenceModel> geofenceList);

    void removeGeofence(String geofenceId);

    void removeGeofences(List<String> geofenceIds);

    void stop();

}
