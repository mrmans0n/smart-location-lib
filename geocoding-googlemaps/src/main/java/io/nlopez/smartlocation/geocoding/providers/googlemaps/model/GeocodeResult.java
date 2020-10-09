package io.nlopez.smartlocation.geocoding.providers.googlemaps.model;

import androidx.annotation.StringDef;

import java.util.List;

public class GeocodeResult {
    private List<ResultEntry> results;
    @StatusType
    private String status;

    public List<ResultEntry> getResults() {
        return results;
    }

    @StatusType
    public String getStatus() {
        return status;
    }

    @StringDef({StatusType.OK, StatusType.ZERO_RESULTS, StatusType.OVER_QUERY_LIMIT, StatusType.REQUEST_DENIED, StatusType.INVALID_REQUEST, StatusType.UNKNOWN_ERROR})
    public @interface StatusType {
        String OK = "OK";
        String ZERO_RESULTS = "ZERO_RESULTS";
        String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
        String REQUEST_DENIED = "REQUEST_DENIED";
        String INVALID_REQUEST = "INVALID_REQUEST";
        String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    }
}
