package io.nlopez.smartlocation.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import io.nlopez.smartlocation.common.Store;

public class LocationStore implements Store<Location> {

    static final String PROVIDER = "LocationStore";

    private static final String PREFERENCES_FILE = "LOCATION_STORE";
    private static final String PREFIX_ID = LocationStore.class.getCanonicalName() + ".KEY";
    private static final String PROVIDER_ID = "PROVIDER";
    private static final String LATITUDE_ID = "LATITUDE";
    private static final String LONGITUDE_ID = "LONGITUDE";
    private static final String ACCURACY_ID = "ACCURACY";
    private static final String ALTITUDE_ID = "ALTITUDE";
    private static final String SPEED_ID = "SPEED";
    private static final String TIME_ID = "TIME";
    private static final String BEARING_ID = "BEARING";

    @NonNull private SharedPreferences mPreferences;

    public LocationStore(@NonNull Context context) {
        this(context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE));
    }

    @VisibleForTesting
    LocationStore(@NonNull SharedPreferences preferences) {
        mPreferences = preferences;
    }

    @Override
    public void put(String id, Location location) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(getFieldKey(id, PROVIDER_ID), location.getProvider());
        editor.putLong(getFieldKey(id, LATITUDE_ID), Double.doubleToLongBits(location.getLatitude()));
        editor.putLong(getFieldKey(id, LONGITUDE_ID), Double.doubleToLongBits(location.getLongitude()));
        editor.putFloat(getFieldKey(id, ACCURACY_ID), location.getAccuracy());
        editor.putLong(getFieldKey(id, ALTITUDE_ID), Double.doubleToLongBits(location.getAltitude()));
        editor.putFloat(getFieldKey(id, SPEED_ID), location.getSpeed());
        editor.putLong(getFieldKey(id, TIME_ID), location.getTime());
        editor.putFloat(getFieldKey(id, BEARING_ID), location.getBearing());
        editor.apply();
    }

    @Override
    public Location get(String id) {
        if (mPreferences.contains(getFieldKey(id, LATITUDE_ID)) && mPreferences.contains(
                getFieldKey(id, LONGITUDE_ID))) {
            final Location location = new Location(mPreferences.getString(PROVIDER_ID, PROVIDER));
            location.setLatitude(Double.longBitsToDouble(mPreferences.getLong(getFieldKey(id, LATITUDE_ID), 0)));
            location.setLongitude(Double.longBitsToDouble(mPreferences.getLong(getFieldKey(id, LONGITUDE_ID), 0)));
            location.setAccuracy(mPreferences.getFloat(getFieldKey(id, ACCURACY_ID), 0));
            location.setAltitude(Double.longBitsToDouble(mPreferences.getLong(getFieldKey(id, ALTITUDE_ID), 0)));
            location.setSpeed(mPreferences.getFloat(getFieldKey(id, SPEED_ID), 0));
            location.setTime(mPreferences.getLong(getFieldKey(id, TIME_ID), 0));
            location.setBearing(mPreferences.getFloat(getFieldKey(id, BEARING_ID), 0));
            return location;
        } else {
            return null;
        }
    }

    @Override
    public void remove(String id) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(getFieldKey(id, PROVIDER_ID));
        editor.remove(getFieldKey(id, LATITUDE_ID));
        editor.remove(getFieldKey(id, LONGITUDE_ID));
        editor.remove(getFieldKey(id, ACCURACY_ID));
        editor.remove(getFieldKey(id, ALTITUDE_ID));
        editor.remove(getFieldKey(id, SPEED_ID));
        editor.remove(getFieldKey(id, TIME_ID));
        editor.remove(getFieldKey(id, BEARING_ID));
        editor.apply();
    }

    @NonNull
    private static String getFieldKey(String id, String field) {
        return PREFIX_ID + "_" + id + "_" + field;
    }

}
