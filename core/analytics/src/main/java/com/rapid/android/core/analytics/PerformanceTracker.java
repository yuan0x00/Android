package com.rapid.android.core.analytics;

import android.os.SystemClock;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能监控追踪器
 * 用于追踪和分析应用性能指标
 */
public class PerformanceTracker {

    private static volatile PerformanceTracker instance;
    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private final Map<String, PerformanceMetrics> metrics = new ConcurrentHashMap<>();

    private PerformanceTracker() {
    }

    public static PerformanceTracker getInstance() {
        if (instance == null) {
            synchronized (PerformanceTracker.class) {
                if (instance == null) {
                    instance = new PerformanceTracker();
                }
            }
        }
        return instance;
    }

    /**
     * 开始追踪
     */
    public void start(@NonNull String traceName) {
        startTimes.put(traceName, SystemClock.elapsedRealtime());
    }

    /**
     * 停止追踪并记录
     */
    public long stop(@NonNull String traceName) {
        Long startTime = startTimes.remove(traceName);
        if (startTime == null) {
            return -1;
        }

        long duration = SystemClock.elapsedRealtime() - startTime;

        // 更新指标
        PerformanceMetrics metric = metrics.get(traceName);
        if (metric == null) {
            metric = new PerformanceMetrics(traceName);
            metrics.put(traceName, metric);
        }
        metric.record(duration);

        // 记录到统计系统
        Map<String, Object> params = new HashMap<>();
        params.put("trace_name", traceName);
        params.put("duration_ms", duration);
        AnalyticsManager.getInstance().logEvent("performance_trace", params);

        return duration;
    }

    /**
     * 取消追踪
     */
    public void cancel(@NonNull String traceName) {
        startTimes.remove(traceName);
    }

    /**
     * 追踪网络请求
     */
    public void trackNetworkRequest(@NonNull String url, long duration, boolean success) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("duration_ms", duration);
        params.put("success", success);
        AnalyticsManager.getInstance().logEvent("network_request", params);
    }

    /**
     * 追踪页面加载时间
     */
    public void trackPageLoad(@NonNull String pageName, long duration) {
        Map<String, Object> params = new HashMap<>();
        params.put("page_name", pageName);
        params.put("load_time_ms", duration);
        AnalyticsManager.getInstance().logEvent("page_load", params);
    }

    /**
     * 追踪内存使用
     */
    public void trackMemoryUsage(long usedMemory, long totalMemory) {
        Map<String, Object> params = new HashMap<>();
        params.put("used_memory_mb", usedMemory / (1024 * 1024));
        params.put("total_memory_mb", totalMemory / (1024 * 1024));
        params.put("usage_percent", (int) ((usedMemory * 100) / totalMemory));
        AnalyticsManager.getInstance().logEvent("memory_usage", params);
    }

    /**
     * 追踪 FPS
     */
    public void trackFPS(int fps) {
        Map<String, Object> params = new HashMap<>();
        params.put("fps", fps);
        AnalyticsManager.getInstance().logEvent("fps", params);
    }

    /**
     * 获取性能指标
     */
    @NonNull
    public PerformanceMetrics getMetrics(@NonNull String traceName) {
        PerformanceMetrics metric = metrics.get(traceName);
        if (metric == null) {
            return new PerformanceMetrics(traceName);
        }
        return metric;
    }

    /**
     * 清除所有追踪数据
     */
    public void clear() {
        startTimes.clear();
        metrics.clear();
    }

    /**
     * 性能指标
     */
    public static class PerformanceMetrics {
        private final String name;
        private int count = 0;
        private long totalDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        private long maxDuration = 0;

        public PerformanceMetrics(@NonNull String name) {
            this.name = name;
        }

        void record(long duration) {
            count++;
            totalDuration += duration;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
        }

        @NonNull
        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public long getAverageDuration() {
            return count > 0 ? totalDuration / count : 0;
        }

        public long getMinDuration() {
            return minDuration == Long.MAX_VALUE ? 0 : minDuration;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public long getTotalDuration() {
            return totalDuration;
        }

        @NonNull
        @Override
        public String toString() {
            return "PerformanceMetrics{" +
                    "name='" + name + '\'' +
                    ", count=" + count +
                    ", avg=" + getAverageDuration() + "ms" +
                    ", min=" + getMinDuration() + "ms" +
                    ", max=" + maxDuration + "ms" +
                    '}';
        }
    }
}
