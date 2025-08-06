package com.example.reandroid.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.example.reandroid.R;
import com.example.reandroid.utils.NetworkUtils;
import com.example.reandroid.utils.ToastUtils;

import java.util.Objects;

public class NetworkStateReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!NetworkUtils.isConnected()) {
                ToastUtils.showShortToast(context.getString(R.string.network_not_good));
            }
        }
    }
}