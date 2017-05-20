package io.nlopez.smartlocation.geocoding;

import android.content.Context;

import io.nlopez.smartlocation.common.Factory2;
import io.nlopez.smartlocation.common.Provider;

public interface GeocodingProviderFactory extends Factory2<GeocodingProvider, Context, Provider.StatusListener> {
}
