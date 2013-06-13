package com.mobivery.smartlocation;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by mrm on 13/06/13.
 */
public class SmartLocationService extends Service {

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SmartLocationService getService() {
            return SmartLocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLocation();
        initActivityRecognition();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocation();
    }

    private void initLocation() {
        locationClient = new LocationClient(this, this, this);
        locationRequest = LocationRequest.create();
    }


    private void initActivityRecognition() {

    }

    private void stopLocation() {

    }


}
