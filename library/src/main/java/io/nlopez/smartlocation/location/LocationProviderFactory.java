package io.nlopez.smartlocation.location;

import android.content.Context;

import io.nlopez.smartlocation.common.Factory2;
import io.nlopez.smartlocation.common.Provider;

public interface LocationProviderFactory extends Factory2<LocationProvider, Context, Provider.StatusListener> {
}
