package io.nlopez.smartlocation.location.providers.legacy;

import android.content.Context;
import android.support.annotation.NonNull;

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
    private final Context mContext;

    public LocationManagerProviderFactory(@NonNull Context context) {
        mContext = context;
        mLogger = LoggerFactory.get();
        mPermissionsManager = LocationPermissionsManager.get();
    }

    @Override
    public LocationProvider create(Provider.StatusListener statusListener) {
        return new LocationManagerProvider(
                statusListener,
                new LocationStore(mContext),
                mLogger,
                mPermissionsManager);
    }
}
