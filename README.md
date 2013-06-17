smart-location-lib
==================

Android library project that intends to use the minimum battery drain possible

Getting started
---------------

You should add this dependency.

````xml
<dependency>
	<groupId>com.mobivery.smartlocation</groupId>
	<artifactId>library</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
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
<service android:name="com.mobivery.smartlocation.ActivityRecognitionService"/>
<service android:name="com.mobivery.smartlocation.SmartLocationService"/>
````

Check out the sample project for seeing how it should be in a real situation.

Usage
-----

Placeholder.