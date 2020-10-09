package io.nlopez.smartlocation.geocoding.providers.googlemaps.util;

import android.location.Address;
import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Locale;

import io.nlopez.smartlocation.common.Factory1;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.Geometry;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.GeometryLocation;
import io.nlopez.smartlocation.geocoding.providers.googlemaps.model.ResultEntry;

/**
 * Creates {@link io.nlopez.smartlocation.geocoding.common.LocationAddress} from google maps results.
 */
public class LocationAddressFactory implements Factory1<LocationAddress, ResultEntry> {

    @NonNull
    private final Locale mLocale;

    public LocationAddressFactory(@NonNull Locale locale) {
        mLocale = locale;
    }

    @NonNull
    @Override
    public LocationAddress create(@NonNull ResultEntry resultEntry) {
        final Address address = new Address(mLocale);

        // Address fields
        address.setAddressLine(0, resultEntry.getFormattedAddress());
        // TODO add more fine grained address retrieval

        // Location
        final Geometry geometry = resultEntry.getGeometry();
        if (geometry != null) {
            final GeometryLocation geometryLocation = geometry.getLocation();
            if (geometryLocation != null) {
                final Location location = geometryLocation.getLocation();
                address.setLatitude(location.getLatitude());
                address.setLongitude(location.getLongitude());
            }
        }
        return new LocationAddress(address);
    }
}
