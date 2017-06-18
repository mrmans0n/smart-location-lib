package io.nlopez.smartlocation.geocoding.providers.googlemaps.model;

import java.util.List;

public class AddressComponent {
    private String longName;
    private String shortName;
    private List<String> types;

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public List<String> getTypes() {
        return types;
    }
}
