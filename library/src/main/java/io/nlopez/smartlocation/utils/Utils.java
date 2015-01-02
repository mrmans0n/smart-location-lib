package io.nlopez.smartlocation.utils;

import android.content.SharedPreferences;
import android.location.Location;

/**
 * Created by Nacho L. on 15/11/13.
 */
public class Utils {

    public static final String LATITUDE_ID = "_latitude";
    public static final String LONGITUDE_ID = "_longitude";
    public static final String ACCURACY_ID = "_accuracy";
    public static final String SPEED_ID = "_speed";
    public static final String TIME_ID = "_time";
    public static final String BEARING_ID = "_bearing";

    private Utils() {
    }

    public static double getDoubleFrom1E6(int number) {
        return (double) number / 1E6;
    }

    public static int getInt1E6FromDouble(double number) {
        return (int) (number * 1E6);
    }

    public static void storeLocationInPreferences(SharedPreferences preferences, Location location, String prefix) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(prefix + LATITUDE_ID, (float) location.getLatitude());
        editor.putFloat(prefix + LONGITUDE_ID, (float) location.getLongitude());
        editor.putFloat(prefix + ACCURACY_ID, location.getAccuracy());
        editor.putFloat(prefix + SPEED_ID, location.getSpeed());
        editor.putLong(prefix + TIME_ID, location.getTime());
        editor.putFloat(prefix + BEARING_ID, location.getBearing());
        editor.apply();
    }

    public static Location getLocationFromPreferences(SharedPreferences preferences, String prefix) {
        if (preferences.contains(prefix + LATITUDE_ID) && preferences.contains(prefix + LONGITUDE_ID)) {
            Location location = new Location("prefs");
            location.setLatitude(preferences.getFloat(prefix + LATITUDE_ID, 0));
            location.setLongitude(preferences.getFloat(prefix + prefix + LONGITUDE_ID, 0));
            location.setAccuracy(preferences.getFloat(prefix + ACCURACY_ID, 0));
            location.setSpeed(preferences.getFloat(prefix + SPEED_ID, 0));
            location.setTime(preferences.getLong(prefix + TIME_ID, location.getTime()));
            location.setBearing(preferences.getFloat(prefix + BEARING_ID, location.getBearing()));
            return location;
        } else {
            return null;
        }
    }

}
