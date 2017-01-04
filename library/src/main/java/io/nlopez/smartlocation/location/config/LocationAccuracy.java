package io.nlopez.smartlocation.location.config;

/**
 * Created by mrm on 20/12/14.
 */
public enum LocationAccuracy {
    LOWEST,
    LOW,
    MEDIUM,
    HIGH;

    public static LocationAccuracy moreEffort(LocationAccuracy locationAccuracy1,
                                              LocationAccuracy locationAccuracy2) {
        return locationAccuracy1.ordinal() > locationAccuracy2.ordinal() ?
                locationAccuracy1 :locationAccuracy2;
    }
}
