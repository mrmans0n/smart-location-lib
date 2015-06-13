package io.nlopez.smartlocation.rx;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * Creates RxJava Observables for all the library calls.
 * <p/>
 * For now it provides just basic support for location and activity recognition.
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
}
