package com.rapid.android.core.network.state;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkStateManager implements DefaultLifecycleObserver {

    private static final NetworkStateManager S_MANAGER = new NetworkStateManager();
    private final NetworkStateMonitor networkCallback = new NetworkStateMonitor();
    private final AtomicInteger activeOwners = new AtomicInteger(0);
    private ConnectivityManager connectivityManager;
    private Context appContext;
    private boolean registered = false;

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
        ensureConnectivityManager();
        if (connectivityManager == null) {
            return;
        }

        if (activeOwners.incrementAndGet() == 1) {
            registerCallback();
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        if (activeOwners.get() <= 0) {
            activeOwners.set(0);
            return;
        }

        if (activeOwners.decrementAndGet() <= 0) {
            unregisterCallback();
        }
    }

    private Context extractContext(@NonNull LifecycleOwner owner) {
        if (owner instanceof Context) {
            return (Context) owner;
        }
        return appContext;
    }

    private void ensureConnectivityManager() {
        if (connectivityManager == null && appContext != null) {
            connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    private void registerCallback() {
        if (registered || connectivityManager == null) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            } else {
                NetworkRequest request = new NetworkRequest.Builder().build();
                connectivityManager.registerNetworkCallback(request, networkCallback);
            }
            registered = true;
        } catch (SecurityException | IllegalArgumentException ignored) {
            registered = false;
        }
    }

    private void unregisterCallback() {
        if (!registered || connectivityManager == null) {
            return;
        }
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (IllegalArgumentException ignored) {
        } finally {
            registered = false;
        }
    }
}
