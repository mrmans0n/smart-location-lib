package io.nlopez.smartlocation;

import com.google.android.gms.location.DetectedActivity;

public interface OnActivityUpdatedListener {
    void onActivityUpdated(DetectedActivity detectedActivity);
}