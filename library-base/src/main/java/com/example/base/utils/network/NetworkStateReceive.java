package com.example.base.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.base.utils.NetworkUtils;
import com.example.base.utils.ToastUtils;

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