package io.nlopez.smartlocation.geocoding.providers.android;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Pair;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Handles the geocoding operations, aka getting a location from an address.
 */
class GeocodingTask extends AsyncTask<String, Void, Pair<String, List<LocationAddress>>> {

    @NonNull private final Logger mLogger;
    @NonNull private final Locale mLocale;
    @NonNull private final WeakReference<Context> mContextRef;
    @NonNull
    private final GeocodingTaskListener mListener;
    private final int mMaxResults;

    GeocodingTask(
            @NonNull Context context,
            @NonNull Logger logger,
            @NonNull Locale locale,
            @NonNull GeocodingTaskListener listener,
            int maxResults) {
        mContextRef = new WeakReference<>(context);
        mLogger = logger;
        mLocale = locale;
        mListener = listener;
        mMaxResults = maxResults;
    }

    @Override
    protected Pair<String, List<LocationAddress>> doInBackground(String... strings) {
        if (strings.length == 0 || mContextRef.get() == null) {
            mLogger.e("Name was not provided");
            return null;
        }
        final Geocoder geocoder = new Geocoder(mContextRef.get(), mLocale);
        final String name = strings[0];
        final List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(name, mMaxResults);
        } catch (IOException e) {
            mLogger.e(e, "Geocoder crashed");
            return null;
        }
        final List<LocationAddress> result = new ArrayList<>();
        for (Address address : addresses) {
            result.add(new LocationAddress(address));
        }
        return new Pair<>(name, result);
    }

    @Override
    protected void onPostExecute(Pair<String, List<LocationAddress>> results) {
        if (results == null) {
            mListener.onLocationFailed();
            return;
        }
        mListener.onLocationResolved(results.first, results.second);
    }

    interface GeocodingTaskListener extends OnGeocodingListener {
        void onLocationFailed();
    }
}
