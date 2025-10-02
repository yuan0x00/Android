package com.core.network.state;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class NetworkStateManager implements DefaultLifecycleObserver {

    private static final NetworkStateManager S_MANAGER = new NetworkStateManager();
    private final NetworkStateMonitor mNetworkStateMonitor = new NetworkStateMonitor();
    private Context appContext;

    private NetworkStateManager() {
    }

    public static NetworkStateManager getInstance() {
        return S_MANAGER;
    }

    // tip：让 NetworkStateManager 可观察页面生命周期，
    // 从而在页面失去焦点时，
    // 及时断开本页面对网络状态的监测，以避免重复回调和一系列不可预期的问题。

    // 关于 Lifecycle 组件的存在意义，可详见《为你还原一个真实的 Jetpack Lifecycle》篇的解析
    // https://xiaozhuanlan.com/topic/3684721950

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        Context context = extractContext(owner);
        if (context == null) {
            return;
        }
        appContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        appContext.registerReceiver(mNetworkStateMonitor, filter);
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (appContext != null) {
            try {
                appContext.unregisterReceiver(mNetworkStateMonitor);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private Context extractContext(@NonNull LifecycleOwner owner) {
        if (owner instanceof Context) {
            return (Context) owner;
        }
        return appContext;
    }
}
