package io.nlopez.smartlocation.geocoding.providers.googlemaps.api;

import androidx.annotation.NonNull;

import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.GeocodeResult;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Defines Google Maps Geocoding REST API service.
 * <p>
 * Even if it's not a complete definition of the API, it has the necessary info for our geocoding needs.
 */
public class GoogleMapsGeocodingService {
    private static GoogleMapsGeocodingService sInstance;
    private static final String API_URL = "https://maps.googleapis.com/maps/api/";

    private final GoogleMapsGeocoding mService;

    private GoogleMapsGeocodingService() {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(GoogleMapsGeocoding.class);
    }

    @NonNull
    public static GoogleMapsGeocodingService get() {
        if (sInstance == null) {
            sInstance = new GoogleMapsGeocodingService();
        }
        return sInstance;
    }

    @NonNull
    public GoogleMapsGeocoding geocoding() {
        return mService;
    }

    public interface GoogleMapsGeocoding {
        @GET("geocode/json")
        Call<GeocodeResult> findLocationByName(@Query("address") String address, @Query("language") String language, @Query("") String apiKey);

        @GET("geocode/json")
        Call<GeocodeResult> findNameByLocation(@Query("latlng") String address, @Query("language") String language, @Query("") String apiKey);
    }

}