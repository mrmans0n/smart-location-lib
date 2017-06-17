package io.nlopez.smartlocation.location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;

/**
 * Basic listener for location updates.
 */
public interface LocationUpdatedListener extends OnLocationUpdatedListener, OnAllProvidersFailed {

    abstract class SimpleLocationUpdatedListener implements io.nlopez.smartlocation.location.LocationUpdatedListener {
        @Override
        public void onAllProvidersFailed() {
            // implement if you want to get notified of this event
        }
    }
}
