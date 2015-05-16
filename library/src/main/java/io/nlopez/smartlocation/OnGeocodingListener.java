package io.nlopez.smartlocation;

import java.util.List;

import io.nlopez.smartlocation.geocoding.utils.LocationAddress;

/**
 * Created by mrm on 4/1/15.
 */
public interface OnGeocodingListener {
    void onLocationResolved(String name, List<LocationAddress> results);
}