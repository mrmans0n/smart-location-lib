package io.nlopez.smartlocation.location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;

/**
 * Basic listener for location updates.
 */
public abstract class LocationListener implements OnLocationUpdatedListener, LocationController.Listener {

    @Override
    public void onAllProvidersFailed() {
        // Override if you want to do something if all providers failed
    }
}
