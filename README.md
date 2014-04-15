Smart Location Library
======================

Android library project that intends to use the minimum battery drain possible when used in navigation apps.

It allows any Android project to smartly control the location sensors usage when using it to access user's location data, using it as a replacement to the system Location APIs.
The principle behind it is detecting the activity the user is doing (moving in a vehicle, riding a bicycle, walking, running, being still) and based on those results, changing the accuracy for the detection and the sensors used. In our case, we will detect if we are moving in a vehicle.

All this is done to be more battery efficient than the usual location strategies.

**Supported Android versions**: Android 2.3+

Context
-------

Under the umbrella of the Green-T project, Mobivery has invested in the developing the present software library having two main purposes in mind:

* Contribute to preservation of battery life, *within the scope of Green-T*
* Encapsulate and simplify the use of common resources for geolocation to be reusable and extensible

![Green-T](http://greent.av.it.pt/images/logo.png)

Getting started
---------------

You should add this to your dependencies:

```groovy
compile 'io.nlopez.smartlocation:library:2.+'
```

Permissions
-----------

These permissions **will be automatically merged into your AndroidManifest.xml** by gradle. There is **NO NEED for you to add them** to your app, though I'm leaving them here so you know what's happening behind closed doors.

````xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION"/>
<uses-permission android:name="com.google.android.providers.gsf.permission.READ_GSERVICES"/>
````

The same happens with these services.

````xml
<service android:name="io.nlopez.smartlocation.ActivityRecognitionService"/>
<service android:name="io.nlopez.smartlocation.SmartLocationService"/>
````

Check out the sample project for seeing how it should be in a real situation.

Internally, the ActivityRecognitionService will provide with the user's activity information to the SmartLocationService, which will be the one deciding on the best settings for the location usage at any time.

Usage
-----

### Starting location

For starting the location service, you can perform just one call with a listener.

````java
SmartLocation.getInstance().start(
    context,
    new SmartLocation.OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location, DetectedActivity detectedActivity) {
            // In here you have the location and the activity. Do whatever you want with them!
        }
    });
````

You have some different methods to be able to do this in different ways.

This one above is the simplest one, because it would do all the boilerplate stuff for you. But you could also intercept an intent that is launched everytime the location is updated (see below).

### Stopping location

For stopping the location (but with a chance to restarting it) you should use the stop method.

````java
SmartLocation.getInstance().stop(context);
````

### Cleanup location

For stopping it for good and avoid service leaks, you must call the cleanup method this way.

````java
SmartLocation.getInstance().cleanup(context);
````

### Where you should do the calls

The best practices for using these methods would be:

* Adding the **start** call on the onResume method of the Activity.
* Adding the **stop** call on the onPause method of the Activity.
* Adding the **cleanup** call on the onDestroy method of the Activity.

The Service will also send the information of user's location and current activity via intents.

The default intent that will be broadcasted will be `io.nlopez.smartlocation.LOCATION_UPDATED`. You can configure the package by passing a SmartLocationOptions object to the start method but more on that later. You can capture it if you want, but the listener should be enough.

Customizing to your needs
-------------------------


We can customize the update strategy in the options class, both the default behavior and the behavior derived from the activity recognizer. Check out the comments of the UpdateStrategy class for more info. By default, it is set to **BEST_EFFORT** location, which will perform the best location accuracy without the battery drainage of pure-GPS location strategies.

For example, we will set a typical strategy for a navigation app (for both cars and bicycles) in this code.

````java
        SmartLocationOptions options = new SmartLocationOptions();
        options.setDefaultUpdateStrategy(UpdateStrategy.BEST_EFFORT);
        options.setOnLocationUpdatedNewStrategy(new SmartLocationOptions.OnLocationUpdated() {
            @Override
            public UpdateStrategy getUpdateStrategyForActivity(DetectedActivity detectedActivity) {
                switch (detectedActivity.getType()) {
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
                        return UpdateStrategy.NAVIGATION;
                    default:
                        return UpdateStrategy.BEST_EFFORT;
                }
            }
        });
        SmartLocation.getInstance().start(context, options);
````

We can force the deactivation of the fused location provider strategies (using LocationManager instead) and the Activity Recognizer with some settings. We would want to do this if we wanted to get the speed value from the GPS (thus, using NAVIGATION as UpdateStrategy) and we don't care about the activity the user is performing. 

```java
    SmartLocationOptions options = new SmartLocationOptions()
                                    .setDefaultUpdateStrategy(UpdateStrategy.NAVIGATION)
                                    .setFusedProvider(false)
                                    .setActivityRecognizer(false);
```

Of course, they both exist on their own and it is possible to use the Activity Recognizer with a LocationManager based strategy if we wanted to.

Please note that it is allowed to interact with the options object in a fluid fashion.

And we can enable or disable the debug mode. In the debug mode, we would have some feedback in logcat about how it is going with the localization stuff. It is recommended the value is false always for production builds. You could use something like this line.

```java
    options.setDebugging(BuildConfig.DEBUG);
```

If we want to interact with the intents being fired by the service, instead of using the interface provided, you can do it. You can either use the default intent action, `io.nlopez.smartlocation.LOCATION_UPDATED`, or set up your own.

````java
    options.setPackageName("com.mypackage.name");
````

With this call, the intent you will want to watch for is `com.mypackage.name.LOCATION_UPDATED`.

Contributing
------------
Forks, patches and other feedback are welcome.

Creators
--------

Nacho López @mrmans0n

License
-------

The MIT License (MIT)

Copyright (c) 2013-2014 Nacho Lopez

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
