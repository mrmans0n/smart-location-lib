package io.nlopez.smartlocation.location;

import android.location.Location;
import androidx.annotation.NonNull;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.location.config.LocationProviderParams;

/**
 * Describes all the functionality needed for a location provider
 */
public interface LocationProvider extends Provider {
    void start(@NonNull OnLocationUpdatedListener listener, @NonNull LocationProviderParams params);

    void stop();

    Location getLastLocation();
}
