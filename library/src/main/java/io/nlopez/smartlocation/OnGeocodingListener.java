package io.nlopez.smartlocation;

import java.util.List;

import io.nlopez.smartlocation.geocoding.utils.LocationAddress;

public interface OnGeocodingListener {
    void onLocationResolved(String name, List<LocationAddress> results);
}