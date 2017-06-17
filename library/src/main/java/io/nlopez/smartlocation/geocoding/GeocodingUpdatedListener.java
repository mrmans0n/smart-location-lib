package io.nlopez.smartlocation.geocoding;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;

/**
 * Basic listener for geocoding updates.
 */
public interface GeocodingUpdatedListener extends OnGeocodingListener, OnAllProvidersFailed {

    /**
     * Convenience abstract listener
     */
    abstract class SimpleGeocodingUpdatedListener implements GeocodingUpdatedListener {
        @Override
        public void onAllProvidersFailed() {
            // Override if you want to do something if all providers failed
        }
    }
}
