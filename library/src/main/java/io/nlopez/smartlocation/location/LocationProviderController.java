package io.nlopez.smartlocation.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.utils.Logger;

public class LocationProviderController implements Provider.StatusListener {
    @NonNull
    private final LinkedList<LocationProviderFactory> mProviderList;
    @NonNull
    private final Logger mLogger;
    @NonNull
    private final Context mContext;
    @Nullable
    private LocationProvider mCurrentProvider;
    @Nullable
    private Listener mListener;

    public LocationProviderController(
            @NonNull Context context,
            @NonNull List<LocationProviderFactory> providerList,
            @NonNull Logger logger) {
        mContext = context;
        mLogger = logger;
        mProviderList = new LinkedList<>(providerList);
    }

    @Nullable
    public LocationProvider getCurrentProvider() {
        return mCurrentProvider;
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
        mCurrentProvider = providerFactory.create(this);
        // mCurrentProvider.start();
        // TODO start blah blah
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
