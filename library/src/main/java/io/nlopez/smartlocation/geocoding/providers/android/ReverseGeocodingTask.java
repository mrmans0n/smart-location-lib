package io.nlopez.smartlocation.geocoding.providers.android;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.util.Pair;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Handles the reverse geocoding operations, aka getting an address from a location.
 */
class ReverseGeocodingTask extends AsyncTask<Location, Void, Pair<Location, List<LocationAddress>>> {

    @NonNull private final Logger mLogger;
    @NonNull private final Locale mLocale;
    @NonNull private final WeakReference<Context> mContextRef;
    @NonNull private final ReverseGeocodingTaskListener mListener;
    private final int mMaxResults;

    ReverseGeocodingTask(
            @NonNull Context context,
            @NonNull Logger logger,
            @NonNull Locale locale,
            @NonNull ReverseGeocodingTaskListener listener,
            int maxResults) {
        mContextRef = new WeakReference<>(context);
        mLogger = logger;
        mLocale = locale;
        mListener = listener;
        mMaxResults = maxResults;
    }

    @Override
    protected Pair<Location, List<LocationAddress>> doInBackground(Location... locations) {
        if (locations.length == 0 || mContextRef.get() == null) {
            mLogger.e("Location was not provided");
            return null;
        }
        final Geocoder geocoder = new Geocoder(mContextRef.get(), mLocale);
        final Location location = locations[0];
        final List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), mMaxResults);
        } catch (IOException e) {
            mLogger.e(e, "Geocoder crashed");
            return null;
        }
        final List<LocationAddress> results = new ArrayList<>();
        for (Address address : addresses) {
            results.add(new LocationAddress(address));
        }
        return new Pair<>(location, results);
    }

    @Override
    protected void onPostExecute(Pair<Location, List<LocationAddress>> results) {
        if (results == null) {
            mListener.onAddressFailed();
            return;
        }
        mListener.onAddressResolved(results.first, results.second);
    }

    interface ReverseGeocodingTaskListener extends OnReverseGeocodingListener {
        void onAddressFailed();
    }
}
