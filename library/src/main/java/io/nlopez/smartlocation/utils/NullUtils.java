package io.nlopez.smartlocation.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Some methods for handling null stuff
 */
public class NullUtils {

    private NullUtils() {
        // ...
    }

    public static <T> T getOrDefault(@Nullable T value, @NonNull T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
