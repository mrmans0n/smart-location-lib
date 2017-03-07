package io.nlopez.smartlocation.rx;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeocodingListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geocoding.utils.LocationAddress;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
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
 * <p/>
 * For now it provides just basic support for all the available actions.
 */
public class ObservableFactory {
    private ObservableFactory() {
        throw new AssertionError("This should not be instantiated");
    }

    /**
     * Returns a RxJava Observable for Location changes
     *
     * @param locationControl instance with the needed configuration
     * @return Observable for Location changes
     */
    public static Observable<Location> from(final SmartLocation.LocationControl locationControl) {
        return Observable.create(new ObservableOnSubscribe<Location>() {
            @Override
            public void subscribe(final ObservableEmitter<Location> emitter) throws Exception {
                locationControl.start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        emitter.onNext(location);
                    }
                });
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                locationControl.stop();
            }
        });
    }

    /**
     * Returns a RxJava Observable for Activity Recognition changes
     *
     * @param activityControl instance with the needed configuration
     * @return Observable for Activity changes
     */
    public static Observable<DetectedActivity> from(final SmartLocation.ActivityRecognitionControl activityControl) {
        return Observable.create(new ObservableOnSubscribe<DetectedActivity>() {
            @Override
            public void subscribe(final ObservableEmitter<DetectedActivity> emitter) throws Exception {
                activityControl.start(new OnActivityUpdatedListener() {
                    @Override
                    public void onActivityUpdated(DetectedActivity detectedActivity) {
                        emitter.onNext(detectedActivity);
                    }
                });
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() {
                activityControl.stop();
            }
        });
    }

    /**
     * Returns a RxJava Observable for Geofence transitions
     *
     * @param geofencingControl instance with the needed configuration
     * @return Observable for Geofence transitions (enter, exit, dwell)
     */
    public static Observable<TransitionGeofence> from(final SmartLocation.GeofencingControl geofencingControl) {
        return Observable.create(new ObservableOnSubscribe<TransitionGeofence>() {
            @Override
            public void subscribe(final ObservableEmitter<TransitionGeofence> emitter) {
                geofencingControl.start(new OnGeofencingTransitionListener() {
                    @Override
                    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {
                        emitter.onNext(transitionGeofence);
                    }
                });
            }
        }).doOnDispose(new Action() {
            @Override
            public void run() {
                geofencingControl.stop();
            }
        });
    }

    /**
     * Returns a RxJava single for direct geocoding results, aka get a Location from an address or name of a place.
     *
     * @param context    caller context
     * @param address    address or name of the place we want to get the location of
     * @param maxResults max number of coincidences to return
     * @return Single for results. Gets a terminal event after the response.
     */
    public static Single<List<LocationAddress>> fromAddress(final Context context, final String address, final int maxResults) {
        return SingleSubject.create(new SingleOnSubscribe<List<LocationAddress>>() {
            @Override
            public void subscribe(final SingleEmitter<List<LocationAddress>> emitter) {
                SmartLocation.with(context).geocoding().add(address, maxResults).start(new OnGeocodingListener() {
                    @Override
                    public void onLocationResolved(String name, List<LocationAddress> results) {
                        emitter.onSuccess(results);
                    }
                });

            }
        });
    }

    /**
     * Returns a RxJava single for reverse geocoding results, aka get an address from a Location.
     *
     * @param context    caller context
     * @param location   location we want to know the address od
     * @param maxResults max number of coincidences to return
     * @return Single for results. Gets a terminal event after the response
     */
    public static Single<List<Address>> fromLocation(final Context context, final Location location, final int maxResults) {
        return SingleSubject.create(new SingleOnSubscribe<List<Address>>() {
            @Override
            public void subscribe(final SingleEmitter<List<Address>> emitter) {
                SmartLocation.with(context).geocoding().add(location, maxResults).start(new OnReverseGeocodingListener() {
                    @Override
                    public void onAddressResolved(Location original, List<Address> results) {
                        emitter.onSuccess(results);
                    }
                });
            }
        });
    }

}
