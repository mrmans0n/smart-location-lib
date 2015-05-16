package io.nlopez.smartlocation;

import android.location.Address;
import android.location.Location;

import java.util.List;

/**
 * Created by mrm on 4/1/15.
 */
public interface OnReverseGeocodingListener {
    void onAddressResolved(Location original, List<Address> results);
}