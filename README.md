Smart Location Library
======================

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Smart%20Location%20Library-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1321) [![Build Status](https://travis-ci.org/mrmans0n/smart-location-lib.svg?branch=master)](https://travis-ci.org/mrmans0n/smart-location-lib)

Android library project that intends to simplify the usage of location providers and activity recognition with a nice fluid API.

**Supported Android versions**: Android 4.0+

Adding to your project
----------------------

You should add this to your dependencies:

```groovy
implement 'io.nlopez.smartlocation:library:3.3.3'
```

Google Play Services compatible version: 11.4.2

If you want the rxjava wrappers, these are now in a separate dependency. Just add this new dependency as well:

```groovy
implement 'io.nlopez.smartlocation:rx:3.3.3'
```

If you got any problem compiling, please check the Common Issues section at the bottom of this document.

## Location

### Starting

For starting the location service:

````java
SmartLocation.with(context).location()
    .start(new OnLocationUpdatedListener() { ... });
````

If you just want to get a single location (not periodic) you can just use the oneFix modifier. Example:

````java
SmartLocation.with(context).location()
    .oneFix()
    .start(new OnLocationUpdatedListener() { ... });
````

### Stopping

For stopping the location just use the stop method.

````java
SmartLocation.with(context).location().stop();
````

### Status

You can get some information about the current status of location providers to know if you will be able to use the location providers.

````java
// Check if the location services are enabled
SmartLocation.with(context).location().state().locationServicesEnabled();

// Check if any provider (network or gps) is enabled
SmartLocation.with(context).location().state().isAnyProviderAvailable();

// Check if GPS is available
SmartLocation.with(context).location().state().isGpsAvailable();

// Check if Network is available
SmartLocation.with(context).location().state().isNetworkAvailable();

// Check if the passive provider is available
SmartLocation.with(context).location().state().isPassiveAvailable();

// Check if the location is mocked
SmartLocation.with(context).location().state().isMockSettingEnabled();
````

### Location strategy

There are three presets for location parameters:

* `LocationParams.BEST_EFFORT` (default)
* `LocationParams.NAVIGATION`
* `LocationParams.LAZY`

You can change it (if you want one other than the default one) by using the `config(locationParams)` modifier.

If you want to add some custom parameters for the distances or times involved in the location strategy, you can create your own LocationParams class.

### Changing providers

There are some providers shipped with the library.

* `LocationGooglePlayServicesWithFallbackProvider` (default). This one will use the Fused Location Provider if it's present, or the LocationManager as fallback if it's not.
* `LocationGooglePlayServicesProvider` This will use the Fused Location Provider.
* `LocationManagerProvider` This is the legacy implementation that uses LocationManager.
* `LocationBasedOnActivityProvider` This allows you to use the activity recognition system to modify the location strategy depending on the activity changes (if the user is walking, running, on a car, a bike...).
* `MultiFallbackLocationProvider` This lets you create your own "fallback provider" if the underlying location service is not available. See "Multiple Fallback Provider" below for details.

You can implement your own if you want. That's ideal if you wanted to use a mock one for testing or something like that, or add support to another possible provider.

Example:

````java
SmartLocation.with(context).location(new LocationBasedOnActivityProvider(callback))
    .start(new OnLocationUpdatedListener() { ... });
````

### Multiple Fallback Provider

The `MultiFallbackProvider` lets you create your own provider that utilizes multiple underlying location services.
The provider will use the location services in the order in which they are added to its `Builder`, which has convenience methods for setting up the Google Play Services provider and the default `LocationManager` provider.
Providers must implement the `ServiceLocationProvider` interface to enable the fallback behavior.
Example:

````java
LocationProvider myProvider = new MyLocationProvider();
LocationProvider fallbackProvider = new MultiFallbackProvider.Builder()
    .withGooglePlayServicesProvider().withProvider(myProvider).build();
````

## Activity

### Starting

For starting the activity recognition service, you should run:

````java
SmartLocation.with(context).activityRecognition()
    .start(new OnActivityUpdatedListener() { ... });
````

### Stopping

For stopping the activity recognition you could use the stop method.

````java
SmartLocation.with(context).activityRecognition().stop();
````

## Geofencing

We can add geofences and receive the information when we enter, exit or dwell in a Geofence. The geofences are defined by a GeofenceModel, and you should use the requestId as a identifier.

We can add and remove geofences with a similar syntax as all the others.

````java
GeofenceModel mestalla = new GeofenceModel.Builder("id_mestalla")
    .setTransition(Geofence.GEOFENCE_TRANSITION_ENTER)
    .setLatitude(39.47453120000001)
    .setLongitude(-0.358065799999963)
    .setRadius(500)
    .build();

GeofenceModel cuenca = new GeofenceModel.Builder("id_cuenca")
    .setTransition(Geofence.GEOFENCE_TRANSITION_EXIT)
    .setLatitude(40.0703925)
    .setLongitude(-2.1374161999999615)
    .setRadius(2000)
    .build();

SmartLocation.with(context).geofencing()
    .add(mestalla)
    .add(cuenca)
    .remove("already_existing_geofence_id")
    .start(new OnGeofencingTransitionListener() { ... });
````

If you want to capture the Geofence transitions without the app running, you can hook up a BroadcastReceiver to the intent action stored in the `GeofencingGooglePlayServicesProvider.BROADCAST_INTENT_ACTION` constant. The intent will come with the geofence, the location and the type of transition within the bundle.

## Geocoding

The library has support for direct geocoding (aka getting a Location object based on a String) and reverse geocoding (getting the Street name based on a Location object).

There are pretty basic calls in the API for both operations separatedly.

### Direct geocoding
````java
SmartLocation.with(context).geocoding()
    .direct("Estadi de Mestalla", new OnGeocodingListener() {
        @Override
        public void onLocationResolved(String name, List<LocationAddress> results) {
            // name is the same you introduced in the parameters of the call
            // results could come empty if there is no match, so please add some checks around that
            // LocationAddress is a wrapper class for Address that has a Location based on its data
            if (results.size() > 0) {
            	Location mestallaLocation = results.get(0).getLocation();
            	// [...] Do your thing! :D
            }
        }
    });
````

### Reverse geocoding
````java
SmartLocation.with(context).geocoding()
    .reverse(location, new OnReverseGeocodingListener() {
        @Override
        public onAddressResolved(Location original, List<Address> results) {
            // ...
        }
    });
````

### Mixing things up

But we can mix and batch those requests, if needed. Also, you can provide the number of maximum possible matches you want to receive for each one of the lookups separatedly.

````java
Location myLocation1 = new Location(...);

SmartLocation.with(context).geocoding()
    .add("Estadi de Mestalla", 5)
    .add("Big Ben", 2)
    .add(myLocation1, 4)
    .start(directGeocodingListener, reverseGeocodingListener);
````

This will launch a new call to the callbacks everytime one of the geofence lookups is resolved.

### Stopping

You should invoke the stop method whenever the calling activity/fragment or whatever is going to be destroyed, for cleanup purposes.

## RxJava / RxAndroid support

The wrappers to rxjava2 are located in this package.

```groovy
implement 'io.nlopez.smartlocation:rx:3.3.1'
```

You can wrap the calls with ObservableFactory methods to retrieve an Observable object. You won't need to call start, just subscribe to the observable to get the updates.

For example, for location:

```java
Observable<Location> locationObservable = ObservableFactory.from(SmartLocation.with(context).location());
locationObservable.subscribe(new Action1<Location>() {
    @Override
    public void call(Location location) {
        // Do your stuff here :)
    }
});
```

Common issues
-------------

If you are already using Google Play Services in your project and have problems compiling, you can try setting the transitive property to false:

```groovy
implement ('io.nlopez.smartlocation:library:3.3.3') {
	transitive = false
}
```

If you got an error in the manifest merging, like this one: 

```
> Manifest merger failed : Attribute meta-data#com.google.android.gms.version@value value=(@integer/google_play_services_version) from AndroidManifest.xml:44:13
    is also present at io.nlopez.smartlocation:library:3.0.5:28:13 value=(6587000)
    Suggestion: add 'tools:replace="android:value"' to <meta-data> element at AndroidManifest.xml:42:9 to override


    Error:(46, 13) Attribute meta-data#com.google.android.gms.version@value value=(@integer/google_play_services_version) from AndroidManifest.xml:46:13
```

If you follow the suggestion provided, you can get rid of it easily. Just change in your manifest the meta-data tag with the google play services version, like this:

```xml
<meta-data tools:replace="android:value" android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />
```

Contributing
------------
Forks, patches and other feedback are welcome.

Creators
--------

Nacho López @mrmans0n

License
-------

The MIT License (MIT)

Copyright (c) 2013-2017 Nacho Lopez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
