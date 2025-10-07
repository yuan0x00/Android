package com.rapid.android.core.webview.core;

import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 智能WebView池管理器
 * 基于LRU策略和内存监控的智能池化管理
 */
public class SmartWebViewPool {
    private static final String TAG = "SmartWebViewPool";

    private final WebViewProvider provider;
    private final Deque<WebViewEntry> pool = new ConcurrentLinkedDeque<>();
    private final AtomicInteger currentSize = new AtomicInteger(0);
    private final WebViewPoolMonitor monitor = WebViewPoolMonitor.getInstance();

    private final int maxPoolSize;
    private final int minPoolSize;
    private final long maxIdleTime; // 最大空闲时间(毫秒)

    public SmartWebViewPool(@NonNull WebViewProvider provider,
                           int minPoolSize, int maxPoolSize,
                           long maxIdleTime) {
        this.provider = provider;
        this.minPoolSize = Math.max(1, minPoolSize);
        this.maxPoolSize = Math.max(this.minPoolSize, maxPoolSize);
        this.maxIdleTime = maxIdleTime;
    }

    public SmartWebViewPool(@NonNull WebViewProvider provider) {
        this(provider, 1, 5, 5 * 60 * 1000); // 默认配置
    }

    /**
     * 获取WebView实例
     */
    @NonNull
    public WebView acquireWebView() {
        synchronized (this) {
            // 清理过期实例
            cleanupExpiredEntries();

            WebViewEntry entry = pool.pollFirst();
            if (entry != null) {
                Log.d(TAG, "Reusing WebView from pool, remaining: " + pool.size());
                monitor.onAcquire(true, pool.size(), maxPoolSize);
                return entry.webView;
            }

            // 池为空，创建新实例
            Log.d(TAG, "Creating new WebView, pool size: " + currentSize.get());
            WebView webView = provider.createWebView();
            currentSize.incrementAndGet();
            monitor.onAcquire(false, pool.size(), maxPoolSize);
            return webView;
        }
    }

    /**
     * 归还WebView实例
     */
    public void releaseWebView(@NonNull WebView webView) {
        synchronized (this) {
            // 清理过期实例
            cleanupExpiredEntries();

            // 检查池是否已满
            if (pool.size() >= maxPoolSize) {
                // 池已满，销毁最老的实例
                WebViewEntry oldestEntry = pool.pollLast();
                if (oldestEntry != null) {
                    provider.destroyWebView(oldestEntry.webView);
                    currentSize.decrementAndGet();
                    Log.d(TAG, "Destroyed oldest WebView, pool size: " + currentSize.get());
                    monitor.onRelease(false, pool.size(), maxPoolSize);
                }
            }

            // 回收当前实例
            provider.recycleWebView(webView);
            WebViewEntry entry = new WebViewEntry(webView, System.currentTimeMillis());
            pool.addFirst(entry);

            Log.d(TAG, "WebView returned to pool, pool size: " + pool.size());
            monitor.onRelease(true, pool.size(), maxPoolSize);
        }
    }

    /**
     * 清理过期实例
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        int cleanedCount = 0;

        while (!pool.isEmpty()) {
            WebViewEntry entry = pool.peekLast();
            if (entry != null && (currentTime - entry.lastUsedTime) > maxIdleTime) {
                pool.pollLast();
                provider.destroyWebView(entry.webView);
                currentSize.decrementAndGet();
                cleanedCount++;
            } else {
                break; // 后面的实例都比这个新
            }
        }

        if (cleanedCount > 0) {
            Log.d(TAG, "Cleaned " + cleanedCount + " expired WebView instances");
            monitor.onClear(cleanedCount);
        }
    }

    /**
     * 根据内存压力调整池大小
     */
    public void adjustPoolSize(float memoryPressure) {
        synchronized (this) {
            int targetSize;

            if (memoryPressure > 0.9f) {
                // 高内存压力，减少池大小到最小
                targetSize = minPoolSize;
            } else if (memoryPressure > 0.7f) {
                // 中等内存压力，减少池大小
                targetSize = Math.max(minPoolSize, maxPoolSize / 2);
            } else {
                // 正常内存压力，保持最大池大小
                targetSize = maxPoolSize;
            }

            adjustToTargetSize(targetSize);
        }
    }

    /**
     * 调整到目标池大小
     */
    private void adjustToTargetSize(int targetSize) {
        while (pool.size() > targetSize && !pool.isEmpty()) {
            WebViewEntry entry = pool.pollLast();
            if (entry != null) {
                provider.destroyWebView(entry.webView);
                currentSize.decrementAndGet();
                monitor.onRelease(false, pool.size(), maxPoolSize);
            }
        }

        Log.d(TAG, "Adjusted pool size to: " + pool.size() + ", target: " + targetSize);
    }

    /**
     * 清空池
     */
    public void clear() {
        synchronized (this) {
            int cleared = 0;
            while (!pool.isEmpty()) {
                WebViewEntry entry = pool.poll();
                if (entry != null) {
                    provider.destroyWebView(entry.webView);
                    cleared++;
                }
            }
            currentSize.set(0);
            Log.d(TAG, "WebView pool cleared");
            monitor.onClear(cleared);
        }
    }

    /**
     * 获取当前池状态
     */
    public PoolStats getPoolStats() {
        synchronized (this) {
            return new PoolStats(pool.size(), currentSize.get(), maxPoolSize);
        }
    }

    /**
     * WebView条目包装类
     */
    private static class WebViewEntry {
        final WebView webView;
        final long lastUsedTime;

        WebViewEntry(WebView webView, long lastUsedTime) {
            this.webView = webView;
            this.lastUsedTime = lastUsedTime;
        }
    }

    /**
     * 池状态统计
     */
    public static class PoolStats {
        public final int availableCount;
        public final int totalCreated;
        public final int maxSize;

        public PoolStats(int availableCount, int totalCreated, int maxSize) {
            this.availableCount = availableCount;
            this.totalCreated = totalCreated;
            this.maxSize = maxSize;
        }

        public float getUtilizationRate() {
            return maxSize > 0 ? (float) availableCount / maxSize : 0;
        }

        @Override
        public String toString() {
            return String.format("PoolStats{available=%d, total=%d, max=%d, utilization=%.1f%%}",
                    availableCount, totalCreated, maxSize, getUtilizationRate() * 100);
        }
    }
}
