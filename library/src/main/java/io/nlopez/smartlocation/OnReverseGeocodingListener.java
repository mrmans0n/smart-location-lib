package io.nlopez.smartlocation;

import android.location.Location;

import java.util.List;

import io.nlopez.smartlocation.geocoding.common.LocationAddress;

public interface OnReverseGeocodingListener {
    void onAddressResolved(Location original, List<LocationAddress> results);
}