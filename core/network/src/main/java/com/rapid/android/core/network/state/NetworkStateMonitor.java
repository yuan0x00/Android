package com.rapid.android.core.network.state;

import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;

import com.rapid.android.core.log.LogKit;

public class NetworkStateMonitor extends ConnectivityManager.NetworkCallback {

    @Override
    public void onAvailable(@NonNull Network network) {
        LogKit.d("NetworkState", "Network available: %s", network.toString());
    }

    @Override
    public void onLost(@NonNull Network network) {
        LogKit.w("NetworkState", "Network connectivity lost");
    }

    @Override
    public void onUnavailable() {
        LogKit.w("NetworkState", "Network unavailable");
    }
}
