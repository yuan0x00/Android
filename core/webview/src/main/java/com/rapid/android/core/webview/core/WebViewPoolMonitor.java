package com.rapid.android.core.webview.core;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebView 池运行态监控，便于快速定位池容量、命中率等问题。
 */
public final class WebViewPoolMonitor {

    private static final String TAG = "WebViewMonitor";
    private static final WebViewPoolMonitor INSTANCE = new WebViewPoolMonitor();

    private final AtomicInteger created = new AtomicInteger();
    private final AtomicInteger reused = new AtomicInteger();
    private final AtomicInteger destroyed = new AtomicInteger();
    private final AtomicInteger prewarmed = new AtomicInteger();

    private volatile int lastLoggedAvailable = -1;

    private WebViewPoolMonitor() {
    }

    @NonNull
    public static WebViewPoolMonitor getInstance() {
        return INSTANCE;
    }

    public void onPrewarm(int count) {
        if (count <= 0) {
            return;
        }
        prewarmed.addAndGet(count);
        Log.d(TAG, "Prewarmed WebView count=" + count + ", totalPrewarm=" + prewarmed.get());
    }

    public void onAcquire(boolean reusedInstance, int available, int maxSize) {
        if (reusedInstance) {
            reused.incrementAndGet();
        } else {
            created.incrementAndGet();
        }
        logSnapshot("acquire", available, maxSize);
    }

    public void onRelease(boolean recycled, int available, int maxSize) {
        if (!recycled) {
            destroyed.incrementAndGet();
        }
        logSnapshot("release", available, maxSize);
    }

    public void onClear(int clearedCount) {
        if (clearedCount > 0) {
            destroyed.addAndGet(clearedCount);
            Log.w(TAG, "WebView pool cleared, clearedCount=" + clearedCount);
        }
        lastLoggedAvailable = -1;
    }

    public void onTrimMemory(int level) {
        Log.w(TAG, "Trim memory level=" + level);
    }

    private void logSnapshot(String reason, int available, int maxSize) {
        if (available == lastLoggedAvailable) {
            return;
        }
        lastLoggedAvailable = available;
        Log.d(TAG, String.format("%s: available=%d, max=%d, created=%d, reused=%d, destroyed=%d",
                reason,
                available,
                maxSize,
                created.get(),
                reused.get(),
                destroyed.get()));
    }
}
