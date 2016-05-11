package io.nlopez.smartlocation.location.providers;

import com.google.android.gms.common.ConnectionResult;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.GooglePlayServicesListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * A {@link LocationProvider} that allows multiple location services to be used.
 * <br/><br/>
 * New instances of <code>MultiFallbackProvider</code> must be initialized via the Builder class:
 * <pre>
 * LocationProvider provider = new MultiLocationProvider.Builder()
 *         .withGooglePlayServicesProvider()
 *         .withDefaultProvider()
 *         .build();
 * </pre>
 * <code>MultiFallbackProvider</code> will attempt to use the location services in the order they
 * were added to the builder.  If the provider fails to connect to the underlying service, the next
 * provider in the list is used.
 * <br/><br/>
 * If no providers are added to the builder, the {@link LocationManagerProvider} is used by default.
 *
 * @author abkaplan07
 */
public class MultiFallbackProvider implements LocationProvider {

    private Queue<LocationProvider> providers;
    private LocationProvider currentProvider;
    private Context context;
    private Logger logger;
    private OnLocationUpdatedListener locationListener;
    private LocationParams locationParams;
    private boolean singleUpdate;
    private boolean shouldStart;


    MultiFallbackProvider() {
        this.providers = new LinkedList<>();
    }

    @Override
    public void init(Context context, Logger logger) {
        this.context = context;
        this.logger = logger;
        LocationProvider current = getCurrentProvider();
        if (current != null) {
            current.init(context, logger);
        }

    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean
            singleUpdate) {
        this.shouldStart = true;
        this.locationListener = listener;
        this.locationParams = params;
        this.singleUpdate = singleUpdate;
        LocationProvider current = getCurrentProvider();
        if (current != null) {
            current.start(listener, params, singleUpdate);
        }
    }

    @Override
    public void stop() {
        LocationProvider current = getCurrentProvider();
        if (current != null) {
            current.stop();
        }

    }

    @Override
    public Location getLastLocation() {
        LocationProvider current = getCurrentProvider();
        if (current == null) {
            return null;
        }
        return current.getLastLocation();
    }

    boolean addProvider(LocationProvider provider) {
        return providers.add(provider);
    }

    Collection<LocationProvider> getProviders() {
        return this.providers;
    }

    LocationProvider getCurrentProvider() {
        if (currentProvider == null && !providers.isEmpty()) {
            this.currentProvider = providers.poll();
        }
        return currentProvider;
    }

    void fallbackProvider() {
        if (!providers.isEmpty()) {
            currentProvider = providers.poll();
            currentProvider.init(context, logger);
            if (shouldStart) {
                currentProvider.start(locationListener, locationParams, singleUpdate);
            }
        }
    }

    /**
     * Builder class for the {@link MultiFallbackProvider}.
     */
    public static class Builder {

        private MultiFallbackProvider builtProvider;

        public Builder() {
            this.builtProvider = new MultiFallbackProvider();
        }

        /**
         * Adds Google Location Services as a provider.
         */
        public Builder withGooglePlayServicesProvider() {
            GooglePlayServicesListener googleListener = new GooglePlayServicesListener() {
                @Override
                public void onConnected(Bundle bundle) {
                    // noop
                }

                @Override
                public void onConnectionSuspended(int i) {
                    builtProvider.fallbackProvider();
                }

                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    builtProvider.fallbackProvider();
                }
            };
            LocationGooglePlayServicesProvider googleProvider = new
                    LocationGooglePlayServicesProvider(googleListener);
            return this.withProvider(googleProvider);
        }

        /**
         * Adds the built-in Android Location Manager as a provider.
         */
        public Builder withDefaultProvider() {
            return this.withProvider(new LocationManagerProvider());
        }

        /**
         * Adds the given {@link LocationProvider} as a provider.
         * @param provider a <code>LocationProvider</code> instance.
         */
        public Builder withProvider(LocationProvider provider) {
            builtProvider.addProvider(provider);
            return this;
        }

        public MultiFallbackProvider build() {
            // Always ensure we have the default provider
            if (builtProvider.providers.isEmpty()) {
                withDefaultProvider();
            }
            return builtProvider;
        }
    }
}
