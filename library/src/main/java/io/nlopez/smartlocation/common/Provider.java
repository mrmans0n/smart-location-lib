package io.nlopez.smartlocation.common;

import android.support.annotation.NonNull;

public interface Provider {
    void release();

    interface StatusListener {
        void onProviderFailed(@NonNull Provider provider);
    }
}
