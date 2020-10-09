package io.nlopez.smartlocation.geocoding.providers.android;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.geocoding.GeocodingProvider;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Geocoding provider based on Android's Geocoder class.
 */
public class AndroidGeocodingProvider implements GeocodingProvider {

    @NonNull
    private final Provider.StatusListener mStatusListener;
    @NonNull
    private final Locale mLocale;
    @NonNull
    private final Context mContext;
    @NonNull
    private final Logger mLogger;
    private boolean mIsReleased;

    public AndroidGeocodingProvider(
            @NonNull Context context,
            @NonNull Provider.StatusListener statusListener,
            @NonNull Logger logger,
            @NonNull Locale locale) {
        mContext = context;
        mStatusListener = statusListener;
        mLogger = logger;
        mLocale = locale;
    }

    @Override
    public void findLocationByName(@NonNull String name, @NonNull final OnGeocodingListener listener, int maxResults) {
        if (!isValidEnvironment()) {
            mStatusListener.onProviderFailed(this);
            return;
        }

        final GeocodingTask geocodingTask = new GeocodingTask(
                mContext,
                mLogger,
                mLocale,
                new GeocodingTask.GeocodingTaskListener() {
                    @Override
                    public void onLocationFailed() {
                        if (mIsReleased) {
                            return;
                        }
                        mStatusListener.onProviderFailed(AndroidGeocodingProvider.this);
                    }

                    @Override
                    public void onLocationResolved(String name, List<LocationAddress> results) {
                        if (mIsReleased) {
                            return;
                        }
                        listener.onLocationResolved(name, results);
                    }
                },
                maxResults);
        geocodingTask.execute(name);
    }

    @Override
    public void findNameByLocation(@NonNull Location location, @NonNull final OnReverseGeocodingListener listener, int maxResults) {
        if (!isValidEnvironment()) {
            mStatusListener.onProviderFailed(this);
            return;
        }
        final ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(
                mContext,
                mLogger,
                mLocale,
                new ReverseGeocodingTask.ReverseGeocodingTaskListener() {
                    @Override
                    public void onAddressFailed() {
                        if (mIsReleased) {
                            return;
                        }
                        mStatusListener.onProviderFailed(AndroidGeocodingProvider.this);
                    }

                    @Override
                    public void onAddressResolved(Location original, List<LocationAddress> results) {
                        if (mIsReleased) {
                            return;
                        }
                        listener.onAddressResolved(original, results);
                    }
                },
                maxResults);
        reverseGeocodingTask.execute(location);
    }

    private boolean isValidEnvironment() {
        boolean isValid = true;
        if (mLocale == null) {
            mLogger.e("Locale is null for some reason");
            isValid = false;
        }
        if (!Geocoder.isPresent()) {
            mLogger.e("Android Geocoder is not present");
            isValid = false;
        }
        return isValid;
    }

    @Override
    public void release() {
        mIsReleased = true;
    }
}
