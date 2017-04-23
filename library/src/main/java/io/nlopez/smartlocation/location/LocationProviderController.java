package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Logger;

public class LocationProviderController implements Provider.StatusListener {
    @NonNull
    private final LinkedList<LocationProviderFactory> mProviderList;
    @NonNull
    private final Logger mLogger;
    @NonNull
    private final Context mContext;
    @NonNull
    private final OnLocationUpdatedListener mUpdateListener;
    @NonNull
    private final LocationProviderParams mParams;
    @Nullable
    private LocationProvider mCurrentProvider;
    @Nullable
    private Listener mListener;

    public LocationProviderController(
            @NonNull Context context,
            @NonNull OnLocationUpdatedListener updateListener,
            @NonNull LocationProviderParams params,
            @NonNull List<LocationProviderFactory> providerList,
            @NonNull Logger logger) {
        mContext = context;
        mUpdateListener = updateListener;
        mParams = params;
        mLogger = logger;
        mProviderList = new LinkedList<>(providerList);
    }

    @Nullable
    public LocationProvider getCurrentProvider() {
        return mCurrentProvider;
    }

    @NonNull
    public LocationProviderController start() {
        startNext();
        return this;
    }

    private void startNext() {
        LocationProviderFactory providerFactory = mProviderList.poll();
        if (providerFactory == null) {
            mLogger.w("All providers failed");
            if (mListener != null) {
                mListener.onAllProvidersFailed();
            }
            return;
        }
        mCurrentProvider = providerFactory.create(mContext, this);
        mCurrentProvider.start(mUpdateListener, mParams);
    }

    @Nullable
    public Location getLastLocation() {
        if (mCurrentProvider == null) {
            return null;
        }
        return mCurrentProvider.getLastLocation();
    }

    public void stop() {
        if (mCurrentProvider == null) {
            return;
        }
        mCurrentProvider.stop();
    }

    @Override
    public void onProviderFailed(@NonNull Provider provider) {
        if (mCurrentProvider != provider) {
            return;
        }
        provider.release();
        startNext();
    }

    public void setListener(@Nullable Listener listener) {
        mListener = listener;
    }

    interface Listener {
        /**
         * All providers have failed to initialize
         */
        void onAllProvidersFailed();
    }
}
