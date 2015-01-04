package io.nlopez.smartlocation.geofencing;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import io.nlopez.smartlocation.geofencing.model.GeofenceModel;

/**
 * Created by mrm on 3/1/15.
 */
public class GeofencingStore {

    private static final String PREFERENCES_FILE = "GEOFENCING_STORE";
    private static final String PREFIX_ID = GeofencingStore.class.getCanonicalName() + ".KEY";
    private static final String LATITUDE_ID = "LATITUDE";
    private static final String LONGITUDE_ID = "LONGITUDE";
    private static final String RADIUS_ID = "RADIUS";
    private static final String TRANSITION_ID = "TRANSITION";
    private static final String EXPIRATION_ID = "EXPIRATION";

    private final SharedPreferences preferences;

    public GeofencingStore(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public void put(String id, GeofenceModel geofenceModel) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(getFieldKey(id, LATITUDE_ID), (float) geofenceModel.getLatitude());
        editor.putFloat(getFieldKey(id, LONGITUDE_ID), (float) geofenceModel.getLongitude());
        editor.putFloat(getFieldKey(id, RADIUS_ID), geofenceModel.getRadius());
        editor.putFloat(getFieldKey(id, TRANSITION_ID), geofenceModel.getTransition());
        editor.putLong(getFieldKey(id, EXPIRATION_ID), geofenceModel.getExpiration());
        editor.commit();
    }

    public GeofenceModel get(String id) {
        if (preferences != null && preferences.contains(getFieldKey(id, LATITUDE_ID)) && preferences.contains(getFieldKey(id, LONGITUDE_ID))) {
            GeofenceModel.Builder builder = new GeofenceModel.Builder(id);
            builder.setLatitude(preferences.getFloat(getFieldKey(id, LATITUDE_ID), 0));
            builder.setLongitude(preferences.getFloat(getFieldKey(id, LONGITUDE_ID), 0));
            builder.setRadius(preferences.getFloat(getFieldKey(id, RADIUS_ID), 0));
            builder.setTransition(preferences.getInt(getFieldKey(id, TRANSITION_ID), 0));
            builder.setExpiration(preferences.getLong(getFieldKey(id, EXPIRATION_ID), 0));
            return builder.build();
        } else {
            return null;
        }
    }

    public void remove(String id) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getFieldKey(id, LATITUDE_ID));
        editor.remove(getFieldKey(id, LONGITUDE_ID));
        editor.remove(getFieldKey(id, RADIUS_ID));
        editor.remove(getFieldKey(id, TRANSITION_ID));
        editor.remove(getFieldKey(id, EXPIRATION_ID));
        editor.commit();
    }

    private String getFieldKey(String id, String field) {
        return PREFIX_ID + "_" + id + "_" + field;
    }

}
