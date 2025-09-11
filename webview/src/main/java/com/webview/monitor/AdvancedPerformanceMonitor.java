package com.webview.monitor;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Choreographer;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 高级性能监控器
 * 提供全面的WebView性能监控功能
 */
public class AdvancedPerformanceMonitor {
    private static final String TAG = "AdvancedPerfMonitor";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final PerformanceData performanceData = new PerformanceData();
    // 内存监控
    private final AtomicLong lastMemoryCheckTime = new AtomicLong(0);
    // 页面加载监控
    private final AtomicReference<PageLoadMetrics> currentPageLoad = new AtomicReference<>();
    private PerformanceListener listener;
    private WebView webView;
    // 帧率监控
    private Choreographer.FrameCallback frameCallback;
    private long lastFrameTimeNanos = 0;
    private int frameCount = 0;
    private float currentFPS = 0;
    private MemoryInfo lastMemoryInfo;

    /**
     * 绑定WebView
     */
    public void bindWebView(@NonNull WebView webView) {
        this.webView = webView;
        startMonitoring();
    }

    /**
     * 解绑WebView
     */
    public void unbindWebView() {
        stopMonitoring();
        this.webView = null;
    }

    /**
     * 设置性能监听器
     */
    public void setPerformanceListener(PerformanceListener listener) {
        this.listener = listener;
    }

    /**
     * 开始页面加载监控
     */
    public void startPageLoad(String url) {
        PageLoadMetrics metrics = new PageLoadMetrics(url, SystemClock.elapsedRealtime());
        currentPageLoad.set(metrics);
        Log.d(TAG, "Started page load monitoring: " + url);
    }

    /**
     * 结束页面加载监控
     */
    public void endPageLoad(String url, boolean success) {
        PageLoadMetrics metrics = currentPageLoad.getAndSet(null);
        if (metrics != null) {
            metrics.endTime = SystemClock.elapsedRealtime();
            metrics.success = success;
            metrics.totalLoadTime = metrics.endTime - metrics.startTime;

            performanceData.recordPageLoad(metrics);

            if (listener != null) {
                listener.onPageLoadMetrics(metrics);
            }

            Log.d(TAG, "Page load completed: " + metrics);
        }
    }

    /**
     * 记录渲染事件
     */
    public void recordRenderEvent(String eventType, long durationMs) {
        RenderEvent event = new RenderEvent(eventType, durationMs, SystemClock.elapsedRealtime());
        performanceData.recordRenderEvent(event);

        // 检查是否是慢渲染
        if (durationMs > 16) { // 60FPS对应的帧时间
            PerformanceWarning warning = new PerformanceWarning(
                PerformanceWarning.Type.SLOW_RENDER,
                "Slow render detected: " + durationMs + "ms",
                durationMs
            );
            if (listener != null) {
                listener.onPerformanceWarning(warning);
            }
        }
    }

    /**
     * 记录JavaScript执行时间
     */
    public void recordJavaScriptExecution(String script, long executionTimeMs) {
        JavaScriptMetrics jsMetrics = new JavaScriptMetrics(script, executionTimeMs);
        performanceData.recordJavaScriptExecution(jsMetrics);

        // 检查是否是慢JavaScript执行
        if (executionTimeMs > 100) { // 100ms阈值
            PerformanceWarning warning = new PerformanceWarning(
                PerformanceWarning.Type.SLOW_JAVASCRIPT,
                "Slow JavaScript execution: " + executionTimeMs + "ms",
                executionTimeMs
            );
            if (listener != null) {
                listener.onPerformanceWarning(warning);
            }
        }
    }

    /**
     * 开始监控
     */
    private void startMonitoring() {
        startFPSMonitoring();
        startMemoryMonitoring();
    }

    /**
     * 停止监控
     */
    private void stopMonitoring() {
        stopFPSMonitoring();
        stopMemoryMonitoring();
    }

    /**
     * 开始FPS监控
     */
    private void startFPSMonitoring() {
        if (frameCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
        }

        frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (lastFrameTimeNanos != 0) {
                    long frameTimeMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000;
                    currentFPS = 1000f / frameTimeMs;

                    // 计算掉帧数（理想60FPS）
                    int expectedFrames = Math.round(frameTimeMs / (1000f / 60f));
                    int droppedFrames = Math.max(0, expectedFrames - 1);

                    if (listener != null) {
                        listener.onFPSUpdate(currentFPS, droppedFrames);
                    }

                    frameCount++;
                }

                lastFrameTimeNanos = frameTimeNanos;
                Choreographer.getInstance().postFrameCallback(this);
            }
        };

        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    /**
     * 停止FPS监控
     */
    private void stopFPSMonitoring() {
        if (frameCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            frameCallback = null;
        }
    }

    /**
     * 开始内存监控
     */
    private void startMemoryMonitoring() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (webView == null) {
                    return;
                }

                checkMemoryUsage();

                // 每5秒检查一次
                mainHandler.postDelayed(this, 5000);
            }
        }, 1000);
    }

    /**
     * 停止内存监控
     */
    private void stopMemoryMonitoring() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 检查内存使用
     */
    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        MemoryInfo info = new MemoryInfo(usedMemory, totalMemory, freeMemory, maxMemory);
        lastMemoryInfo = info;

        if (listener != null) {
            listener.onMemoryUpdate(info);
        }

        // 检查内存警告
        float usagePercent = (float) usedMemory / maxMemory;
        if (usagePercent > 0.85f) { // 85%内存使用率
            PerformanceWarning warning = new PerformanceWarning(
                PerformanceWarning.Type.HIGH_MEMORY_USAGE,
                String.format("High memory usage: %.1f%%", usagePercent * 100),
                usagePercent
            );
            if (listener != null) {
                listener.onPerformanceWarning(warning);
            }
        }
    }

    /**
     * 获取当前性能数据
     */
    @NonNull
    public PerformanceData getPerformanceData() {
        return performanceData;
    }

    /**
     * 获取当前FPS
     */
    public float getCurrentFPS() {
        return currentFPS;
    }

    /**
     * 获取最后内存信息
     */
    @Nullable
    public MemoryInfo getLastMemoryInfo() {
        return lastMemoryInfo;
    }

    public interface PerformanceListener {
        /**
         * FPS更新
         * @param fps 当前帧率
         * @param droppedFrames 掉帧数
         */
        void onFPSUpdate(float fps, int droppedFrames);

        /**
         * 内存使用更新
         * @param info 内存信息
         */
        void onMemoryUpdate(@NonNull MemoryInfo info);

        /**
         * 页面加载指标
         * @param metrics 加载指标
         */
        void onPageLoadMetrics(@NonNull PageLoadMetrics metrics);

        /**
         * 性能警告
         * @param warning 警告信息
         */
        void onPerformanceWarning(@NonNull PerformanceWarning warning);
    }

    /**
     * 内存信息
     */
    public static class MemoryInfo {
        public final long usedMemory;
        public final long totalMemory;
        public final long freeMemory;
        public final long maxMemory;

        public MemoryInfo(long usedMemory, long totalMemory, long freeMemory, long maxMemory) {
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
            this.maxMemory = maxMemory;
        }

        public float getUsagePercent() {
            return maxMemory > 0 ? (float) usedMemory / maxMemory : 0;
        }

        public float getUsedMemoryMB() {
            return usedMemory / 1024f / 1024f;
        }

        public float getTotalMemoryMB() {
            return totalMemory / 1024f / 1024f;
        }
    }

    /**
     * 页面加载指标
     */
    public static class PageLoadMetrics {
        public final String url;
        public final long startTime;
        public long endTime;
        public long totalLoadTime;
        public boolean success;
        public int resourceCount;
        public long totalResourceSize;

        public PageLoadMetrics(String url, long startTime) {
            this.url = url;
            this.startTime = startTime;
        }

        @Override
        public String toString() {
            return String.format("PageLoadMetrics{url='%s', loadTime=%dms, success=%s}",
                    url, totalLoadTime, success);
        }
    }

    /**
     * 渲染事件
     */
    public static class RenderEvent {
        public final String eventType;
        public final long durationMs;
        public final long timestamp;

        public RenderEvent(String eventType, long durationMs, long timestamp) {
            this.eventType = eventType;
            this.durationMs = durationMs;
            this.timestamp = timestamp;
        }
    }

    /**
     * JavaScript执行指标
     */
    public static class JavaScriptMetrics {
        public final String script;
        public final long executionTimeMs;
        public final long timestamp;

        public JavaScriptMetrics(String script, long executionTimeMs) {
            this.script = script;
            this.executionTimeMs = executionTimeMs;
            this.timestamp = SystemClock.elapsedRealtime();
        }
    }

    /**
     * 性能警告
     */
    public static class PerformanceWarning {
        public final Type type;
        public final String message;
        public final Object data;
        public final long timestamp;
        public PerformanceWarning(Type type, String message, Object data) {
            this.type = type;
            this.message = message;
            this.data = data;
            this.timestamp = SystemClock.elapsedRealtime();
        }

        public enum Type {
            SLOW_RENDER,
            SLOW_JAVASCRIPT,
            HIGH_MEMORY_USAGE,
            LOW_FPS
        }
    }

    /**
     * 性能数据汇总
     */
    public static class PerformanceData {
        private final java.util.List<PageLoadMetrics> pageLoads = new java.util.ArrayList<>();
        private final java.util.List<RenderEvent> renderEvents = new java.util.ArrayList<>();
        private final java.util.List<JavaScriptMetrics> jsExecutions = new java.util.ArrayList<>();

        public void recordPageLoad(PageLoadMetrics metrics) {
            synchronized (pageLoads) {
                pageLoads.add(metrics);
                // 保持最近100条记录
                if (pageLoads.size() > 100) {
                    pageLoads.remove(0);
                }
            }
        }

        public void recordRenderEvent(RenderEvent event) {
            synchronized (renderEvents) {
                renderEvents.add(event);
                if (renderEvents.size() > 200) {
                    renderEvents.remove(0);
                }
            }
        }

        public void recordJavaScriptExecution(JavaScriptMetrics metrics) {
            synchronized (jsExecutions) {
                jsExecutions.add(metrics);
                if (jsExecutions.size() > 100) {
                    jsExecutions.remove(0);
                }
            }
        }

        public java.util.List<PageLoadMetrics> getPageLoads() {
            synchronized (pageLoads) {
                return new java.util.ArrayList<>(pageLoads);
            }
        }

        public java.util.List<RenderEvent> getRenderEvents() {
            synchronized (renderEvents) {
                return new java.util.ArrayList<>(renderEvents);
            }
        }

        public java.util.List<JavaScriptMetrics> getJavaScriptExecutions() {
            synchronized (jsExecutions) {
                return new java.util.ArrayList<>(jsExecutions);
            }
        }

        public long getAveragePageLoadTime() {
            synchronized (pageLoads) {
                if (pageLoads.isEmpty()) return 0;
                long total = 0;
                for (PageLoadMetrics metrics : pageLoads) {
                    total += metrics.totalLoadTime;
                }
                return total / pageLoads.size();
            }
        }
    }
}
