package com.core.webview.core;

import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.webview.event.WebViewEventListener;

/**
 * 默认WebView控制器实现
 */
public class DefaultWebViewController implements WebViewController {
    private final WebView webView;
    private final WebViewPool webViewPool;
    private final WebViewLifecycleManager lifecycleManager;

    private WebViewEventListener eventListener;
    private volatile boolean isDestroyed = false;

    public DefaultWebViewController(@NonNull WebView webView,
                                  @NonNull WebViewPool webViewPool,
                                  @NonNull WebViewLifecycleManager lifecycleManager) {
        this.webView = webView;
        this.webViewPool = webViewPool;
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public void loadUrl(@NonNull String url) {
        android.util.Log.d("WebViewController", "Loading URL: " + url + ", WebView valid: " + isWebViewValid());
        if (isWebViewValid()) {
            webView.loadUrl(url);
            android.util.Log.d("WebViewController", "URL loaded successfully: " + url);
        } else {
            android.util.Log.w("WebViewController", "Cannot load URL - WebView is invalid or destroyed: " + url);
        }
    }

    @Override
    public void loadHtml(@NonNull String html, @Nullable String baseUrl) {
        if (isWebViewValid()) {
            webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null);
        }
    }

    @Override
    public void reload() {
        if (isWebViewValid()) {
            webView.reload();
        }
    }

    @Override
    public void stopLoading() {
        if (isWebViewValid()) {
            webView.stopLoading();
        }
    }

    @Override
    public boolean canGoBack() {
        return isWebViewValid() && webView.canGoBack();
    }

    @Override
    public void goBack() {
        if (isWebViewValid() && webView.canGoBack()) {
            webView.goBack();
        }
    }

    @Override
    public boolean canGoForward() {
        return isWebViewValid() && webView.canGoForward();
    }

    @Override
    public void goForward() {
        if (isWebViewValid() && webView.canGoForward()) {
            webView.goForward();
        }
    }

    @Override
    @Nullable
    public String getCurrentUrl() {
        return isWebViewValid() ? webView.getUrl() : null;
    }

    @Override
    @Nullable
    public String getTitle() {
        return isWebViewValid() ? webView.getTitle() : null;
    }

    @Override
    public boolean isLoading() {
        return isWebViewValid() && webView.getProgress() < 100;
    }

    @Override
    public void evaluateJavaScript(@NonNull String script) {
        evaluateJavaScript(script, null);
    }

    @Override
    public void evaluateJavaScript(@NonNull String script, @Nullable ValueCallback<String> callback) {
        if (isWebViewValid()) {
            webView.evaluateJavascript(script, callback);
        }
    }

    @Override
    public void clearCache(boolean includeDiskFiles) {
        if (isWebViewValid()) {
            webView.clearCache(includeDiskFiles);
        }
    }

    @Override
    public void clearHistory() {
        if (isWebViewValid()) {
            webView.clearHistory();
        }
    }

    @Override
    @Nullable
    public WebView getWebView() {
        return webView;
    }

    @Override
    public void destroy() {
        if (!isDestroyed) {
            isDestroyed = true;
            if (lifecycleManager != null) {
                lifecycleManager.destroyImmediately();
            }
        }
    }

    /**
     * 检查WebView是否仍然有效
     * @return 如果WebView仍然有效则返回true
     */
    private boolean isWebViewValid() {
        return webView != null && !isDestroyed;
    }

    /**
     * 获取事件监听器
     */
    @Nullable
    public WebViewEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public void setEventListener(@Nullable WebViewEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * 获取WebView池
     */
    @NonNull
    public WebViewPool getWebViewPool() {
        return webViewPool;
    }

    /**
     * 获取生命周期管理器
     */
    @NonNull
    public WebViewLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }
}
