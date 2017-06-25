package io.nlopez.smartlocation.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Some methods for handling null stuff
 */
public class Nulls {

    private Nulls() {
        // ...
    }

    @NonNull
    public static <T> T notNull(@Nullable T value) {
        if (value == null) {
            throw new AssertionError("value is null");
        }
        return value;
    }

    public static <T> T orDefault(@Nullable T value, @NonNull T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
