package io.nlopez.smartlocation;

import android.location.Address;

import java.util.List;

/**
 * Created by mrm on 4/1/15.
 */
public interface OnGeocodingListener {
    void onLocationResolved(String name, List<Address> results);
}