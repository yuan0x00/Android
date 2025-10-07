package com.rapid.android.core.webview.core;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

/**
 * 统一监听系统内存信号，按需清理 WebView 池资源。
 */
public final class WebViewPoolSupervisor implements ComponentCallbacks2 {

    private static volatile boolean initialized = false;

    private final Context appContext;

    private WebViewPoolSupervisor(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void ensureInitialized(@NonNull Context context) {
        if (initialized) {
            return;
        }
        synchronized (WebViewPoolSupervisor.class) {
            if (initialized) {
                return;
            }
            WebViewPoolSupervisor supervisor = new WebViewPoolSupervisor(context);
            supervisor.appContext.registerComponentCallbacks(supervisor);
            initialized = true;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            WebViewFactory.getInstance(appContext).clearPool();
            WebViewPoolMonitor.getInstance().onTrimMemory(level);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        // no-op
    }

    @Override
    public void onLowMemory() {
        WebViewFactory.getInstance(appContext).clearPool();
        WebViewPoolMonitor.getInstance().onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
    }
}
