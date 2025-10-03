package com.core.network.state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.core.log.LogKit;
import com.core.network.util.NetworkUtils;

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
