package io.nlopez.smartlocation.location.providers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.utils.Logger;

/**
 *  Baidu Maps Location Provider.
 *  <p>To use this as a location provider, a valid Baidu API key must be added to the application
 *  manifest.</p>
 *  @author abkaplan07
 */
public class LocationBaiduProvider implements LocationProvider, BDLocationListener {

    private Logger logger;
    private LocationClient locationClient;
    private OnLocationUpdatedListener listener;
    private boolean oneShot = false;


    @Override
    public void init(Context context, Logger logger) {
        this.logger = logger;
        if (this.locationClient == null) {
            // Initialize the Baidu Location SDK
            SDKInitializer.initialize(context.getApplicationContext());
            this.locationClient = new LocationClient(context);
            this.logger.d("init", "initialized baidu location client");
        }
    }

    @Override
    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean
        singleUpdate) {
        logger.d("start", "started receiving baidu map updates");
        if (this.locationClient == null) {
            throw new IllegalStateException("Location client not initialized!");
        }
        this.listener = listener;
        if (listener == null) {
            logger.d("Listener is null, you sure about this?");
        }
        LocationClientOption options = new LocationClientOption();
        // Configure client to use GPS (always)
        options.setOpenGps(true);
        options.setIsNeedAddress(false);
        options.setIsNeedLocationDescribe(false);
        // Baidu does not support intervals < 1 second
        // Distance-based location updates are not supported yet.
        if (params.getInterval() < 1000) {
            options.setScanSpan(1000);
        } else {
            options.setScanSpan((int) params.getInterval());
        }
        switch (params.getAccuracy()) {
            case HIGH:
                options.setLocationMode(LocationMode.Hight_Accuracy);
                break;
            case MEDIUM:
                options.setLocationMode(LocationMode.Device_Sensors);
                break;
            default:
                options.setLocationMode(LocationMode.Battery_Saving);
                break;
        }
        this.locationClient.setLocOption(options);
        this.oneShot = singleUpdate;
        this.locationClient.registerLocationListener(this);
        if (!this.locationClient.isStarted()) {
            this.locationClient.start();
        }
    }

    @Override
    public void stop() {
        this.locationClient.unRegisterLocationListener(this);
        this.locationClient.stop();

    }

    @Override
    public Location getLastLocation() {
        if (this.locationClient == null) {
            return null;
        }
        BDLocation lastLoc = this.locationClient.getLastKnownLocation();
        if (lastLoc == null) {
            return null;
        }
        return toAndroidLocation(lastLoc);
    }

    private Location toAndroidLocation(@NonNull BDLocation location) {
        Location loc = new Location(LocationManager.NETWORK_PROVIDER);
        loc.setAccuracy(location.getRadius());
        loc.setAltitude(location.getAltitude());
        loc.setBearing(location.getDirection());
        loc.setLatitude(location.getLatitude());
        loc.setLongitude(location.getLongitude());
        loc.setSpeed(location.getSpeed());
        // TODO: Parse time to millis from epoch
        return loc;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        logger.d("location received", bdLocation);
        if (!isError(bdLocation)) {
            if (oneShot) {
                this.stop();
            }
            if (this.listener != null) {
                this.listener.onLocationUpdated(toAndroidLocation(bdLocation));
            }
        }
    }

    private boolean isError(BDLocation bdLocation) {
        return bdLocation.getLocType() != BDLocation.TypeCriteriaException
            && bdLocation.getLocType() != BDLocation.TypeNetWorkException
            && bdLocation.getLocType() != BDLocation.TypeOffLineLocationFail
            && bdLocation.getLocType() != BDLocation.TypeOffLineLocationNetworkFail
            && bdLocation.getLocType() != BDLocation.TypeServerError;
    }


}
