package com.webview.monitor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.atomic.AtomicLong;

/**
 * WebView性能监控器
 * 监控页面加载时间、内存使用等性能指标
 */
public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 页面加载监控
    private final AtomicLong pageLoadStartTime = new AtomicLong(0);
    private final AtomicLong pageLoadCount = new AtomicLong(0);
    private final AtomicLong totalLoadTime = new AtomicLong(0);

    // 内存监控
    private final AtomicLong lastMemoryCheck = new AtomicLong(System.currentTimeMillis());
    private volatile boolean memoryMonitoringEnabled = true;

    private PerformanceListener listener;

    public void setPerformanceListener(PerformanceListener listener) {
        this.listener = listener;
    }

    /**
     * 开始页面加载监控
     */
    public void startPageLoad(String url) {
        long startTime = System.currentTimeMillis();
        pageLoadStartTime.set(startTime);
        Log.d(TAG, "Page load started: " + url);
    }

    /**
     * 结束页面加载监控
     */
    public void endPageLoad(String url) {
        long startTime = pageLoadStartTime.get();
        if (startTime == 0) {
            return; // 没有对应的开始时间
        }

        long endTime = System.currentTimeMillis();
        long loadTime = endTime - startTime;
        pageLoadStartTime.set(0); // 重置

        // 更新统计
        pageLoadCount.incrementAndGet();
        totalLoadTime.addAndGet(loadTime);

        Log.d(TAG, "Page load completed in " + loadTime + "ms: " + url);

        if (listener != null) {
            listener.onPageLoadCompleted(loadTime, url);

            // 检查是否为慢加载
            if (loadTime > 3000) { // 3秒阈值
                listener.onSlowLoadDetected(loadTime, 3000, url);
            }
        }
    }

    /**
     * 启用内存监控
     */
    public void enableMemoryMonitoring() {
        memoryMonitoringEnabled = true;
        startMemoryMonitoring();
    }

    /**
     * 禁用内存监控
     */
    public void disableMemoryMonitoring() {
        memoryMonitoringEnabled = false;
    }

    private void startMemoryMonitoring() {
        if (!memoryMonitoringEnabled) {
            return;
        }

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!memoryMonitoringEnabled) {
                    return;
                }

                checkMemoryUsage();
                // 每5秒检查一次
                mainHandler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        float usedMB = usedMemory / 1024f / 1024f;
        float maxMB = maxMemory / 1024f / 1024f;
        float usagePercent = (usedMemory * 100f) / maxMemory;

        if (listener != null) {
            listener.onMemoryUsage(usedMB, maxMB, usagePercent);

            // 内存使用超过80%时发出警告
            if (usagePercent > 80) {
                listener.onLowMemoryWarning(usedMB, maxMB);
                Log.w(TAG, String.format("High memory usage: %.1fMB / %.1fMB (%.1f%%)",
                        usedMB, maxMB, usagePercent));
            }
        }
    }

    /**
     * 获取平均页面加载时间
     */
    public long getAverageLoadTime() {
        long count = pageLoadCount.get();
        return count > 0 ? totalLoadTime.get() / count : 0;
    }

    /**
     * 获取总页面加载次数
     */
    public long getTotalLoadCount() {
        return pageLoadCount.get();
    }

    /**
     * 重置统计数据
     */
    public void reset() {
        pageLoadCount.set(0);
        totalLoadTime.set(0);
        pageLoadStartTime.set(0);
        Log.d(TAG, "Performance statistics reset");
    }

    /**
     * 停止监控
     */
    public void stop() {
        disableMemoryMonitoring();
        mainHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Performance monitoring stopped");
    }

    public interface PerformanceListener {
        /**
         * 页面加载完成
         * @param loadTimeMs 加载时间(毫秒)
         * @param url 加载的URL
         */
        void onPageLoadCompleted(long loadTimeMs, String url);

        /**
         * 慢加载检测
         * @param loadTimeMs 加载时间(毫秒)
         * @param thresholdMs 慢加载阈值(毫秒)
         * @param url 加载的URL
         */
        void onSlowLoadDetected(long loadTimeMs, long thresholdMs, String url);

        /**
         * 内存使用报告
         * @param usedMemoryMB 已使用内存(MB)
         * @param maxMemoryMB 最大内存(MB)
         * @param usagePercent 使用率百分比
         */
        void onMemoryUsage(float usedMemoryMB, float maxMemoryMB, float usagePercent);

        /**
         * 内存不足警告
         * @param usedMemoryMB 已使用内存(MB)
         * @param maxMemoryMB 最大内存(MB)
         */
        void onLowMemoryWarning(float usedMemoryMB, float maxMemoryMB);
    }
}
