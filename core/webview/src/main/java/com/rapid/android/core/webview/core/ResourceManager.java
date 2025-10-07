package com.rapid.android.core.webview.core;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebView资源管理器
 * 负责WebView的内存管理和资源优化
 */
public class ResourceManager {
    private static final String TAG = "ResourceManager";
    private static final long MEMORY_CHECK_INTERVAL = 30 * 1000; // 30秒检查一次
    private static final float MEMORY_WARNING_THRESHOLD = 0.8f; // 80%内存使用率警告
    private static final float MEMORY_CRITICAL_THRESHOLD = 0.9f; // 90%内存使用率严重警告

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);

    private ResourceListener listener;
    private Runnable memoryCheckRunnable;

    public ResourceManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 设置资源监听器
     */
    public void setResourceListener(ResourceListener listener) {
        this.listener = listener;
    }

    /**
     * 开始资源监控
     */
    public void startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            memoryCheckRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isMonitoring.get()) {
                        return;
                    }

                    checkMemoryUsage();
                    mainHandler.postDelayed(this, MEMORY_CHECK_INTERVAL);
                }
            };
            mainHandler.post(memoryCheckRunnable);
            Log.d(TAG, "Resource monitoring started");
        }
    }

    /**
     * 停止资源监控
     */
    public void stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            if (memoryCheckRunnable != null) {
                mainHandler.removeCallbacks(memoryCheckRunnable);
                memoryCheckRunnable = null;
            }
            Log.d(TAG, "Resource monitoring stopped");
        }
    }

    /**
     * 检查内存使用情况
     */
    private void checkMemoryUsage() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return;
        }

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem;
        long availableMemory = memoryInfo.availMem;
        long usedMemory = totalMemory - availableMemory;

        float usedMB = usedMemory / 1024f / 1024f;
        float totalMB = totalMemory / 1024f / 1024f;
        float usagePercent = usedMemory * 1.0f / totalMemory;

        // 检查内存阈值
        if (usagePercent >= MEMORY_CRITICAL_THRESHOLD) {
            Log.w(TAG, String.format("Critical memory usage: %.1fMB / %.1fMB (%.1f%%)",
                    usedMB, totalMB, usagePercent * 100));
            if (listener != null) {
                listener.onMemoryCritical(usedMB, totalMB, usagePercent);
            }
        } else if (usagePercent >= MEMORY_WARNING_THRESHOLD) {
            Log.w(TAG, String.format("High memory usage: %.1fMB / %.1fMB (%.1f%%)",
                    usedMB, totalMB, usagePercent * 100));
            if (listener != null) {
                listener.onMemoryWarning(usedMB, totalMB, usagePercent);
            }
        }

        // 检查是否需要GC
        if (shouldRecommendGC(usagePercent)) {
            if (listener != null) {
                listener.onRecommendGC();
            }
        }
    }

    /**
     * 判断是否应该推荐GC
     */
    private boolean shouldRecommendGC(float usagePercent) {
        // 简单的GC推荐策略：内存使用率超过75%且可用内存不足100MB
        return usagePercent > 0.75f && getAvailableMemoryMB() < 100;
    }

    /**
     * 获取可用内存(MB)
     */
    private float getAvailableMemoryMB() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return 0;
        }

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        return memoryInfo.availMem / 1024f / 1024f;
    }

    /**
     * 强制垃圾回收
     */
    public void forceGC() {
        Log.d(TAG, "Forcing garbage collection");
        System.gc();
        System.runFinalization();
        System.gc();
    }

    /**
     * 获取当前内存信息
     */
    public MemoryInfo getCurrentMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return new MemoryInfo(0, 0, 0);
        }

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem;
        long availableMemory = memoryInfo.availMem;
        long usedMemory = totalMemory - availableMemory;

        return new MemoryInfo(
                usedMemory / 1024f / 1024f,
                totalMemory / 1024f / 1024f,
                availableMemory / 1024f / 1024f
        );
    }

    public interface ResourceListener {
        /**
         * 内存警告
         * @param usedMB 已使用内存(MB)
         * @param totalMB 总内存(MB)
         * @param usagePercent 使用率百分比
         */
        void onMemoryWarning(float usedMB, float totalMB, float usagePercent);

        /**
         * 内存严重不足
         * @param usedMB 已使用内存(MB)
         * @param totalMB 总内存(MB)
         * @param usagePercent 使用率百分比
         */
        void onMemoryCritical(float usedMB, float totalMB, float usagePercent);

        /**
         * 推荐进行垃圾回收
         */
        void onRecommendGC();
    }

    /**
     * 内存信息数据类
     */
    public static class MemoryInfo {
        public final float usedMB;
        public final float totalMB;
        public final float availableMB;

        public MemoryInfo(float usedMB, float totalMB, float availableMB) {
            this.usedMB = usedMB;
            this.totalMB = totalMB;
            this.availableMB = availableMB;
        }

        public float getUsagePercent() {
            return totalMB > 0 ? (usedMB / totalMB) : 0;
        }

        @Override
        public String toString() {
            return String.format("MemoryInfo{used=%.1fMB, total=%.1fMB, available=%.1fMB, usage=%.1f%%}",
                    usedMB, totalMB, availableMB, getUsagePercent() * 100);
        }
    }
}
