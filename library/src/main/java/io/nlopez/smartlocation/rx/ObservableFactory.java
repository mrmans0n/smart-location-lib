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
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

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
        Observable<Location> result = Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                locationControl.start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        subscriber.onNext(location);
                    }
                });
                // TODO find a way to send back an onError message when necessary
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                locationControl.stop();
            }
        });

        return result;
    }

    /**
     * Returns a RxJava Observable for Activity Recognition changes
     *
     * @param activityControl instance with the needed configuration
     * @return Observable for Activity changes
     */
    public static Observable<DetectedActivity> from(final SmartLocation.ActivityRecognitionControl activityControl) {
        Observable<DetectedActivity> result = Observable.create(new Observable.OnSubscribe<DetectedActivity>() {
            @Override
            public void call(final Subscriber<? super DetectedActivity> subscriber) {
                activityControl.start(new OnActivityUpdatedListener() {
                    @Override
                    public void onActivityUpdated(DetectedActivity detectedActivity) {
                        subscriber.onNext(detectedActivity);
                    }
                });
                // TODO find a way to send back an onError message when necessary
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                activityControl.stop();
            }
        });
        return result;
    }

    /**
     * Returns a RxJava Observable for Geofence transitions
     *
     * @param geofencingControl instance with the needed configuration
     * @return Observable for Geofence transitions (enter, exit, dwell)
     */
    public static Observable<TransitionGeofence> from(final SmartLocation.GeofencingControl geofencingControl) {
        Observable<TransitionGeofence> result = Observable.create(new Observable.OnSubscribe<TransitionGeofence>() {
            @Override
            public void call(final Subscriber<? super TransitionGeofence> subscriber) {
                geofencingControl.start(new OnGeofencingTransitionListener() {
                    @Override
                    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {
                        subscriber.onNext(transitionGeofence);
                    }
                });
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                geofencingControl.stop();
            }
        });
        return result;
    }

    /**
     * Returns a RxJava Observable for direct geocoding results, aka get a Location from an address or name of a place.
     *
     * @param context    caller context
     * @param address    address or name of the place we want to get the location of
     * @param maxResults max number of coincidences to return
     * @return Observable for results
     */
    public static Observable<List<LocationAddress>> fromAddress(final Context context, final String address, final int maxResults) {
        Observable<List<LocationAddress>> result = Observable.create(new Observable.OnSubscribe<List<LocationAddress>>() {
            @Override
            public void call(final Subscriber<? super List<LocationAddress>> subscriber) {
                SmartLocation.with(context).geocoding().add(address, maxResults).start(new OnGeocodingListener() {
                    @Override
                    public void onLocationResolved(String name, List<LocationAddress> results) {
                        subscriber.onNext(results);
                        subscriber.onCompleted();
                    }
                });

            }
        });
        return result;
    }

    /**
     * Returns a RxJava Observable for reverse geocoding results, aka get an address from a Location.
     *
     * @param context    caller context
     * @param location   location we want to know the address od
     * @param maxResults max number of coincidences to return
     * @return Observable for results
     */
    public static Observable<List<Address>> fromLocation(final Context context, final Location location, final int maxResults) {
        Observable<List<Address>> result = Observable.create(new Observable.OnSubscribe<List<Address>>() {
            @Override
            public void call(final Subscriber<? super List<Address>> subscriber) {
                SmartLocation.with(context).geocoding().add(location, maxResults).start(new OnReverseGeocodingListener() {
                    @Override
                    public void onAddressResolved(Location original, List<Address> results) {
                        subscriber.onNext(results);
                        subscriber.onCompleted();
                    }
                });
            }
        });
        return result;
    }

}
