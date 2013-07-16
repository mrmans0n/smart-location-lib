package com.mobivery.greent.smartlocation;

/**
 * Created by MVY11 on 17/06/13.
 */
public class SmartLocationOptions {

    private String packageName = SmartLocation.DEFAULT_PACKAGE + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIntentActionString() {
        return getPackageName() + SmartLocation.LOCATION_BROADCAST_INTENT_TRAIL;
    }
}
