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

### Gradle

You should add this to your dependencys:

```groovy
    compile 'com.mobivery.greent.smartlocation:library:1.0.3'
```

And you should add Mobivery's repository to your repositories:

```groovy
repositories {

    // ... blah blah the rest of your repositories ...

    maven {
        url 'http://maven-repo.mobivery.com.s3.amazonaws.com/release'
    }
}
```

### Maven

You should add this dependency.

````xml
<dependency>
	<groupId>com.mobivery.greent.smartlocation</groupId>
	<artifactId>library</artifactId>
	<version>1.0.3</version>
</dependency>
````

And you should add Mobivery's repository to the pom.xml <repositories> tag also.

````xml
<repository>
	<id>mobivery-repository</id>
    <url>http://maven-repo.mobivery.com.s3.amazonaws.com/release</url>
</repository>
````

Permissions
-----------

You must add these permissions to your AndroidManifest.xml. 

````xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION"/>
<uses-permission android:name="com.google.android.providers.gsf.permission.READ_GSERVICES"/>

````

You also have to add two services to your application node in the manifest.

````xml
<service android:name="com.mobivery.greent.smartlocation.ActivityRecognitionService"/>
<service android:name="com.mobivery.greent.smartlocation.SmartLocationService"/>
````

Check out the sample project for seeing how it should be in a real situation.

Internally, the ActivityRecognitionService will provide with the user's activity information to the SmartLocationService, which will be the one deciding on the best settings for the location usage at any time.

Usage
-----

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

For stopping the location (but with a chance to restarting it) you should use the stop method.

````java
SmartLocation.getInstance().stop(context);
````

For stopping it for good and avoid service leaks, you must call the cleanup method this way.

````java
SmartLocation.getInstance().cleanup(context);
````

The best practices for using these methods would be:

* Adding the **start** call on the onResume method of the Activity.
* Adding the **stop** call on the onPause method of the Activity.
* Adding the **cleanup** call on the onDestroy method of the Activity.

The Service will also send the information of user's location and current activity via intents.

The default intent that will be broadcasted will be `com.mobivery.smartlocation.LOCATION_UPDATED`. You can configure the package by passing a SmartLocationOptions object to the start method but more on that later. You can capture it if you want, but the listener should be enough.

Customizing to your needs
-------------------------
In the following example you can see how to setup a new package for the LOCATION_UPDATED intent.

````java
        SmartLocationOptions options = new SmartLocationOptions();
        options.setPackageName("com.mypackage.name");
        SmartLocation.getInstance().start(context, options);
````

With this call, the intent you will want to watch for is `com.mypackage.name.LOCATION_UPDATED`.

We can also customize the update strategy in the options class, both the default behavior and the behavior derived from the activity recognizer. Check out the comments of the UpdateStrategy class for more info. By default, it is set to **BEST_EFFORT** location, which will perform the best location accuracy without the battery drainage of only GPS location strategies.

For example, we will set a typical strategy for a navigation app (for both cars and bicycles) in this code.

````java
        SmartLocationOptions options = new SmartLocationOptions();
        options.setDefaultUpdateStrategy(UpdateStrategy.BEST_EFFORT);
        options.setOnLocationUpdatedNewStrategy(new SmartLocationOptions.OnLocationUpdated() {
            @Override
            public UpdateStrategy getUpdateStrategyForActivity(int detectedActivity) {
                switch (detectedActivity) {
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

Contributing
------------
Forks, patches and other feedback are welcome.

Creators
--------

Nacho López @mrmans0n

License
-------

The MIT License (MIT)

Copyright (c) 2013 Mobivery

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
