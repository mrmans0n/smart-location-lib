package io.nlopez.smartlocation.common;

import androidx.annotation.NonNull;

/**
 * Common set of actions for all providers, regardless of them being for location, activity, geocoding, etc.
 */
public interface Provider {
    void release();

    interface StatusListener {
        void onProviderFailed(@NonNull Provider provider);
    }
}
