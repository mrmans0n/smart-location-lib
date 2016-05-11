package io.nlopez.smartlocation.location.providers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import io.nlopez.smartlocation.utils.BaiduServicesListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Baidu Maps Location Provider.
 * <br/><br/>
 * To use this as a location provider, a valid Baidu API key must be added to the application
 * manifest. This provider by default initializes the Baidu SDK and checks if the application has
 * permission to use Baidu services. However, this can only be done <strong>once</strong> during
 * runtime.
 * <br/><br/>
 * Use the following code to create the provider without initializing the Baidu SDK:
 * <pre>
 *     LocationProvider baidu = new LocationBaiduProvider(false, false)
 *     ...
 *     // Baidu Broadcast Receiver
 *     baidu.setBaiduConnected(true)
 * </pre>
 * @author abkaplan07
 */
public class LocationBaiduProvider implements LocationProvider, BDLocationListener,
        BaiduServicesListener {

    private Logger logger;
    private Context context;
    private BaiduServicesListener baiduListener;
    private LocationClient locationClient;
    private OnLocationUpdatedListener listener;
    private boolean oneShot = false;
    private boolean initBaiduSdk = true;
    private LocationClientOption locationOptions;
    private boolean baiduConnected;
    private boolean shouldStart = false;
    private BroadcastReceiver sdkReceiver;

    /**
     * Creates a Baidu Location Services provider. The provider initializes the Baidu SDK.
     */
    public LocationBaiduProvider() {
        this(true, false, null);
    }

    /**
     * Creates a Baidu Location Services provider.
     *
     * @param initBaiduSdk - if <code>true</code>, initializes the Baidu SDK.
     */
    public LocationBaiduProvider(boolean initBaiduSdk) {
        this(initBaiduSdk, !initBaiduSdk, null);
    }

    /**
     * Creates a Baidu Location Services provider.
     * @param initBaiduSdk - if <code>true</code>, attempt to initialize the Baidu SDK.
     * @param baiduConnected - if <code>true</code>, indicates that we are connected to Baidu Services.
     */
    public LocationBaiduProvider(boolean initBaiduSdk, boolean baiduConnected) {
        this(initBaiduSdk, baiduConnected, null);
    }

    /**
     * Creates a Baidu Location Services provider. The provider initializes the Baidu SDK.
     *
     * @param baiduListener - a listener for Baidu SDK messages.
     */
    public LocationBaiduProvider(BaiduServicesListener baiduListener) {
        this(true, false, baiduListener);
    }

    LocationBaiduProvider(boolean initBaiduSdk, boolean baiduConnected, BaiduServicesListener
            baiduListener) {
        this.initBaiduSdk = initBaiduSdk;
        this.baiduConnected = baiduConnected;
        this.baiduListener = baiduListener;
    }


    @Override
    public void init(Context context, Logger logger) {
        this.logger = logger;
        this.context = context;
        this.logger.d("init", "initialized baidu location client");
        if (this.locationClient == null) {
            this.locationClient = new LocationClient(context);
        }
        if (initBaiduSdk && !baiduConnected) {
            // Initialize the Baidu Location SDK
            IntentFilter filter = new IntentFilter();
            filter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
            filter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
            filter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
            this.sdkReceiver = new BaiduSDKReceiver(this);
            context.registerReceiver(sdkReceiver, filter);
            SDKInitializer.initialize(context.getApplicationContext());
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
        locationOptions = createClientOptions(params);
        oneShot = singleUpdate;
        if (baiduConnected) {
            startUpdates(locationOptions);
        } else {
            logger.d("queued start of location updates");
            shouldStart = true;
        }
    }

    private void startUpdates(LocationClientOption options) {
        this.locationClient.setLocOption(options);
        this.locationClient.registerLocationListener(this);
        if (!this.locationClient.isStarted()) {
            this.locationClient.start();
        }
        // No longer need to start updates
        shouldStart = false;
    }

    @NonNull
    private LocationClientOption createClientOptions(LocationParams params) {
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
        return options;
    }

    @Override
    public void stop() {
        locationClient.unRegisterLocationListener(this);
        locationClient.stop();
        clearSdkReceiver();
    }

    private void clearSdkReceiver() {
        if (sdkReceiver != null) {
            context.unregisterReceiver(sdkReceiver);
            sdkReceiver = null;
        }
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

    /**
     * Sets the state of the Baidu connection. If connected, the provider will start location
     * updates if they have been queued.
     * @param baiduConnected - the status of the Baidu Services connection.
     */
    public void setBaiduConnected(boolean baiduConnected) {
        this.baiduConnected = baiduConnected;
        if (baiduConnected && shouldStart) {
            startUpdates(locationOptions);
        }
    }

    @Override
    public void onConnected() {
        logger.d("onConnected", "connected to baidu services");
        baiduConnected = true;
        if (shouldStart) {
            startUpdates(locationOptions);
        }
        if (baiduListener != null) {
            baiduListener.onConnected();
        }
    }

    @Override
    public void onPermissionDenied(int errCode) {
        logger.d("onPermissionDenied: $1%s: $2%d", "baidu permission denied", errCode);
        baiduConnected = false;
        if (baiduListener != null) {
            baiduListener.onPermissionDenied(errCode);
        }
    }

    @Override
    public void onConnectFailed() {
        logger.d("onConnectFailed: %s", "failed to connect to baidu services");
        baiduConnected = false;
        if (baiduListener != null) {
            baiduListener.onConnectFailed();
        }
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

    private boolean isError(BDLocation bdLocation) {
        return bdLocation.getLocType() != BDLocation.TypeCriteriaException
                && bdLocation.getLocType() != BDLocation.TypeNetWorkException
                && bdLocation.getLocType() != BDLocation.TypeOffLineLocationFail
                && bdLocation.getLocType() != BDLocation.TypeOffLineLocationNetworkFail
                && bdLocation.getLocType() != BDLocation.TypeServerError;
    }

    public class BaiduSDKReceiver extends BroadcastReceiver {

        BaiduServicesListener listener;

        public BaiduSDKReceiver(BaiduServicesListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SDKInitializer
                    .SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                clearSdkReceiver();
                if (listener != null) {
                    listener.onConnected();
                }
            } else if (intent.getAction().equals(SDKInitializer
                    .SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                int errCode = intent.getIntExtra(SDKInitializer
                        .SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0);
                clearSdkReceiver();
                if (listener != null) {
                    listener.onPermissionDenied(errCode);
                }
            } else if (intent.getAction().equals(SDKInitializer
                    .SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                clearSdkReceiver();
                if (listener != null) {
                    listener.onConnectFailed();
                }
            }
        }
    }


}
