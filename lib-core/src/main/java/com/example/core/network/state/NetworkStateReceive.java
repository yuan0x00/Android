package com.example.core.network.state;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.core.utils.NetworkUtils;
import com.example.core.utils.ToastUtils;

import java.util.Objects;

public class NetworkStateReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showShortToast("网络连接差");
            }
        }
    }
}