package com.core.webview.core;

import android.content.Context;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.core.webview.config.WebViewConfiguration;

/**
 * WebView工厂类
 * 负责创建和管理WebView实例及其相关组件
 */
public class WebViewFactory {
    private static final int DEFAULT_POOL_SIZE = 3;

    private static volatile WebViewFactory instance;
    private final Context applicationContext;
    private final WebViewPool webViewPool;
    private final WebViewProvider webViewProvider;

    private WebViewFactory(@NonNull Context context) {
        this.applicationContext = context.getApplicationContext();
        this.webViewProvider = new DefaultWebViewProvider(applicationContext);
        this.webViewPool = new WebViewPool(webViewProvider, DEFAULT_POOL_SIZE);
    }

    /**
     * 获取单例实例
     */
    @NonNull
    public static WebViewFactory getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (WebViewFactory.class) {
                if (instance == null) {
                    instance = new WebViewFactory(context);
                }
            }
        }
        return instance;
    }

    /**
     * 预热WebView池：提前创建指定数量的WebView以降低首次创建开销。
     * 必须在主线程调用。
     * @param count 预热数量
     */
    public void prewarm(int count) {
        if (count <= 0) return;
        int toCreate = Math.min(count, Math.max(0, webViewPool.getMaxPoolSize() - webViewPool.getAvailableCount()));
        for (int i = 0; i < toCreate; i++) {
            // 提前创建并放回池中
            WebView webView = webViewPool.acquireWebView();
            webViewPool.releaseWebView(webView);
        }
    }

    /**
     * 创建WebView控制器
     * @param configuration WebView配置
     * @param lifecycle 生命周期对象
     * @return WebView控制器实例
     */
    @NonNull
    public WebViewController createController(@NonNull WebViewConfiguration configuration,
                                           @NonNull Lifecycle lifecycle) {
        WebView webView = webViewPool.acquireWebView();

        // 应用配置
        configuration.applyTo(webView.getSettings());

        // 创建生命周期管理器
        WebViewLifecycleManager lifecycleManager = new WebViewLifecycleManager(webView, webViewPool, lifecycle);
        lifecycle.addObserver(lifecycleManager);

        // 创建控制器
        return new DefaultWebViewController(webView, webViewPool, lifecycleManager, configuration);
    }

    /**
     * 创建WebView控制器（使用默认配置）
     * @param lifecycle 生命周期对象
     * @return WebView控制器实例
     */
    @NonNull
    public WebViewController createController(@NonNull Lifecycle lifecycle) {
        return createController(new com.core.webview.config.DefaultWebViewConfig.Builder().build(), lifecycle);
    }

    /**
     * 获取WebView池状态
     */
    public int getAvailableWebViewCount() {
        return webViewPool.getAvailableCount();
    }

    /**
     * 获取WebView池最大容量
     */
    public int getMaxPoolSize() {
        return webViewPool.getMaxPoolSize();
    }

    /**
     * 清空WebView池
     */
    public void clearPool() {
        webViewPool.clear();
    }

    /**
     * 默认WebView提供者实现
     */
    private static class DefaultWebViewProvider implements WebViewProvider {
        private final Context context;

        DefaultWebViewProvider(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public WebView createWebView() {
            return new WebView(context);
        }

        @Override
        public void destroyWebView(@NonNull WebView webView) {
            webView.destroy();
        }

        @Override
        public void recycleWebView(@NonNull WebView webView) {
            // 清理WebView状态以供复用
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.loadUrl("about:blank");
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
        }
    }
}
