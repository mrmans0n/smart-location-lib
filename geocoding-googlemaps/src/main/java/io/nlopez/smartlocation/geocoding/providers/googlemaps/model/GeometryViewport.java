package io.nlopez.smartlocation.geocoding.providers.googlemaps.model;

public class GeometryViewport {
    private GeometryLocation northeast;
    private GeometryLocation southwest;

    public GeometryLocation getNortheast() {
        return northeast;
    }

    public GeometryLocation getSouthwest() {
        return southwest;
    }
}
