package io.nlopez.smartlocation.geocoding;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.utils.Logger;

public class GeocodingController implements Provider.StatusListener {

    @NonNull private final LinkedList<GeocodingProviderFactory> mProviderList;
    @NonNull private final Logger mLogger;
    @NonNull private final Context mContext;
    @NonNull private final OnGeocodingListener mGeocodingUpdatedListener;
    @NonNull private final String mName;
    @NonNull private final OnAllProvidersFailed mListener;
    @Nullable private GeocodingProvider mCurrentProvider;
    private final int mMaxResults;

    @VisibleForTesting
    GeocodingController(
            @NonNull Context context,
            @NonNull String name,
            int maxResults,
            @NonNull OnGeocodingListener geocodingUpdatedListener,
            @NonNull OnAllProvidersFailed listener,
            @NonNull List<GeocodingProviderFactory> providerList,
            @NonNull Logger logger) {
        mContext = context;
        mName = name;
        mMaxResults = maxResults;
        mLogger = logger;
        mGeocodingUpdatedListener = geocodingUpdatedListener;
        mListener = listener;
        mProviderList = new LinkedList<>(providerList);
    }

    @Nullable
    public GeocodingProvider getCurrentProvider() {
        return mCurrentProvider;
    }

    @NonNull
    public GeocodingController start() {
        startNext();
        return this;
    }

    private void startNext() {
        final GeocodingProviderFactory providerFactory = mProviderList.poll();
        if (providerFactory == null) {
            mLogger.w("All providers failed");
            mListener.onAllProvidersFailed();
            return;
        }
        mCurrentProvider = providerFactory.create(mContext, this);
        mCurrentProvider.findLocationByName(
                mName,
                mGeocodingUpdatedListener,
                mMaxResults);
    }

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

    /**
     * Use to instantiate {@link GeocodingController}
     */
    public static class Factory {
        @NonNull
        public GeocodingController create(
                @NonNull Context context,
                @NonNull String name,
                int maxResults,
                @NonNull OnGeocodingListener geocodingUpdatedListener,
                @NonNull OnAllProvidersFailed listener,
                @NonNull List<GeocodingProviderFactory> providerList,
                @NonNull Logger logger) {
            return new GeocodingController(
                    context,
                    name,
                    maxResults,
                    geocodingUpdatedListener,
                    listener,
                    providerList,
                    logger);
        }
    }
}
