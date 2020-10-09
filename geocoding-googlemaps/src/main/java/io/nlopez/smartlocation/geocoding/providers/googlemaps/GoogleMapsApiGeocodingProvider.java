package io.nlopez.smartlocation.geocoding.providers.googlemaps;

import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.geocoding.GeocodingProvider;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.api.GoogleMapsGeocodingService;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.GeocodeResult;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.GeocodeResult.StatusType;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.ResultEntry;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.util.LocationAddressFactory;
import io.nlopez.smartlocation.utils.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Geocoding provider based on Android's Geocoder class.
 */
public class GoogleMapsApiGeocodingProvider implements GeocodingProvider {

    @NonNull private final StatusListener mStatusListener;
    @NonNull private final Locale mLocale;
    @NonNull private final Logger mLogger;
    @NonNull private final String mApiKey;
    @NonNull private final GoogleMapsGeocodingService mService;
    @NonNull private final LocationAddressFactory mLocationAddressFactory;

    public GoogleMapsApiGeocodingProvider(
            @NonNull StatusListener statusListener,
            @NonNull GoogleMapsGeocodingService service,
            @NonNull String apiKey,
            @NonNull LocationAddressFactory locationAddressFactory,
            @NonNull Logger logger,
            @NonNull Locale locale) {
        mStatusListener = statusListener;
        mService = service;
        mApiKey = apiKey;
        mLocationAddressFactory = locationAddressFactory;
        mLogger = logger;
        mLocale = locale;
    }

    @Override
    public void findLocationByName(@NonNull final String name, @NonNull final OnGeocodingListener listener, final int maxResults) {
        if (!isValidEnvironment()) {
            mStatusListener.onProviderFailed(this);
            return;
        }
        final Call<GeocodeResult> call =
                mService.geocoding().findLocationByName(name, mLocale.getLanguage(), mApiKey);
        call.enqueue(new Callback<GeocodeResult>() {
            @Override
            public void onResponse(Call<GeocodeResult> call, Response<GeocodeResult> response) {
                if (!isValidGeocodeResult(response.body())) {
                    mStatusListener.onProviderFailed(GoogleMapsApiGeocodingProvider.this);
                    return;
                }
                listener.onLocationResolved(
                        name,
                        locationAddressesFromGeocodeResult(response.body(), maxResults));
            }

            @Override
            public void onFailure(Call<GeocodeResult> call, Throwable t) {
                mLogger.e("Error in GoogleMapsApiGeocodingProvider#findLocationByName", t);
                mStatusListener.onProviderFailed(GoogleMapsApiGeocodingProvider.this);
            }
        });
    }

    @Override
    public void findNameByLocation(@NonNull final Location location, @NonNull final OnReverseGeocodingListener listener, final int maxResults) {
        if (!isValidEnvironment()) {
            mStatusListener.onProviderFailed(this);
            return;
        }

        final String locationText = String.format(Locale.US, "%.6f,%.6f", location.getLatitude(), location.getLongitude());

        final Call<GeocodeResult> call =
                mService.geocoding().findNameByLocation(locationText, mLocale.getLanguage(), mApiKey);
        call.enqueue(new Callback<GeocodeResult>() {
            @Override
            public void onResponse(Call<GeocodeResult> call, Response<GeocodeResult> response) {
                if (!isValidGeocodeResult(response.body())) {
                    mStatusListener.onProviderFailed(GoogleMapsApiGeocodingProvider.this);
                    return;
                }
                listener.onAddressResolved(
                        location,
                        locationAddressesFromGeocodeResult(response.body(), maxResults));
            }

            @Override
            public void onFailure(Call<GeocodeResult> call, Throwable t) {
                mLogger.e("Error in GoogleMapsApiGeocodingProvider#findNameByLocation", t);
                mStatusListener.onProviderFailed(GoogleMapsApiGeocodingProvider.this);
            }
        });
    }

    @NonNull
    private List<LocationAddress> locationAddressesFromGeocodeResult(@NonNull GeocodeResult geocodeResult, int maxResults) {
        final List<LocationAddress> resultList = new ArrayList<>();
        final List<ResultEntry> results = geocodeResult.getResults();
        for (int i = 0; i < Math.min(results.size(), maxResults); i++) {
            final ResultEntry resultEntry = results.get(i);
            resultList.add(mLocationAddressFactory.create(resultEntry));
        }
        return resultList;
    }

    private boolean isValidGeocodeResult(@Nullable GeocodeResult result) {
        if (result == null) {
            return false;
        }
        @StatusType String status = result.getStatus();
        switch (status) {
            case StatusType.ZERO_RESULTS:
            case StatusType.OK:
                return true;
            default:
                mLogger.i("GeocodeResult returned invalid status code: " + status);
                return false;
        }
    }

    private boolean isValidEnvironment() {
        boolean isValid = true;
        if (TextUtils.isEmpty(mApiKey)) {
            mLogger.e("Google Maps API key needed. Go to https://developers.google.com/maps/documentation/geocoding/get-api-key and get yours.");
            isValid = false;
        }
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
        // no-op
    }
}
