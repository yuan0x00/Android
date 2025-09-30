package com.core.webview.core;

import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.webview.event.WebViewEventListener;

/**
 * WebView控制器接口
 * 定义WebView的核心操作方法
 */
public interface WebViewController {

    /**
     * 加载URL
     * @param url 要加载的URL
     */
    void loadUrl(@NonNull String url);

    /**
     * 加载HTML内容
     * @param html HTML内容
     * @param baseUrl 基础URL
     */
    void loadHtml(@NonNull String html, @Nullable String baseUrl);

    /**
     * 重新加载当前页面
     */
    void reload();

    /**
     * 停止加载
     */
    void stopLoading();

    /**
     * 是否可以后退
     */
    boolean canGoBack();

    /**
     * 后退
     */
    void goBack();

    /**
     * 是否可以前进
     */
    boolean canGoForward();

    /**
     * 前进
     */
    void goForward();

    /**
     * 获取当前URL
     */
    @Nullable
    String getCurrentUrl();

    /**
     * 获取页面标题
     */
    @Nullable
    String getTitle();

    /**
     * 是否正在加载
     */
    boolean isLoading();

    /**
     * 执行JavaScript代码
     * @param script JavaScript代码
     */
    void evaluateJavaScript(@NonNull String script);

    /**
     * 执行JavaScript代码并获取结果
     * @param script JavaScript代码
     * @param callback 结果回调
     */
    void evaluateJavaScript(@NonNull String script, @Nullable ValueCallback<String> callback);

    /**
     * 清空缓存
     * @param includeDiskFiles 是否包含磁盘文件
     */
    void clearCache(boolean includeDiskFiles);

    /**
     * 清空历史记录
     */
    void clearHistory();

    /**
     * 设置事件监听器
     * @param listener 事件监听器
     */
    void setEventListener(@Nullable WebViewEventListener listener);

    /**
     * 获取WebView实例
     * @return WebView实例，可能为null
     */
    @Nullable
    WebView getWebView();

    /**
     * 销毁控制器及其关联的WebView
     */
    void destroy();
}
