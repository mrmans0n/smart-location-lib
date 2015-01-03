package io.nlopez.smartlocation.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;

/**
 * Created by mrm on 3/1/15.
 */
public class LocationStore {

    public static final String PROVIDER = "LocationStore";

    private static final String PREFERENCES_FILE = "LOCATION_STORE";
    private static final String PREFIX_ID = LocationStore.class.getCanonicalName() + ".KEY";
    private static final String LATITUDE_ID = "LATITUDE";
    private static final String LONGITUDE_ID = "LONGITUDE";
    private static final String ACCURACY_ID = "ACCURACY";
    private static final String SPEED_ID = "SPEED";
    private static final String TIME_ID = "TIME";
    private static final String BEARING_ID = "BEARING";

    private final SharedPreferences preferences;

    public LocationStore(@NonNull Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public void put(String id, Location location) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(getFieldKey(id, LATITUDE_ID), (float) location.getLatitude());
        editor.putFloat(getFieldKey(id, LONGITUDE_ID), (float) location.getLongitude());
        editor.putFloat(getFieldKey(id, ACCURACY_ID), location.getAccuracy());
        editor.putFloat(getFieldKey(id, SPEED_ID), location.getSpeed());
        editor.putLong(getFieldKey(id, TIME_ID), location.getTime());
        editor.putFloat(getFieldKey(id, BEARING_ID), location.getBearing());
        editor.commit();
    }

    public Location get(String id) {
        if (preferences != null && preferences.contains(getFieldKey(id, LATITUDE_ID)) && preferences.contains(getFieldKey(id, LONGITUDE_ID))) {
            Location location = new Location(PROVIDER);
            location.setLatitude(preferences.getFloat(getFieldKey(id, LATITUDE_ID), 0));
            location.setLongitude(preferences.getFloat(getFieldKey(id, LONGITUDE_ID), 0));
            location.setAccuracy(preferences.getFloat(getFieldKey(id, ACCURACY_ID), 0));
            location.setSpeed(preferences.getFloat(getFieldKey(id, SPEED_ID), 0));
            location.setTime(preferences.getLong(getFieldKey(id, TIME_ID), location.getTime()));
            location.setBearing(preferences.getFloat(getFieldKey(id, BEARING_ID), location.getBearing()));
            return location;
        } else {
            return null;
        }
    }

    public void remove(String id) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(getFieldKey(id, LATITUDE_ID));
        editor.remove(getFieldKey(id, LONGITUDE_ID));
        editor.remove(getFieldKey(id, ACCURACY_ID));
        editor.remove(getFieldKey(id, SPEED_ID));
        editor.remove(getFieldKey(id, TIME_ID));
        editor.remove(getFieldKey(id, BEARING_ID));
        editor.commit();
    }

    private String getFieldKey(String id, String field) {
        return PREFIX_ID + "_" + id + "_" + field;
    }

}
