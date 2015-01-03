Smart Location Library
======================

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
SmartLocation.with(context).location().start(new OnLocationUpdatedListener() { ... });
````

If you just want to get a single location (not periodic) you can just use the oneFix modifier. Example:

````java
SmartLocation.with(context).location().oneFix().start(new OnLocationUpdatedListener() { ... });
````

### Stopping

For stopping the location you could use the stop method.

````java
SmartLocation.with(context).location().stop();
````

## Activity

### Starting

For starting the activity recognition service, you should run:

````java
SmartLocation.with(context).activityRecognition().start(new OnActivityUpdatedListener() { ... });

### Stopping

For stopping the activity recognition you could use the stop method.

````java
SmartLocation.with(context).activityRecognition().stop();
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
