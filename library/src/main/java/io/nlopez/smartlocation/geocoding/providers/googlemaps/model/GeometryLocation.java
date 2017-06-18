package io.nlopez.smartlocation.geocoding.providers.googlemaps.model;

import android.location.Location;

public class GeometryLocation {
    private double lat;
    private double lng;

    public Location getLocation() {
        final Location location = new Location("fromGeometryLocation");
        location.setLatitude(lat);
        location.setLongitude(lng);
        return location;
    }
}
