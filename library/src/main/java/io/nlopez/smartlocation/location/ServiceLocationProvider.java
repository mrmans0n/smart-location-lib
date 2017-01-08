package io.nlopez.smartlocation.location;

import io.nlopez.smartlocation.utils.ServiceConnectionListener;

/**
 * An extension of the {@link LocationProvider} interface for location providers that utilize 3rd
 * party services. Implementations must invoke the appropriate {@link ServiceConnectionListener}
 * events when the connection to the 3rd party service succeeds, fails, or is suspended.
 *
 * @author abkaplan07
 */
public interface ServiceLocationProvider extends LocationProvider {

    /**
     * Gets the {@link ServiceConnectionListener} callback for this location provider.
     */
    ServiceConnectionListener getServiceListener();

    /**
     * Set the {@link ServiceConnectionListener} used for callbacks from the 3rd party service.
     *
     * @param listener a <code>ServiceConnectionListener</code> to respond to connection events from
     *                 the underlying 3rd party location service.
     */
    void setServiceListener(ServiceConnectionListener listener);
}
