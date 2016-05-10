package io.nlopez.smartlocation.utils;

/**
 * Listener for Baidu Services SDK
 */
public interface BaiduServicesListener {

    /**
     * Callback when the Baidu SDK successfully connects to Baidu Services.
     */
    void onConnected();

    /**
     * Callback when permission to use Baidu Services is denied.
     * @param errCode - error code from Baidu.
     */
    void onPermissionDenied(int errCode);

    /**
     * Callback when the Baidu SDK could not connect to Baidu Services.
     */
    void onConnectFailed();
}
