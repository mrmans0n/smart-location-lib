package io.nlopez.smartlocation.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.GeofencingRequest;

import java.util.List;

import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.utils.Logger;

import static io.nlopez.smartlocation.utils.Nulls.notNull;

public class GeofencingAddController extends GeofencingBaseController {
    @Nullable private GeofencingRequest mRequest;
    @Nullable private PendingIntent mPendingIntent;

    GeofencingAddController(
            @NonNull Context context,
            @NonNull OnAllProvidersFailed listener,
            @NonNull List<GeofencingProviderFactory> providerList,
            @NonNull Logger logger) {
        super(context, listener, providerList, logger);
    }

    @NonNull
    public GeofencingAddController addGeofences(@NonNull GeofencingRequest request, @NonNull PendingIntent pendingIntent) {
        mRequest = request;
        mPendingIntent = pendingIntent;
        startNext();
        return this;
    }

    @Override
    protected void geofencingAction() {
        notNull(mCurrentProvider).addGeofences(notNull(mRequest), notNull(mPendingIntent));
    }

    @Override
    public void release() {
        super.release();
        mRequest = null;
        mPendingIntent = null;
    }
}
