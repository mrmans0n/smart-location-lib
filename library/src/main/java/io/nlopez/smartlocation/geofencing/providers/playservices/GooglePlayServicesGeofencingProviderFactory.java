package io.nlopez.smartlocation.geofencing.providers.playservices;

import android.content.Context;
import androidx.annotation.NonNull;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.geofencing.GeofencingProvider;
import io.nlopez.smartlocation.geofencing.GeofencingProviderFactory;
import io.nlopez.smartlocation.location.LocationPermissionsManager;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

public class GooglePlayServicesGeofencingProviderFactory implements GeofencingProviderFactory {

    private final Logger mLogger;
    private final LocationPermissionsManager mPermissionsManager;

    public GooglePlayServicesGeofencingProviderFactory() {
        mLogger = LoggerFactory.get();
        mPermissionsManager = LocationPermissionsManager.get();
    }

    @NonNull
    @Override
    public GeofencingProvider create(@NonNull Context context, @NonNull Provider.StatusListener statusListener) {
        return new GooglePlayServicesGeofencingProvider(
                context,
                statusListener,
                mLogger,
                mPermissionsManager);
    }
}
