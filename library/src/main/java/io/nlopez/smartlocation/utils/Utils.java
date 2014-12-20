package io.nlopez.smartlocation.utils;

/**
 * Created by Nacho L. on 15/11/13.
 */
public class Utils {

    private Utils() {
    }

    public static double getDoubleFrom1E6(int number) {
        return (double) number / 1E6;
    }

    public static int getInt1E6FromDouble(double number) {
        return (int) (number * 1E6);
    }

}
