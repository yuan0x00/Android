package com.rapid.android.core.webview.core;

import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

/**
 * WebView生命周期管理器
 * 负责管理WebView的生命周期，确保在适当的时机调用WebView的生命周期方法
 */
public class WebViewLifecycleManager implements DefaultLifecycleObserver {
    private static final String TAG = "WebViewLifecycle";

    private final WeakReference<WebView> webViewRef;
    private final WebViewPool webViewPool;
    private final WeakReference<Lifecycle> lifecycleRef;

    public WebViewLifecycleManager(@NonNull WebView webView,
                                   @NonNull WebViewPool webViewPool,
                                   @NonNull Lifecycle lifecycle) {
        this.webViewRef = new WeakReference<>(webView);
        this.webViewPool = webViewPool;
        this.lifecycleRef = new WeakReference<>(lifecycle);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        WebView webView = webViewRef.get();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
            Log.d(TAG, "WebView resumed");
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        WebView webView = webViewRef.get();
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
            Log.d(TAG, "WebView paused");
        }
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        WebView webView = webViewRef.get();
        if (webView != null) {
            // 清理WebView
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(false);
            webView.loadUrl("about:blank");
            webView.pauseTimers();

            // 归还到池中而不是直接销毁
            webViewPool.releaseWebView(webView);
            Log.d(TAG, "WebView recycled to pool");
        }
        detachFromLifecycle();
    }

    /**
     * 立即销毁WebView
     */
    public void destroyImmediately() {
        WebView webView = webViewRef.get();
        if (webView != null) {
            // 从父容器中移除WebView，防止内存泄露
            android.view.ViewGroup parent = (android.view.ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
                Log.d(TAG, "WebView removed from parent before destruction");
            }

            // 标记WebView为无效状态
            markWebViewAsDestroyed(webView);
            webView.destroy();
            Log.d(TAG, "WebView destroyed immediately");
            // 清除引用，防止被回收到池中
            webViewRef.clear();
        }
        detachFromLifecycle();
    }

    /**
     * 标记WebView为已销毁状态
     * @param webView 要标记的WebView
     */
    private void markWebViewAsDestroyed(@NonNull WebView webView) {
        // 通知WebViewPool更新WebView状态
        webViewPool.markWebViewAsDestroyed(webView);
        Log.d(TAG, "WebView marked as destroyed in pool");
    }

    /**
     * 检查WebView是否仍然有效
     */
    public boolean isWebViewValid() {
        WebView webView = webViewRef.get();
        if (webView == null) {
            return false;
        }

        try {
            // 使用更安全的方法检查WebView状态
            // 检查WebView的基本属性而不是调用可能触发底层检查的方法
            return webView.getParent() != null || webView.getContext() != null;
        } catch (Exception e) {
            // WebView已经被销毁
            return false;
        }
    }

    private void detachFromLifecycle() {
        Lifecycle lifecycle = lifecycleRef != null ? lifecycleRef.get() : null;
        if (lifecycle != null) {
            lifecycle.removeObserver(this);
        }
    }
}
