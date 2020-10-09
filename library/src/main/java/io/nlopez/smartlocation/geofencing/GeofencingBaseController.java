package io.nlopez.smartlocation.geofencing;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.utils.Logger;

public abstract class GeofencingBaseController implements Provider.StatusListener {
    @NonNull private final LinkedList<GeofencingProviderFactory> mProviderList;
    @NonNull private final Logger mLogger;
    @NonNull private final Context mContext;
    @NonNull private final OnAllProvidersFailed mListener;
    @Nullable protected GeofencingProvider mCurrentProvider;

    public GeofencingBaseController(
            @NonNull Context context,
            @NonNull OnAllProvidersFailed listener,
            @NonNull List<GeofencingProviderFactory> providerList,
            @NonNull Logger logger) {
        mContext = context;
        mListener = listener;
        mLogger = logger;
        mProviderList = new LinkedList<>(providerList);
    }

    @Nullable
    public GeofencingProvider getCurrentProvider() {
        return mCurrentProvider;
    }

    protected void startNext() {
        final GeofencingProviderFactory providerFactory = mProviderList.poll();
        if (providerFactory == null) {
            mLogger.w("All providers failed");
            mListener.onAllProvidersFailed();
            return;
        }
        mCurrentProvider = providerFactory.create(mContext, this);
        geofencingAction();
    }

    protected abstract void geofencingAction();

    public void release() {
        if (mCurrentProvider != null) {
            mCurrentProvider.release();
        }
    }

    @Override
    public void onProviderFailed(@NonNull Provider provider) {
        if (mCurrentProvider != provider) {
            return;
        }
        mLogger.d(provider + " failed.");
        provider.release();
        startNext();
    }

    public static class Factory {
        @NonNull
        public GeofencingAddController createAddController(
                @NonNull Context context,
                @NonNull OnAllProvidersFailed listener,
                @NonNull List<GeofencingProviderFactory> providerList,
                @NonNull Logger logger) {
            return new GeofencingAddController(context, listener, providerList, logger);
        }

        @NonNull
        public GeofencingRemoveController createRemoveController(
                @NonNull Context context,
                @NonNull OnAllProvidersFailed listener,
                @NonNull List<GeofencingProviderFactory> providerList,
                @NonNull Logger logger) {
            return new GeofencingRemoveController(context, listener, providerList, logger);
        }
    }
}
