Smart Location Library
======================

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Smart%20Location%20Library-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1321) [![Build Status](https://travis-ci.org/mrmans0n/smart-location-lib.svg?branch=master)](https://travis-ci.org/mrmans0n/smart-location-lib)

Android library project that intends to simplify the usage of location providers and activity recognition with a nice fluid API.

**Supported Android versions**: Android 4.0+

Adding to your project
----------------------

You should add this to your dependencies:

```groovy
compile 'io.nlopez.smartlocation:library:4.0.0'
```

Google Play Services compatible version: 15.0.0

If you want the rxjava2 wrappers, these are now in a separate dependency. Just add this new dependency as well:

```groovy
compile 'io.nlopez.smartlocation:rxjava2:4.0.0'
```

If you got any problem compiling, please check the Common Issues section at the bottom of this document.

## Location

### Starting

For starting the location service:

````java
SmartLocation.with(context).location()
    .start(new LocationUpdatedListener() { ... });
````

The start method returns a LocationController that you can store for manual clean up. 

````java
@Override
public void onResume() {
    super.onResume();
    mLocationController = SmartLocation.with(context).location()
        .start(new LocationUpdatedListener() { ... });
}

@Override
public void onPause() {
    super.onPause();
    mLocationController.stop(); // analogous to SmartLocation.with(context).location().stop();
}

@Override
public void onDestroy() {
    mLocationController.release(); 
    super.onDestroy();
}

````

Please note that the library will requests the permissions on its own if they are not present (for API >= Marshmallow).

### Location strategy

There are presets for location parameters:

* `LocationProviderParams.BEST_EFFORT` (default)
* `LocationProviderParams.NAVIGATION`
* `LocationProviderParams.LAZY`
* `LocationProviderParams.BEST_EFFORT_ONCE`
* `LocationProviderParams.NAVIGATION_ONCE`
* `LocationProviderParams.LAZY_ONCE`

You can select the one you want, if BEST_EFFORT doesn't suit your needs, by using the `config(locationParams)` modifier.

````java
SmartLocation.with(context).location()
    .config(LocationProviderParams.BEST_EFFORT_ONCE)
    .start(new LocationUpdatedListener() { ... });
````

If you want to add some custom parameters for the distances or times involved in the location strategy, you can create your own LocationParams class.

````java
SmartLocation.with(context).location()
    .config(new LocationProviderParams.Builder()
        .runOnlyOnce(true)
        .accuracy(LocationAccuracy.MEDIUM)
        .interval(10000)
        .distance(500)
        .build())
    .start(new LocationUpdatedListener() { ... });
````

*NOTE* If you want to run them only once, use the `_ONCE` parameters: BEST_EFFORT_ONCE, NAVIGATION_ONCE or LAZY_ONCE. Or as I said above, you can create you own with the builder LocationProviderParams.Builder class, with the runOnlyOnce value set to true.

You can see examples on how to do location in this library by taking a look to the [LocationFragment](sample/src/main/java/io/nlopez/smartlocation/sample/LocationFragment.java) in the sample app.

### Multiple providers, with fallback

By default we will be using Google Play Services, and if it can't be used for whatever reason, it will fall back to LocationManager (the Android old style).

But you can configure the providers you want to use and their priority. For instance, if you just want the LocationManager backed provider first and a mocked one as a fallback, and play services as last resort, you can! You just have to specify the provider factories in the order you want to run them. It will keep iterating through all of them until one gives a proper answer, or if not a single one of them gets a proper answer, the onAllProvidersFailed method would be run in the listener.

````java
SmartLocation.with(context).location(new LocationManagerProviderFactory(), new MockedProviderFactory(), new GooglePlayServicesLocationProviderFactory())
    .start(new LocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            mText.setText("Location: " + location.toString());
        }

        @Override
        public void onAllProvidersFailed() {
            mText.setText("All providers failed");
        }
    });
````

## Geofencing

We can add geofences and receive the information when we enter, exit or dwell in a Geofence. The geofences are defined by a Geofence model, and you should use the requestId as a identifier.

We can add and remove geofences with a similar syntax as all the others.

````java
// Prepare the geofence
final List<Geofence> geofenceList = new ArrayList<>();
geofenceList.add(new Geofence.Builder()
        .setRequestId("id_mestalla")
        .setCircularRegion(39.47453120000001, -0.358065799999963, 100)
        .setLoiteringDelay(30000) // half a minute
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .setExpirationDuration(60) // 1 minute
        .build());
// Prepare the request
final GeofencingRequest request = new GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofences(geofenceList)
        .build();

// Prepare a pending intent to reference your service that will be run when the geofence fires
final PendingIntent pendingIntent = PendingIntent.getService(getContext(),
        0,
        new Intent(getContext(), GeofenceIntentService.class),
        PendingIntent.FLAG_UPDATE_CURRENT);

// Add the geofence
SmartLocation.with(getContext())
        .geofencing()
        .addGeofences(request, getGeofencePendingIntent());
````

Look at the sample project, in the [GeofencingFragment](sample/src/main/java/io/nlopez/smartlocation/sample/GeofencingFragment.java) class, for a complete usage example for this.

## Geocoding

The library has support for direct geocoding (aka getting a Location object based on a String) and reverse geocoding (getting the Street name based on a Location object).

There are pretty basic calls in the API for both operations separatedly.

The basic android Geocoder support is built in in the core package for the library, but if you want to also add a Google Maps API provider, you can do so by adding this dependency to your project:

```groovy
compile 'io.nlopez.smartlocation:geocoding-googlemaps:4.0.0'
```

And don't forget to add the provider in your providers list, same syntax as the one described in the location example above.

### Direct geocoding (finding the geo location based on a name)
````java
SmartLocation.with(context).geocoding() 
    .findLocationByName("Estadi de Mestalla", new SimpleGeocodingUpdatedListener() {
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

To support both Geocoder and Google Maps API in the above example, switch the first line for this one: 
````java
SmartLocation.with(context).geocoding(new AndroidGeocodingProviderFactory(), new GoogleMapsApiGeocodingProviderFactory(GOOGLE_MAPS_API_KEY))
    .findLocationByName(...) // ... etc etc
````

### Reverse geocoding (finding a name based on a Location)
````java
SmartLocation.with(context).geocoding()
    .findNameByLocation(location, new SimpleReverseGeocodingUpdatedListener() {
        @Override
        public onAddressResolved(Location original, List<LocationAddress> results) {
            // ...
        }
    });
````

You can see the [GeocodingFragment](sample/src/main/java/io/nlopez/smartlocation/sample/GeocodingFragment.java) class on the sample application to see a practical example. 

## RxJava / RxAndroid support

The wrappers to rxjava2 are located in this package.

```groovy
compile 'io.nlopez.smartlocation:rxjava2:4.0.0'
```

You can wrap the calls with the `Observables` class static methods to retrieve an Observable object. You won't need to call start, just subscribe to the observable to get the updates.

For example, for location:

```java
Observables.from(SmartLocation.with(context).location())
    .subscribe(...);
```

When you unsubscribe from these observables, you would automatically stop location updates as well. Don't forget to unsubscrie otherwise you would have a memory leak.

For geocoding:
```java
Observables.fromAddress(SmartLocation.with(context).geocoding(), "221B Baker Street, London")
    .subscribe(...);
```

For reverse geocoding:
```java
Observables.fromLocation(SmartLocation.with(context).geocoding(), mLocation)
    .subscribe(...);
```

Please note both Geocoding and Reverse geocoding observables are implemented as Single to match their one off behavior.

Migrating from 3.x
------------------

Many things changed from 3.x version. I suggest you read this file and reimplement the calls using your IDE for autocompletion. Ideally only the listener classes will need to get changed, as most of the changes were internal. 

Some parameters have changed place, like `.oneFix()`. It has to be configured now as part of the LocationProviderParams class passed around in `config(...)`. 

Common issues
-------------

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

Copyright (c) 2013-2018 Nacho Lopez

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
