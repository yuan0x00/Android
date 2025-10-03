package com.core.webview.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.webview.bridge.JavaScriptBridge;
import com.core.webview.config.WebViewConfiguration;
import com.core.webview.download.DownloadManager;
import com.core.webview.event.WebViewEventListener;
import com.core.webview.monitor.AdvancedPerformanceMonitor;
import com.core.webview.monitor.PerformanceMonitor;
import com.core.webview.permission.PermissionManager;
import com.core.webview.security.AdvancedSecurityManager;
import com.core.webview.security.SSLVerifier;

/**
 * 默认WebView控制器实现
 */
public class DefaultWebViewController implements WebViewController {
    private static final String TAG = "WebViewController";
    private static final String JS_BRIDGE_NAME = "WebViewBridgeAndroid";

    private final WebView webView;
    private final WebViewPool webViewPool;
    private final WebViewLifecycleManager lifecycleManager;
    private final WebViewConfiguration configuration;

    private final PermissionManager permissionManager;
    private final DownloadManager downloadManager;
    private final JavaScriptBridge javaScriptBridge;
    private final SSLVerifier sslVerifier;
    private final AdvancedSecurityManager securityManager;
    private final ResourceManager resourceManager;
    private final PerformanceMonitor performanceMonitor;
    private final AdvancedPerformanceMonitor advancedPerformanceMonitor;

    private WebViewEventListener eventListener;
    private volatile boolean isDestroyed = false;

    public DefaultWebViewController(@NonNull WebView webView,
                                    @NonNull WebViewPool webViewPool,
                                    @NonNull WebViewLifecycleManager lifecycleManager,
                                    @NonNull WebViewConfiguration configuration) {
        this.webView = webView;
        this.webViewPool = webViewPool;
        this.lifecycleManager = lifecycleManager;
        this.configuration = configuration;

        Context context = webView.getContext();

        this.permissionManager = new PermissionManager(context)
                .setPermissionListener(new PermissionManager.PermissionListener() {
                    @Override
                    public void onPermissionRequested(@NonNull Uri origin, @NonNull String[] resources, boolean granted) {
                        notifyPermissionRequested(origin.toString(), resources, granted);
                    }

                    @Override
                    public void onPermissionRevoked(@NonNull Uri origin, @NonNull String[] resources) {
                        notifyPermissionRequested(origin.toString(), resources, false);
                    }
                });

        this.downloadManager = new DownloadManager(context)
                .setDownloadListener(new DownloadManager.DownloadListener() {
                    @Override
                    public void onDownloadStarted(@NonNull DownloadManager.DownloadRequest request) {
                        notifyDownloadRequested(request.url, request.userAgent, request.contentDisposition,
                                request.mimetype, request.contentLength);
                    }

                    @Override
                    public void onDownloadCompleted(@NonNull DownloadManager.DownloadRequest request, boolean success,
                                                    @Nullable String filePath) {
                        // 目前仅记录日志，后续可扩展事件上报
                        android.util.Log.d(TAG, "Download completed(" + success + "): " + request.url);
                    }

                    @Override
                    public void onDownloadFailed(@NonNull DownloadManager.DownloadRequest request, @NonNull String reason) {
                        android.util.Log.w(TAG, "Download failed: " + request.url + " reason=" + reason);
                    }

                    @Override
                    public void onDownloadProgress(@NonNull DownloadManager.DownloadRequest request, long bytesDownloaded, long bytesTotal) {
                        // 进度信息当前仅用于调试
                    }
                });

        this.javaScriptBridge = new JavaScriptBridge(webView)
                .setBridgeListener(new JavaScriptBridge.BridgeListener() {
                    @Override
                    public void onJavaScriptCall(@NonNull String method, @Nullable Object data) {
                        notifyJavaScriptResult("js->java:" + method + " data=" + String.valueOf(data));
                    }

                    @Override
                    public void onJavaCall(@NonNull String script) {
                        notifyJavaScriptResult("java->js:" + script);
                    }

                    @Override
                    public void onBridgeError(@NonNull String error) {
                        android.util.Log.w(TAG, "Bridge error: " + error);
                        notifyJavaScriptResult("bridge_error:" + error);
                    }
                });
        this.javaScriptBridge.registerHandler("ping", (data, callback) -> {
            String payload = data != null ? data.toString() : "";
            if (callback != null) {
                callback.onReceiveValue("{\"echo\":\"" + payload + "\"}");
            }
            notifyJavaScriptResult("ping:" + payload);
        });
        webView.addJavascriptInterface(javaScriptBridge.getJavaScriptInterface(), JS_BRIDGE_NAME);

        this.sslVerifier = new SSLVerifier()
                .setVerificationListener((host, error, result) -> {
                    notifySslError(host, result.getDescription(), result.isAllowed());
                    return result.isAllowed();
                });

        this.securityManager = new AdvancedSecurityManager()
                .setStrictDomainChecking(configuration.isStrictDomainChecking())
                .setSSLVerificationEnabled(configuration.isSafeBrowsingEnabled());
        String[] allowedDomains = configuration.getAllowedDomains();
        if (allowedDomains != null) {
            for (String domain : allowedDomains) {
                if (!TextUtils.isEmpty(domain)) {
                    securityManager.addAllowedDomain(domain.trim());
                }
            }
        }

        this.resourceManager = new ResourceManager(context);
        this.resourceManager.setResourceListener(new ResourceManager.ResourceListener() {
            @Override
            public void onMemoryWarning(float usedMB, float totalMB, float usagePercent) {
                android.util.Log.w(TAG, String.format("Memory warning %.1f/%.1f MB", usedMB, totalMB));
            }

            @Override
            public void onMemoryCritical(float usedMB, float totalMB, float usagePercent) {
                android.util.Log.e(TAG, String.format("Memory critical %.1f/%.1f MB", usedMB, totalMB));
                resourceManager.forceGC();
            }

            @Override
            public void onRecommendGC() {
                resourceManager.forceGC();
            }
        });

        this.performanceMonitor = new PerformanceMonitor();
        this.performanceMonitor.setPerformanceListener(new PerformanceMonitor.PerformanceListener() {
            @Override
            public void onPageLoadCompleted(long loadTimeMs, String url) {
                android.util.Log.d(TAG, "Page loaded in " + loadTimeMs + "ms: " + url);
            }

            @Override
            public void onSlowLoadDetected(long loadTimeMs, long thresholdMs, String url) {
                android.util.Log.w(TAG, "Slow load (" + loadTimeMs + "ms) -> " + url);
            }

            @Override
            public void onMemoryUsage(float usedMemoryMB, float maxMemoryMB, float usagePercent) {
                android.util.Log.d(TAG, String.format("Heap usage %.1f/%.1f MB", usedMemoryMB, maxMemoryMB));
            }

            @Override
            public void onLowMemoryWarning(float usedMemoryMB, float maxMemoryMB) {
                android.util.Log.w(TAG, String.format("Low heap memory %.1f/%.1f MB", usedMemoryMB, maxMemoryMB));
            }
        });

        this.advancedPerformanceMonitor = new AdvancedPerformanceMonitor();
        this.advancedPerformanceMonitor.setPerformanceListener(new AdvancedPerformanceMonitor.PerformanceListener() {
            @Override
            public void onFPSUpdate(float fps, int droppedFrames) {
                android.util.Log.d(TAG, String.format("FPS %.1f (dropped %d)", fps, droppedFrames));
            }

            @Override
            public void onMemoryUpdate(@NonNull AdvancedPerformanceMonitor.MemoryInfo info) {
                android.util.Log.v(TAG, String.format("Runtime memory %.1f/%.1f MB", info.getUsedMemoryMB(), info.getTotalMemoryMB()));
            }

            @Override
            public void onPageLoadMetrics(@NonNull AdvancedPerformanceMonitor.PageLoadMetrics metrics) {
                android.util.Log.d(TAG, metrics.toString());
            }

            @Override
            public void onPerformanceWarning(@NonNull AdvancedPerformanceMonitor.PerformanceWarning warning) {
                android.util.Log.w(TAG, "Performance warning: " + warning.message);
            }
        });

        setupWebViewClients();
        startMonitoringIfRequired();
    }

    private void setupWebViewClients() {
        webView.setWebViewClient(new EnhancedWebViewClient());
        webView.setWebChromeClient(new EnhancedWebChromeClient());
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) ->
                downloadManager.handleDownloadRequest(url, userAgent, contentDisposition, mimetype, contentLength));
        advancedPerformanceMonitor.bindWebView(webView);
    }

    private void startMonitoringIfRequired() {
        if (configuration.isEnableMemoryMonitoring()) {
            resourceManager.startMonitoring();
        }
        if (configuration.isEnablePerformanceMonitoring()) {
            performanceMonitor.enableMemoryMonitoring();
        } else {
            performanceMonitor.disableMemoryMonitoring();
        }
    }

    @Override
    public void loadUrl(@NonNull String url) {
        android.util.Log.d(TAG, "Loading URL: " + url + ", valid=" + isWebViewValid());
        if (isWebViewValid()) {
            webView.loadUrl(url);
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
            javaScriptBridge.cleanup();
            resourceManager.stopMonitoring();
            performanceMonitor.stop();
            advancedPerformanceMonitor.unbindWebView();
            advancedPerformanceMonitor.setPerformanceListener(null);
            if (lifecycleManager != null) {
                lifecycleManager.destroyImmediately();
            }
            eventListener = null;
        }
    }

    private boolean isWebViewValid() {
        return webView != null && !isDestroyed;
    }

    @Nullable
    public WebViewEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public void setEventListener(@Nullable WebViewEventListener listener) {
        this.eventListener = listener;
    }

    @NonNull
    public WebViewPool getWebViewPool() {
        return webViewPool;
    }

    @NonNull
    public WebViewLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    private void notifyPageStarted(@NonNull String url) {
        performanceMonitor.startPageLoad(url);
        advancedPerformanceMonitor.startPageLoad(url);
        if (eventListener != null) {
            eventListener.onPageStarted(url);
        }
    }

    private void notifyPageFinished(@NonNull String url, boolean success) {
        performanceMonitor.endPageLoad(url);
        advancedPerformanceMonitor.endPageLoad(url, success);
        if (eventListener != null && success) {
            eventListener.onPageFinished(url);
        }
    }

    private void notifyProgress(int progress) {
        if (eventListener != null) {
            eventListener.onProgressChanged(progress);
        }
    }

    private void notifyTitle(@Nullable String title) {
        if (eventListener != null && title != null) {
            eventListener.onTitleChanged(title);
        }
    }

    private void notifyUrlBlocked(@NonNull String url, @NonNull String reason) {
        if (eventListener != null) {
            eventListener.onUrlBlocked(url, reason);
        }
    }

    private void notifyDownloadRequested(@NonNull String url, @Nullable String userAgent,
                                         @Nullable String contentDisposition, @Nullable String mimeType,
                                         long contentLength) {
        if (eventListener != null) {
            eventListener.onDownloadRequested(url, userAgent, contentDisposition, mimeType, contentLength);
        }
    }

    private void notifyPermissionRequested(@NonNull String origin, @NonNull String[] resources, boolean granted) {
        if (eventListener != null) {
            eventListener.onPermissionRequested(origin, resources);
        }
    }

    private void notifySslError(@NonNull String url, @NonNull String error, boolean allowed) {
        if (eventListener != null) {
            eventListener.onSslError(url, error);
        }
    }

    private void notifyJavaScriptResult(@NonNull String message) {
        if (eventListener != null) {
            eventListener.onJavaScriptResult(message);
        }
    }

    private class EnhancedWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            notifyPageStarted(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            notifyPageFinished(url, true);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            String failingUrl = request != null ? request.getUrl().toString() : view.getUrl();
            String description = error != null ? error.getDescription().toString() : "Unknown error";
            if (eventListener != null) {
                eventListener.onReceivedError(error != null ? error.getErrorCode() : -1, description, failingUrl);
            }
            notifyPageFinished(failingUrl != null ? failingUrl : "", false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AdvancedSecurityManager.SecurityCheckResult result = securityManager.checkResourceRequest(request);
                if (!result.isAllowed()) {
                    notifyUrlBlocked(request.getUrl().toString(), result.getDescription());
                    return true;
                }
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            AdvancedSecurityManager.SecurityCheckResult result = securityManager.checkUrl(url);
            if (!result.isAllowed()) {
                notifyUrlBlocked(url, result.getDescription());
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            WebResourceResponse response = super.shouldInterceptRequest(view, request);
            return securityManager.filterResponse(response, request != null ? request.getUrl().toString() : null);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse response = super.shouldInterceptRequest(view, url);
            return securityManager.filterResponse(response, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            String host = error != null ? error.getUrl() : view.getUrl();
            boolean allowed = host != null && error != null && sslVerifier.handleSSLError(host, error);
            if (allowed) {
                handler.proceed();
            } else {
                handler.cancel();
            }
        }
    }

    private class EnhancedWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            notifyProgress(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            notifyTitle(title);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            notifyJavaScriptResult("console:" + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            String origin = request.getOrigin() != null ? request.getOrigin().toString() : "";
            String[] resources = request.getResources();
            boolean allow = eventListener != null && eventListener.onPermissionRequested(origin, resources);
            if (allow) {
                request.grant(resources);
                notifyPermissionRequested(origin, resources, true);
            } else {
                permissionManager.handlePermissionRequest(request);
            }
        }
    }
}
