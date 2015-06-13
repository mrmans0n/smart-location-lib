package io.nlopez.smartlocation.geofencing;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import io.nlopez.smartlocation.common.Store;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;

/**
 * Created by mrm on 3/1/15.
 */
public class GeofencingStore implements Store<GeofenceModel> {

    private static final String PREFERENCES_FILE = "GEOFENCING_STORE";
    private static final String PREFIX_ID = GeofencingStore.class.getCanonicalName() + ".KEY";
    private static final String LATITUDE_ID = "LATITUDE";
    private static final String LONGITUDE_ID = "LONGITUDE";
    private static final String RADIUS_ID = "RADIUS";
    private static final String TRANSITION_ID = "TRANSITION";
    private static final String EXPIRATION_ID = "EXPIRATION";
    private static final String LOITERING_DELAY_ID = "LOITERING_DELAY";

    private SharedPreferences preferences;

    public GeofencingStore(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @VisibleForTesting
    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void put(String id, GeofenceModel geofenceModel) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(getFieldKey(id, LATITUDE_ID), Double.doubleToLongBits(geofenceModel.getLatitude()));
        editor.putLong(getFieldKey(id, LONGITUDE_ID), Double.doubleToLongBits(geofenceModel.getLongitude()));
        editor.putFloat(getFieldKey(id, RADIUS_ID), geofenceModel.getRadius());
        editor.putInt(getFieldKey(id, TRANSITION_ID), geofenceModel.getTransition());
        editor.putLong(getFieldKey(id, EXPIRATION_ID), geofenceModel.getExpiration());
        editor.putInt(getFieldKey(id, LOITERING_DELAY_ID), geofenceModel.getLoiteringDelay());
        editor.apply();
    }

    @Override
    public GeofenceModel get(String id) {
        if (preferences != null && preferences.contains(getFieldKey(id, LATITUDE_ID)) && preferences.contains(getFieldKey(id, LONGITUDE_ID))) {
            GeofenceModel.Builder builder = new GeofenceModel.Builder(id);
            builder.setLatitude(Double.longBitsToDouble(preferences.getLong(getFieldKey(id, LATITUDE_ID), 0)));
            builder.setLongitude(Double.longBitsToDouble(preferences.getLong(getFieldKey(id, LONGITUDE_ID), 0)));
            builder.setRadius(preferences.getFloat(getFieldKey(id, RADIUS_ID), 0));
            builder.setTransition(preferences.getInt(getFieldKey(id, TRANSITION_ID), 0));
            builder.setExpiration(preferences.getLong(getFieldKey(id, EXPIRATION_ID), 0));
            builder.setLoiteringDelay(preferences.getInt(getFieldKey(id, LOITERING_DELAY_ID), 0));
            return builder.build();
        } else {
            return null;
        }
    }

    @Override
    public void remove(String id) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getFieldKey(id, LATITUDE_ID));
        editor.remove(getFieldKey(id, LONGITUDE_ID));
        editor.remove(getFieldKey(id, RADIUS_ID));
        editor.remove(getFieldKey(id, TRANSITION_ID));
        editor.remove(getFieldKey(id, EXPIRATION_ID));
        editor.remove(getFieldKey(id, LOITERING_DELAY_ID));
        editor.apply();
    }

    private String getFieldKey(String id, String field) {
        return PREFIX_ID + "_" + id + "_" + field;
    }

}
