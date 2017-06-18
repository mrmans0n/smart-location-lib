package io.nlopez.smartlocation.geocoding.providers.googlemaps.model;

import java.util.List;

public class ResultEntry {
    private List<AddressComponent> addressComponents;
    private String formattedAddress;
    private Geometry geometry;
    private String placeId;
    private List<String> types;

    public List<AddressComponent> getAddressComponents() {
        return addressComponents;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getPlaceId() {
        return placeId;
    }

    public List<String> getTypes() {
        return types;
    }
}
