package io.nlopez.smartlocation.geocoding;

import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.utils.Logger;

public class ReverseGeocodingController implements Provider.StatusListener {

    @NonNull private final LinkedList<GeocodingProviderFactory> mProviderList;
    @NonNull private final Logger mLogger;
    @NonNull private final Context mContext;
    @NonNull private final OnReverseGeocodingListener mUpdateListener;
    @NonNull private final OnAllProvidersFailed mListener;
    @NonNull private final Location mLocation;
    private final int mMaxResults;
    @Nullable private GeocodingProvider mCurrentProvider;

    @VisibleForTesting
    ReverseGeocodingController(
            @NonNull Context context,
            @NonNull Location location,
            int maxResults,
            @NonNull OnReverseGeocodingListener updateListener,
            @NonNull OnAllProvidersFailed listener,
            @NonNull List<GeocodingProviderFactory> providerList,
            @NonNull Logger logger) {
        mContext = context;
        mLocation = location;
        mMaxResults = maxResults;
        mLogger = logger;
        mUpdateListener = updateListener;
        mListener = listener;
        mProviderList = new LinkedList<>(providerList);
    }

    @Nullable
    public GeocodingProvider getCurrentProvider() {
        return mCurrentProvider;
    }

    @NonNull
    public ReverseGeocodingController start() {
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
        mCurrentProvider.findNameByLocation(
                mLocation,
                mUpdateListener,
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
     * Use to instantiate {@link ReverseGeocodingController}
     */
    public static class Factory {
        @NonNull
        public ReverseGeocodingController create(
                @NonNull Context context,
                @NonNull Location location,
                int maxResults,
                @NonNull OnReverseGeocodingListener updateListener,
                @NonNull OnAllProvidersFailed listener,
                @NonNull List<GeocodingProviderFactory> providerList,
                @NonNull Logger logger) {
            return new ReverseGeocodingController(context, location, maxResults, updateListener, listener, providerList, logger);
        }
    }
}
