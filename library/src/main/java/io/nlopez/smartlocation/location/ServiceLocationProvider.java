package io.nlopez.smartlocation.location;

import io.nlopez.smartlocation.utils.ServiceConnectionListener;

/**
 * Created by findyr-akaplan on 5/12/16.
 */
public interface ServiceLocationProvider extends LocationProvider {

    ServiceConnectionListener getServiceListener();
    void setServiceListener(ServiceConnectionListener listener);
}
