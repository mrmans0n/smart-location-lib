package io.nlopez.smartlocation.location.providers.playservices;

import android.content.Context;
import androidx.annotation.NonNull;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.location.LocationPermissionsManager;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.LocationProviderFactory;
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

public class GooglePlayServicesLocationProviderFactory implements LocationProviderFactory {

    private final Logger mLogger;
    private final LocationPermissionsManager mPermissionsManager;

    public GooglePlayServicesLocationProviderFactory() {
        mLogger = LoggerFactory.get();
        mPermissionsManager = LocationPermissionsManager.get();
    }

    @NonNull
    @Override
    public LocationProvider create(@NonNull Context context, @NonNull Provider.StatusListener statusListener) {
        return new GooglePlayServicesLocationProvider(
                context,
                statusListener,
                new LocationStore(context),
                mLogger,
                mPermissionsManager);
    }
}
