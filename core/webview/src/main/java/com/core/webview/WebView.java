package com.core.webview;

import android.content.Context;
import android.webkit.ValueCallback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.core.webview.config.DefaultWebViewConfig;
import com.core.webview.config.WebViewConfiguration;
import com.core.webview.core.WebViewController;
import com.core.webview.core.WebViewFactory;
import com.core.webview.event.WebViewEventListener;

/**
 * WebView - 现代化Android WebView封装库
 *
 * 主要特性：
 * - 池化WebView复用，减少创建开销
 * - 完整的生命周期管理
 * - 流式API设计
 * - 现代化事件监听器
 * - 强大的配置系统
 * - 内置性能监控
 * - 增强的安全性
 */
public class WebView {
    private final WebViewController controller;
    private final WebViewConfiguration config;

    private WebView(@NonNull WebViewController controller,
                    @NonNull WebViewConfiguration config) {
        this.controller = controller;
        this.config = config;
    }

    /**
     * 创建WebView实例
     * @param context Android上下文
     * @param lifecycle 生命周期对象（通常是Activity或Fragment）
     * @return WebView.Builder实例
     */
    @NonNull
    public static Builder with(@NonNull Context context, @NonNull Lifecycle lifecycle) {
        return new Builder(context, lifecycle);
    }

    /**
     * 加载URL
     * @param url 要加载的URL
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView loadUrl(@NonNull String url) {
        controller.loadUrl(url);
        return this;
    }

    /**
     * 加载HTML内容
     * @param html HTML内容
     * @param baseUrl 基础URL
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView loadHtml(@NonNull String html, @Nullable String baseUrl) {
        controller.loadHtml(html, baseUrl);
        return this;
    }

    /**
     * 重新加载页面
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView reload() {
        controller.reload();
        return this;
    }

    /**
     * 停止加载
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView stopLoading() {
        controller.stopLoading();
        return this;
    }

    /**
     * 后退
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView goBack() {
        controller.goBack();
        return this;
    }

    /**
     * 前进
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView goForward() {
        controller.goForward();
        return this;
    }

    /**
     * 执行JavaScript代码
     * @param script JavaScript代码
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView evaluateJavaScript(@NonNull String script) {
        controller.evaluateJavaScript(script);
        return this;
    }

    /**
     * 执行JavaScript代码并获取结果
     * @param script JavaScript代码
     * @param callback 结果回调
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView evaluateJavaScript(@NonNull String script,
                                         @Nullable ValueCallback<String> callback) {
        controller.evaluateJavaScript(script, callback);
        return this;
    }

    /**
     * 清空缓存
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView clearCache() {
        controller.clearCache(true);
        return this;
    }

    /**
     * 清空历史记录
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView clearHistory() {
        controller.clearHistory();
        return this;
    }

    /**
     * 设置事件监听器
     * @param listener 事件监听器
     * @return 当前实例，支持链式调用
     */
    @NonNull
    public WebView setEventListener(@Nullable WebViewEventListener listener) {
        controller.setEventListener(listener);
        return this;
    }

    // Getters
    public boolean canGoBack() { return controller.canGoBack(); }
    public boolean canGoForward() { return controller.canGoForward(); }
    @Nullable public String getCurrentUrl() { return controller.getCurrentUrl(); }
    @Nullable public String getTitle() { return controller.getTitle(); }
    public boolean isLoading() { return controller.isLoading(); }
    @Nullable public android.webkit.WebView getWebView() { return controller.getWebView(); }

    /**
     * 销毁WebView实例
     */
    public void destroy() {
        controller.destroy();
    }

    /**
     * Builder模式构建WebView
     */
    public static class Builder {
        private final Context context;
        private final Lifecycle lifecycle;
        private final DefaultWebViewConfig.Builder configBuilder = new DefaultWebViewConfig.Builder();
        private WebViewConfiguration explicitConfig;

        private Builder(@NonNull Context context, @NonNull Lifecycle lifecycle) {
            this.context = context;
            this.lifecycle = lifecycle;
        }

        /**
         * 配置JavaScript支持
         * @param enabled 是否启用
         * @return Builder实例
         */
        @NonNull
        public Builder javaScriptEnabled(boolean enabled) {
            configBuilder.setJavaScriptEnabled(enabled);
            return this;
        }

        /**
         * 配置缓存模式
         * @param cacheMode 缓存模式
         * @return Builder实例
         */
        @NonNull
        public Builder cacheMode(int cacheMode) {
            configBuilder.setCacheMode(cacheMode);
            return this;
        }

        /**
         * 配置缓存大小
         * @param size 缓存大小（字节）
         * @return Builder实例
         */
        @NonNull
        public Builder cacheSize(long size) {
            configBuilder.setCacheSize(size);
            return this;
        }

        /**
         * 配置允许的域名
         * @param domains 域名数组
         * @return Builder实例
         */
        @NonNull
        public Builder allowedDomains(String[] domains) {
            configBuilder.setAllowedDomains(domains);
            return this;
        }

        /**
         * 配置严格域名检查
         * @param strict 是否严格检查
         * @return Builder实例
         */
        @NonNull
        public Builder strictDomainChecking(boolean strict) {
            configBuilder.setStrictDomainChecking(strict);
            return this;
        }

        /**
         * 配置缩放支持
         * @param enabled 是否启用
         * @return Builder实例
         */
        @NonNull
        public Builder supportZoom(boolean enabled) {
            configBuilder.setSupportZoom(enabled);
            return this;
        }

        /**
         * 配置硬件加速
         * @param enabled 是否启用
         * @return Builder实例
         */
        @NonNull
        public Builder hardwareAcceleration(boolean enabled) {
            configBuilder.setHardwareAcceleration(enabled);
            return this;
        }

        /**
         * 配置性能监控
         * @param enabled 是否启用
         * @return Builder实例
         */
        @NonNull
        public Builder performanceMonitoring(boolean enabled) {
            configBuilder.setEnablePerformanceMonitoring(enabled);
            return this;
        }

        /**
         * 配置内存监控
         * @param enabled 是否启用
         * @return Builder实例
         */
        @NonNull
        public Builder memoryMonitoring(boolean enabled) {
            configBuilder.setEnableMemoryMonitoring(enabled);
            return this;
        }

        /**
         * 配置慢渲染检测阈值
         * @param thresholdMs 阈值（毫秒）
         * @return Builder实例
         */
        @NonNull
        public Builder slowRenderingThreshold(long thresholdMs) {
            configBuilder.setSlowRenderingThreshold(thresholdMs);
            return this;
        }

        /**
         * 配置安全浏览
         * @param enabled 是否启用
         * @return Builder实例
         */
        @NonNull
        public Builder safeBrowsingEnabled(boolean enabled) {
            configBuilder.setSafeBrowsingEnabled(enabled);
            return this;
        }

        /**
         * 使用自定义配置
         * @param config 自定义配置
         * @return Builder实例
         */
        @NonNull
        public Builder config(@NonNull WebViewConfiguration config) {
            this.explicitConfig = config;
            return this;
        }

        /**
         * 构建WebView实例
         * @return WebView实例
         */
        @NonNull
        public WebView build() {
            WebViewConfiguration config = explicitConfig != null ? explicitConfig : configBuilder.build();
            WebViewFactory factory = WebViewFactory.getInstance(context);
            WebViewController controller = factory.createController(config, lifecycle);
            return new WebView(controller, config);
        }
    }
}
