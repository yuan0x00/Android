package com.webview.config;

import android.webkit.WebSettings;

import androidx.annotation.NonNull;

/**
 * WebView配置接口
 * 定义WebView的所有配置选项
 */
public interface WebViewConfiguration {

    // 基础配置
    boolean isJavaScriptEnabled();
    boolean isDomStorageEnabled();
    boolean isDatabaseEnabled();
    boolean isUseWideViewPort();
    boolean isLoadWithOverviewMode();

    // 安全配置
    boolean isAllowFileAccess();
    boolean isAllowContentAccess();
    boolean isAllowFileAccessFromFileURLs();
    boolean isAllowUniversalAccessFromFileURLs();
    boolean isSavePassword();

    // 缓存配置
    int getCacheMode();
    long getCacheSize();

    // 显示配置
    boolean isSupportZoom();
    boolean isBuiltInZoomControls();
    boolean isDisplayZoomControls();
    boolean isHardwareAcceleration();

    // 安全浏览
    boolean isSafeBrowsingEnabled();

    // 域名限制
    String[] getAllowedDomains();
    boolean isStrictDomainChecking();

    // 性能监控
    boolean isEnablePerformanceMonitoring();
    boolean isEnableMemoryMonitoring();
    long getSlowRenderingThreshold();

    /**
     * 应用配置到WebSettings
     * @param webSettings WebSettings实例
     */
    void applyTo(@NonNull WebSettings webSettings);

    /**
     * 验证配置的合法性
     * @throws IllegalArgumentException 如果配置无效
     */
    void validate() throws IllegalArgumentException;
}
