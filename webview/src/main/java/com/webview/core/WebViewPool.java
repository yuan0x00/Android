package com.webview.core;

import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * WebView对象池管理器
 * 实现WebView的复用，减少创建和销毁的开销
 */
public class WebViewPool {
    private static final String TAG = "WebViewPool";

    private final Queue<WebView> availableWebViews = new ConcurrentLinkedQueue<>();
    private final Map<WebView, Boolean> webViewStates = new ConcurrentHashMap<>();
    private final WebViewProvider provider;
    private final int maxPoolSize;
    private final Object lock = new Object();
    private final WebViewPoolMonitor monitor = WebViewPoolMonitor.getInstance();

    public WebViewPool(@NonNull WebViewProvider provider, int maxPoolSize) {
        this.provider = provider;
        this.maxPoolSize = Math.max(1, maxPoolSize);
    }

    /**
     * 获取可用的WebView实例
     * @return WebView实例，如果池为空则创建新的
     */
    @NonNull
    public WebView acquireWebView() {
        synchronized (lock) {
            // 查找有效的WebView
            while (!availableWebViews.isEmpty()) {
                WebView webView = availableWebViews.poll();
                if (webView != null && Boolean.TRUE.equals(webViewStates.get(webView))) {
                    Log.d(TAG, "Reusing WebView from pool");
                    monitor.onAcquire(true, availableWebViews.size(), maxPoolSize);
                    return webView;
                } else if (webView != null) {
                    // WebView无效，从状态映射中移除并销毁
                    webViewStates.remove(webView);
                    provider.destroyWebView(webView);
                    Log.d(TAG, "Destroyed invalid WebView from pool");
                }
            }

            // 创建新的WebView
            WebView newWebView = provider.createWebView();
            webViewStates.put(newWebView, true); // 标记为有效
            Log.d(TAG, "Creating new WebView");
            monitor.onAcquire(false, availableWebViews.size(), maxPoolSize);
            return newWebView;
        }
    }

    /**
     * 归还WebView实例到池中
     * @param webView 要归还的WebView实例
     */
    public void releaseWebView(@NonNull WebView webView) {
        synchronized (lock) {
            if (webView != null) {
                // 检查WebView是否仍然有效且没有被销毁
                boolean isValid = Boolean.TRUE.equals(webViewStates.get(webView)) && isWebViewAttachedToValidContext(webView);
                if (isValid && availableWebViews.size() < maxPoolSize) {
                    provider.recycleWebView(webView);
                    availableWebViews.offer(webView);
                    Log.d(TAG, "WebView returned to pool, pool size: " + availableWebViews.size());
                    monitor.onRelease(true, availableWebViews.size(), maxPoolSize);
                } else {
                    webViewStates.remove(webView); // 从状态映射中移除
                    provider.destroyWebView(webView);
                    Log.d(TAG, "WebView destroyed (invalid or pool full)");
                    monitor.onRelease(false, availableWebViews.size(), maxPoolSize);
                }
            }
        }
    }

    /**
     * 清空WebView池
     */
    public void clear() {
        synchronized (lock) {
            int cleared = 0;
            while (!availableWebViews.isEmpty()) {
                WebView webView = availableWebViews.poll();
                if (webView != null) {
                    webViewStates.remove(webView); // 从状态映射中移除
                    provider.destroyWebView(webView);
                    cleared++;
                }
            }
            webViewStates.clear(); // 清空所有状态
            Log.d(TAG, "WebView pool cleared");
            monitor.onClear(cleared);
        }
    }

    /**
     * 获取当前池中可用WebView的数量
     */
    public int getAvailableCount() {
        synchronized (lock) {
            return availableWebViews.size();
        }
    }

    /**
     * 获取池的最大容量
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * 标记WebView为无效状态
     * @param webView 要标记的WebView
     */
    public void markWebViewAsDestroyed(@NonNull WebView webView) {
        synchronized (lock) {
            webViewStates.put(webView, false);
            Log.d(TAG, "WebView marked as destroyed");
        }
    }

    /**
     * 检查WebView是否仍然连接到有效的上下文
     * @param webView 要检查的WebView
     * @return 如果WebView仍然有效则返回true
     */
    private boolean isWebViewAttachedToValidContext(@NonNull WebView webView) {
        try {
            // 检查WebView的context是否仍然有效
            if (webView.getContext() == null) {
                return false;
            }

            // 检查WebView是否仍然连接到View树
            if (webView.getParent() != null) {
                // 如果WebView仍然有父View，检查父View的context是否有效
                if (webView.getParent() instanceof android.view.ViewGroup) {
                    android.content.Context parentContext = ((android.view.ViewGroup) webView.getParent()).getContext();
                    if (parentContext == null) {
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            // 如果出现异常，说明WebView已经失效
            Log.w(TAG, "Error checking WebView context validity", e);
            return false;
        }
    }

}
