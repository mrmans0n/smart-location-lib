package io.nlopez.smartlocation.location.config;

import android.support.annotation.IntDef;

@IntDef({LocationAccuracy.LOWEST, LocationAccuracy.LOW, LocationAccuracy.MEDIUM, LocationAccuracy.HIGH})
public @interface LocationAccuracy {
    int LOWEST = 0;
    int LOW = 1;
    int MEDIUM = 2;
    int HIGH = 3;
}
