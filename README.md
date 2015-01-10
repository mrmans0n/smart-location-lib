Smart Location Library
======================

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Smart%20Location%20Library-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1321)

Android library project that intends to simplify the usage of location providers and activity recognition with a nice fluid API.

**Supported Android versions**: Android 2.3+

Getting started
---------------

You should add this to your dependencies:

```groovy
compile 'io.nlopez.smartlocation:library:3.0.0'
```

Usage
-----

## Location

### Starting

For starting the location service, you should run:

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

For stopping the location you could use the stop method.

````java
SmartLocation.with(context).location().stop();
````

### Location strategy

There are three presets for location parameters: LocationParams.BEST_EFFORT, LocationParams.NAVIGATION and LocationParams.LAZY. By default, this will use the BEST_EFFORT one.

If you want to add some custom parameters for the distances or times involved in the location strategy, you can provide a custom LocationParams class, with the `config(yourParams)` modifier.

### Changing providers

There are some providers shipped with the library.

* LocationGooglePlayServicesProvider (default). This will use the Fused Location Provider.
* LocationManagerProvider. This is the legacy implementation that uses LocationManager.
* LocationBasedOnActivityProvider. This allows you to use the activity recognition system to modify the location strategy depending on the activity changes (if the user is walking, running, on a car, a bike...).

You can implement your own if you want and feed it to the library.

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

This is still experimental.

TODO write this

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
