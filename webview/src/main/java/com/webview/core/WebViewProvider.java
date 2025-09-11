package com.webview.core;

import android.webkit.WebView;

import androidx.annotation.NonNull;

/**
 * WebView提供者接口
 * 定义WebView实例的创建和管理规范
 */
public interface WebViewProvider {

    /**
     * 创建WebView实例
     * @return 配置完成的WebView实例
     */
    @NonNull
    WebView createWebView();

    /**
     * 销毁WebView实例
     * @param webView 要销毁的WebView实例
     */
    void destroyWebView(@NonNull WebView webView);

    /**
     * 回收WebView实例以供复用
     * @param webView 要回收的WebView实例
     */
    void recycleWebView(@NonNull WebView webView);
}
