package io.nlopez.smartlocation;

import android.location.Location;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import io.nlopez.smartlocation.geocoding.GeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.ReverseGeocodingUpdatedListener;
import io.nlopez.smartlocation.geocoding.common.LocationAddress;
import io.nlopez.smartlocation.location.LocationUpdatedListener;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link Observables}
 */
@RunWith(RobolectricTestRunner.class)
public class ObservablesTest {
    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Mock private SmartLocation.LocationBuilder mLocationBuilder;
    @Mock private SmartLocation.GeocodingBuilder mGeocodingBuilder;
    @Mock private Location mLocation;
    @Mock private List<LocationAddress> mLocationAddresses;
    @Captor private ArgumentCaptor<LocationUpdatedListener> mLocationUpdatedListenerArgumentCaptor;
    @Captor private ArgumentCaptor<GeocodingUpdatedListener> mGeocodingUpdatedListenerArgumentCaptor;
    @Captor private ArgumentCaptor<ReverseGeocodingUpdatedListener> mReverseGeocodingUpdatedListenerArgumentCaptor;

    @Test
    public void testLocationObservableSendsValuesOnTheStream() {
        final Observable<Location> observable = Observables.from(mLocationBuilder);
        final TestObserver<Location> observer = observable.test();
        verify(mLocationBuilder).start(mLocationUpdatedListenerArgumentCaptor.capture());
        final LocationUpdatedListener listener = mLocationUpdatedListenerArgumentCaptor.getValue();
        listener.onLocationUpdated(mLocation);
        observer.assertValue(mLocation);
    }

    @Test
    public void testLocationObservableSendsErrorsOnTheStream() {
        final Observable<Location> observable = Observables.from(mLocationBuilder);
        final TestObserver<Location> observer = observable.test();
        verify(mLocationBuilder).start(mLocationUpdatedListenerArgumentCaptor.capture());
        final LocationUpdatedListener listener = mLocationUpdatedListenerArgumentCaptor.getValue();
        listener.onAllProvidersFailed();
        observer.assertErrorMessage("All providers failed");
        verify(mLocationBuilder).stop();
    }

    @Test
    public void testLocationObservableStopsLocationWhenUnsubscribing() {
        final Observable<Location> observable = Observables.from(mLocationBuilder);
        final TestObserver<Location> observer = observable.test();
        observer.dispose();
        verify(mLocationBuilder).stop();
    }

    @Test
    public void testGeocodingObservableReturnsAddressOnSuccess() {
        final Single<List<LocationAddress>> observable = Observables.fromAddress(mGeocodingBuilder, "address");
        final TestObserver<List<LocationAddress>> observer = observable.test();
        verify(mGeocodingBuilder).findLocationByName(eq("address"), mGeocodingUpdatedListenerArgumentCaptor.capture());
        final GeocodingUpdatedListener listener = mGeocodingUpdatedListenerArgumentCaptor.getValue();
        listener.onLocationResolved("address", mLocationAddresses);
        observer.assertValue(mLocationAddresses);
        observer.assertComplete();
    }

    @Test
    public void testGeocodingObservableReturnsErrorWhenFailed() {
        final Single<List<LocationAddress>> observable = Observables.fromAddress(mGeocodingBuilder, "address");
        final TestObserver<List<LocationAddress>> observer = observable.test();
        verify(mGeocodingBuilder).findLocationByName(eq("address"), mGeocodingUpdatedListenerArgumentCaptor.capture());
        final GeocodingUpdatedListener listener = mGeocodingUpdatedListenerArgumentCaptor.getValue();
        listener.onAllProvidersFailed();
        observer.assertErrorMessage("All providers failed");
    }

    @Test
    public void testReverseGeocodingObservableReturnsNameOnSuccess() {
        final Single<List<LocationAddress>> observable = Observables.fromLocation(mGeocodingBuilder, mLocation);
        final TestObserver<List<LocationAddress>> observer = observable.test();
        verify(mGeocodingBuilder).findNameByLocation(eq(mLocation), mReverseGeocodingUpdatedListenerArgumentCaptor.capture());
        final ReverseGeocodingUpdatedListener listener = mReverseGeocodingUpdatedListenerArgumentCaptor.getValue();
        listener.onAddressResolved(mLocation, mLocationAddresses);
        observer.assertValue(mLocationAddresses);
        observer.assertComplete();
    }

    @Test
    public void testReverseGeocodingObservableReturnsErrorWhenFailed() {
        final Single<List<LocationAddress>> observable = Observables.fromLocation(mGeocodingBuilder, mLocation);
        final TestObserver<List<LocationAddress>> observer = observable.test();
        verify(mGeocodingBuilder).findNameByLocation(eq(mLocation), mReverseGeocodingUpdatedListenerArgumentCaptor.capture());
        final ReverseGeocodingUpdatedListener listener = mReverseGeocodingUpdatedListenerArgumentCaptor.getValue();
        listener.onAllProvidersFailed();
        observer.assertErrorMessage("All providers failed");
    }
}