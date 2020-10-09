package io.nlopez.smartlocation.geocoding;

import android.location.Location;
import androidx.annotation.NonNull;

import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.common.Provider;

/**
 * Describes all the functionality needed for a geocoder
 */
public interface GeocodingProvider extends Provider {
    void findLocationByName(@NonNull String name, @NonNull OnGeocodingListener listener, int maxResults);

    void findNameByLocation(@NonNull Location location, @NonNull OnReverseGeocodingListener listener, int maxResults);
}
