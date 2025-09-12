package com.webview.core;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

/**
 * 封装WebView预热逻辑，避免在业务侧重复实现 IdleHandler。
 */
public final class WebViewPrewarmer {

    private WebViewPrewarmer() {}

    /**
     * 在主线程空闲时预热指定数量的WebView，降低首屏创建开销。
     * 不影响冷启动的关键路径。
     */
    @MainThread
    public static void prewarmInIdle(@NonNull Context context, int count) {
        Looper.myQueue().addIdleHandler(() -> {
            try {
                WebViewFactory.getInstance(context).prewarm(count);
            } catch (Throwable ignored) {}
            return false;
        });
    }
}


