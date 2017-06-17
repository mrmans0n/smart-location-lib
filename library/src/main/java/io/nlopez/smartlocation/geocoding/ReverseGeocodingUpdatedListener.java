package io.nlopez.smartlocation.geocoding;

import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;

/**
 * Basic listener for reverse geocoding updates.
 */
public interface ReverseGeocodingUpdatedListener extends OnReverseGeocodingListener, OnAllProvidersFailed {
    /**
     * Convenience abstract listener
     */
    abstract class SimpleReverseGeocodingUpdatedListener implements ReverseGeocodingUpdatedListener {
        @Override
        public void onAllProvidersFailed() {
            // Override if you want to do something if all providers failed
        }
    }
}