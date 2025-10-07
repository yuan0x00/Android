package com.rapid.android.core.network.state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.rapid.android.core.log.LogKit;
import com.rapid.android.core.network.util.NetworkUtils;

import java.util.Objects;

public class NetworkStateMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!NetworkUtils.isConnected(context.getApplicationContext())) {
                LogKit.w("NetworkState", "Network connectivity lost");
            }
        }
    }
}
