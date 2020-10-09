package io.nlopez.smartlocation.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.utils.Logger;

import static io.nlopez.smartlocation.utils.Nulls.notNull;

public class GeofencingRemoveController extends GeofencingBaseController {
    @Nullable private PendingIntent mPendingIntent;
    @Nullable private List<String> mGeofenceIds;

    GeofencingRemoveController(
            @NonNull Context context,
            @NonNull OnAllProvidersFailed listener,
            @NonNull List<GeofencingProviderFactory> providerList,
            @NonNull Logger logger) {
        super(context, listener, providerList, logger);
    }

    @NonNull
    public GeofencingRemoveController removeGeofence(@NonNull List<String> geofenceIds) {
        mGeofenceIds = geofenceIds;
        startNext();
        return this;
    }

    public GeofencingRemoveController removeGeofence(@NonNull PendingIntent pendingIntent) {
        mPendingIntent = pendingIntent;
        startNext();
        return this;
    }

    @Override
    protected void geofencingAction() {
        if (mPendingIntent != null) {
            notNull(mCurrentProvider).removeGeofences(mPendingIntent);
        } else if (mGeofenceIds != null) {
            notNull(mCurrentProvider).removeGeofences(mGeofenceIds);
        } else {
            throw new RuntimeException("No pending intent or geofence ids provided for removing");
        }
    }

    public void release() {
        super.release();
        mGeofenceIds = null;
        mPendingIntent = null;
    }
}
