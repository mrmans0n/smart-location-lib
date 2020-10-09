package io.nlopez.smartlocation.geofencing;

import android.app.PendingIntent;
import androidx.annotation.NonNull;

import com.google.android.gms.location.GeofencingRequest;

import java.util.List;

import io.nlopez.smartlocation.common.Provider;

/**
 * Describes all the functionality needed for a geofencing provider.
 * As of now there is only the play service geofencing provider so this
 * api will mimic it for simplicity.
 */
public interface GeofencingProvider extends Provider {
    void addGeofences(@NonNull GeofencingRequest request, @NonNull PendingIntent pendingIntent);

    void removeGeofences(@NonNull List<String> geofenceIds);

    void removeGeofences(@NonNull PendingIntent pendingIntent);
}
