package io.nlopez.smartlocation.location.providers.legacy;

import android.content.Context;
import androidx.annotation.NonNull;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.location.LocationPermissionsManager;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.LocationProviderFactory;
import io.nlopez.smartlocation.location.LocationStore;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

public class LocationManagerProviderFactory implements LocationProviderFactory {

    private final Logger mLogger;
    private final LocationPermissionsManager mPermissionsManager;

    public LocationManagerProviderFactory() {
        this(LoggerFactory.get(), LocationPermissionsManager.get());
    }

    public LocationManagerProviderFactory(@NonNull Logger logger, @NonNull LocationPermissionsManager permissionsManager) {
        mLogger = logger;
        mPermissionsManager = permissionsManager;
    }

    @Override
    public LocationProvider create(Context context, Provider.StatusListener statusListener) {
        return new LocationManagerProvider(
                context,
                statusListener,
                new LocationStore(context),
                mLogger,
                mPermissionsManager);
    }
}
