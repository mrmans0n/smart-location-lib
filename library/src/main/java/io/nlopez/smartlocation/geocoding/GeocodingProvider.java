package io.nlopez.smartlocation.geocoding;

import android.content.Context;
import android.location.Location;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by mrm on 20/12/14.
 */
public interface GeocodingProvider {
    void init(Context context, Logger logger);

    void fromName(String name, int maxResults);

    void fromLocation(Location location, int maxResults);

    void stop();

}
