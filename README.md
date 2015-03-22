Smart Location Library
======================

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Smart%20Location%20Library-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1321) [![Build Status](https://travis-ci.org/mrmans0n/smart-location-lib.svg?branch=master)](https://travis-ci.org/mrmans0n/smart-location-lib)

Android library project that intends to simplify the usage of location providers and activity recognition with a nice fluid API.

**Supported Android versions**: Android 2.3+

Adding to your project
----------------------

You should add this to your dependencies:

```groovy
compile 'io.nlopez.smartlocation:library:3.0.5'
```

If you are already using Google Play Services in your project and have problems compiling, you can try setting the transitive property to false:

```groovy
compile ('io.nlopez.smartlocation:library:3.0.5'){
	transitive = false
}
```

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

### Location strategy

There are three presets for location parameters:

* `LocationParams.BEST_EFFORT` (default)
* `LocationParams.NAVIGATION`
* `LocationParams.LAZY`

You can change it (if you want one other than the default one) by using the `config(locationParams)` modifier.

If you want to add some custom parameters for the distances or times involved in the location strategy, you can create your own LocationParams class.

### Changing providers

There are some providers shipped with the library.

* `LocationGooglePlayServicesProvider` (default). This will use the Fused Location Provider.
* `LocationManagerProvider` This is the legacy implementation that uses LocationManager.
* `LocationBasedOnActivityProvider` This allows you to use the activity recognition system to modify the location strategy depending on the activity changes (if the user is walking, running, on a car, a bike...).

You can implement your own if you want. That's ideal if you wanted to use a mock one for testing or something like that, or add support to another possible provider.

Example:

````java
SmartLocation.with(context).location()
    .provider(new LocationBasedOnActivityProvider(callback))
    .start(new OnLocationUpdatedListener() { ... });
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

This is still experimental. Please don't use this feature in production yet :)

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

Contributing
------------
Forks, patches and other feedback are welcome.

Creators
--------

Nacho López @mrmans0n

License
-------

The MIT License (MIT)

Copyright (c) 2013-2015 Nacho Lopez

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
