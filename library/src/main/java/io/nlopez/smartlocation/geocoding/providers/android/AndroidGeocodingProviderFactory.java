package io.nlopez.smartlocation.geocoding.providers.android;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.Locale;

import io.nlopez.smartlocation.common.Provider;
import io.nlopez.smartlocation.geocoding.GeocodingProvider;
import io.nlopez.smartlocation.geocoding.GeocodingProviderFactory;
import io.nlopez.smartlocation.utils.Logger;
import io.nlopez.smartlocation.utils.LoggerFactory;

public class AndroidGeocodingProviderFactory implements GeocodingProviderFactory {

    private final Logger mLogger;
    private final Locale mLocale;

    public AndroidGeocodingProviderFactory() {
        this(LoggerFactory.get(), Locale.getDefault());
    }

    public AndroidGeocodingProviderFactory(@NonNull Logger logger, @NonNull Locale locale) {
        mLogger = logger;
        mLocale = locale;
    }

    @NonNull
    @Override
    public GeocodingProvider create(Context context, Provider.StatusListener statusListener) {
        return new AndroidGeocodingProvider(context, statusListener, mLogger, mLocale);
    }
}
