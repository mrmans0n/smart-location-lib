package io.nlopez.smartlocation.location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;

/**
 * Basic listener for location updates.
 */
public abstract class LocationUpdatedListener implements OnLocationUpdatedListener, OnAllProvidersFailed {

    @Override
    public void onAllProvidersFailed() {
        // Override if you want to do something if all providers failed
    }
}
