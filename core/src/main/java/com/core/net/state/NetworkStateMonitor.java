package com.core.net.state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.core.utils.net.NetworkUtils;
import com.core.utils.ui.ToastUtils;

import java.util.Objects;

public class NetworkStateMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showShortToast("网络连接差");
            }
        }
    }
}