package io.nlopez.smartlocation;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.location.LocationUpdatedListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.subjects.SingleSubject;

/**
 * Creates RxJava Observables for all the library calls.
 */
public final class Observables {
    private Observables() {
        throw new AssertionError("This should not be instantiated");
    }

    /**
     * Returns a RxJava Observable for Location changes
     *
     * @param locationBuilder instance with the needed configuration
     * @return Observable for Location changes
     */
    @NonNull
    public static Observable<Location> from(@NonNull final SmartLocation.LocationBuilder locationBuilder) {
        return Observable.create(new ObservableOnSubscribe<Location>() {
            @Override
            public void subscribe(final ObservableEmitter<Location> emitter) throws Exception {
                locationBuilder.start(new LocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(@Nullable Location location) {
                        emitter.onNext(location);
                    }

                    @Override
                    public void onAllProvidersFailed() {
                        emitter.onError(new RuntimeException("All providers failed"));
                    }
                });
            }
        }).doOnDispose(stopLocation(locationBuilder))
                .doAfterTerminate(stopLocation(locationBuilder));
    }

    @NonNull
    private static Action stopLocation(final @NonNull SmartLocation.LocationBuilder locationBuilder) {
        return new Action() {
            @Override
            public void run() throws Exception {
                locationBuilder.stop();
            }
        };
    }

    /**
     * Returns a RxJava single for direct geocoding results, aka get a Location from an address or name of a place.
     *
     * @param geocodingBuilder configuration for the geocoding operation to perform
     * @param address          address or name of the place we want to get the location of
     * @return Single for results. Gets a terminal event after the response.
     */
    @NonNull
    public static Single<List<LocationAddress>> fromAddress(
            @NonNull final SmartLocation.GeocodingBuilder geocodingBuilder,
            @NonNull final String address) {
        return SingleSubject.create(new SingleOnSubscribe<List<LocationAddress>>() {
            @Override
            public void subscribe(final SingleEmitter<List<LocationAddress>> emitter) {
                geocodingBuilder.findLocationByName(address, new GeocodingUpdatedListener() {
                    @Override
                    public void onAllProvidersFailed() {
                        emitter.onError(new RuntimeException("All providers failed"));
                    }

                    @Override
                    public void onLocationResolved(@NonNull String name, @NonNull List<LocationAddress> results) {
                        emitter.onSuccess(results);
                    }
                });

            }
        });
    }

    /**
     * Returns a RxJava single for reverse geocoding results, aka get an address from a Location.
     *
     * @param geocodingBuilder configuration for the reverse geocoding operation to perform
     * @param location         location we want to know the address of
     * @return Single for results. Gets a terminal event after the response
     */
    @NonNull
    public static Single<List<LocationAddress>> fromLocation(
            @NonNull final SmartLocation.GeocodingBuilder geocodingBuilder,
            @NonNull final Location location) {
        return SingleSubject.create(new SingleOnSubscribe<List<LocationAddress>>() {
            @Override
            public void subscribe(final SingleEmitter<List<LocationAddress>> emitter) {
                geocodingBuilder.findNameByLocation(location,
                        new ReverseGeocodingUpdatedListener() {
                            @Override
                            public void onAllProvidersFailed() {
                                emitter.onError(new RuntimeException("All providers failed"));
                            }

                            @Override
                            public void onAddressResolved(@NonNull Location original, @NonNull List<LocationAddress> results) {
                                emitter.onSuccess(results);
                            }
                        });
            }
        });
    }
}
