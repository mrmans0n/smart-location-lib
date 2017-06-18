package io.nlopez.smartlocation.geocoding.providers.googlemaps.model;

public class Geometry {
    private GeometryLocation location;
    private String locationType;
    private GeometryViewport viewport;

    public GeometryLocation getLocation() {
        return location;
    }

    public String getLocationType() {
        return locationType;
    }

    public GeometryViewport getViewport() {
        return viewport;
    }
}
