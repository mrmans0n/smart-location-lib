package io.nlopez.smartlocation.geocoding;

import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;

/**
 * Basic listener for reverse geocoding updates.
 */
public abstract class ReverseGeocodingUpdatedListener implements OnReverseGeocodingListener, OnAllProvidersFailed {
    @Override
    public void onAllProvidersFailed() {
        // Override if you want to do something if all providers failed
    }
}