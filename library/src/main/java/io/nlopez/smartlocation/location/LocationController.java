package io.nlopez.smartlocation.location;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.LinkedList;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.common.OnAllProvidersFailed;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.location.config.LocationProviderParams;
import io.nlopez.smartlocation.utils.Logger;

public class LocationController implements Provider.StatusListener {
    public static final int NO_TIMEOUT = -1;

    @NonNull private final LinkedList<LocationProviderFactory> mProviderList;
    @NonNull private final Logger mLogger;
    @NonNull private final Context mContext;
    @NonNull private final OnLocationUpdatedListener mUpdateListener;
    @NonNull private final LocationProviderParams mParams;
    @NonNull private final OnAllProvidersFailed mListener;
    @NonNull private final Handler mHandler;
    @Nullable private LocationProvider mCurrentProvider;
    private final long mTimeout;

    @VisibleForTesting
    LocationController(
            @NonNull Context context,
            @NonNull OnLocationUpdatedListener updateListener,
            @NonNull OnAllProvidersFailed listener,
            @NonNull LocationProviderParams params,
            long timeout,
            @NonNull List<LocationProviderFactory> providerList,
            @NonNull Logger logger,
            @NonNull Handler handler) {
        mContext = context;
        mUpdateListener = updateListener;
        mParams = params;
        mListener = listener;
        mTimeout = timeout;
        mLogger = logger;
        mHandler = handler;
        mProviderList = new LinkedList<>(providerList);
    }

    @Nullable
    public LocationProvider getCurrentProvider() {
        return mCurrentProvider;
    }

    @NonNull
    public LocationController start() {
        startNext();
        return this;
    }

    private void startNext() {
        final LocationProviderFactory providerFactory = mProviderList.poll();
        if (providerFactory == null) {
            mLogger.w("All providers failed");
            mListener.onAllProvidersFailed();
            return;
        }
        mCurrentProvider = providerFactory.create(mContext, this);
        final TimeoutableLocationUpdateListener updateListener =
                new TimeoutableLocationUpdateListener(
                        mCurrentProvider,
                        mUpdateListener,
                        new ProviderTimeoutListener() {
                            @Override
                            public void onProviderTimeout(@NonNull Provider provider) {
                                mLogger.d(provider + " timed out.");
                                LocationController.this.onProviderFailed(provider);
                            }
                        },
                        mTimeout,
                        mHandler);
        mCurrentProvider.start(updateListener, mParams);
        updateListener.onProviderStarted();
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

    public interface ProviderTimeoutListener {
        void onProviderTimeout(@NonNull Provider provider);
    }

    /**
     * Handles the dispatch of location updates, and allows extra features on top like timeouts.
     */
    static class TimeoutableLocationUpdateListener implements OnLocationUpdatedListener, Runnable {

        private final OnLocationUpdatedListener mListener;
        private final Handler mHandler;
        private final long mTimeout;
        private final ProviderTimeoutListener mTimeoutListener;
        private final LocationProvider mProvider;
        private boolean locationReceived = false;
        private boolean cancelled = false;

        TimeoutableLocationUpdateListener(
                @NonNull LocationProvider provider,
                @NonNull OnLocationUpdatedListener listener,
                @NonNull ProviderTimeoutListener timeoutListener,
                long timeout,
                @NonNull Handler handler) {
            mProvider = provider;
            mListener = listener;
            mTimeoutListener = timeoutListener;
            mTimeout = timeout;
            mHandler = handler;
        }

        public void onProviderStarted() {
            mHandler.postDelayed(this, mTimeout);
        }

        @Override
        public void onLocationUpdated(Location location) {
            if (!cancelled) {
                locationReceived = true;
                mListener.onLocationUpdated(location);
            }
        }

        @Override
        public void run() {
            if (!locationReceived) {
                mTimeoutListener.onProviderTimeout(mProvider);
                cancelled = true;
            }
        }
    }

    /**
     * Used to create instances of {@link LocationController}
     */
    public static class Factory {
        @NonNull
        public LocationController create(
                @NonNull Context context,
                @NonNull OnLocationUpdatedListener updateListener,
                @NonNull OnAllProvidersFailed listener,
                @NonNull LocationProviderParams params,
                long timeout,
                @NonNull List<LocationProviderFactory> providerList,
                @NonNull Logger logger) {
            return new LocationController(
                    context,
                    updateListener,
                    listener,
                    params,
                    timeout,
                    providerList,
                    logger,
                    new Handler(Looper.getMainLooper()));
        }
    }
}
