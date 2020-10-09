package io.nlopez.smartlocation.geocoding.providers.googlemaps;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Locale;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.geocoding.GeocodingProvider;
import io.nlopez.smartlocation.geocoding.GeocodingProviderFactory;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.api.GoogleMapsGeocodingService;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.util.LocationAddressFactory;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

public class GoogleMapsApiGeocodingProviderFactory implements GeocodingProviderFactory {

    private final Logger mLogger;
    private final Locale mLocale;
    private final String mApiKey;
    private final GoogleMapsGeocodingService mService;
    private final LocationAddressFactory mLocationAddressFactory;

    public GoogleMapsApiGeocodingProviderFactory(@NonNull String apiKey) {
        this(apiKey, GoogleMapsGeocodingService.get(), LoggerFactory.get(), Locale.getDefault());
    }

    public GoogleMapsApiGeocodingProviderFactory(
            @NonNull String apiKey,
            @NonNull GoogleMapsGeocodingService service,
            @NonNull Logger logger,
            @NonNull Locale locale) {
        mApiKey = apiKey;
        mService = service;
        mLogger = logger;
        mLocale = locale;
        mLocationAddressFactory = new LocationAddressFactory(mLocale);
    }

    @NonNull
    @Override
    public GeocodingProvider create(@NonNull Context context, @NonNull Provider.StatusListener statusListener) {
        return new GoogleMapsApiGeocodingProvider(
                statusListener,
                mService,
                mApiKey,
                mLocationAddressFactory,
                mLogger,
                mLocale);
    }
}
