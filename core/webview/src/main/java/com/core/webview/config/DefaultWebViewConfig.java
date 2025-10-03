package com.core.webview.config;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

/**
 * 默认WebView配置实现
 */
public class DefaultWebViewConfig implements WebViewConfiguration, Parcelable {

    public static final Creator<DefaultWebViewConfig> CREATOR = new Creator<DefaultWebViewConfig>() {
        @Override
        public DefaultWebViewConfig createFromParcel(Parcel in) {
            return new DefaultWebViewConfig(in);
        }

        @Override
        public DefaultWebViewConfig[] newArray(int size) {
            return new DefaultWebViewConfig[size];
        }
    };
    // 基础配置
    private boolean javaScriptEnabled = true;
    private boolean domStorageEnabled = true;
    private boolean databaseEnabled = true;
    private boolean useWideViewPort = true;
    private boolean loadWithOverviewMode = true;
    // 安全配置
    private boolean allowFileAccess = false;
    private boolean allowContentAccess = false;
    private boolean allowFileAccessFromFileURLs = false;
    private boolean allowUniversalAccessFromFileURLs = false;
    private boolean savePassword = false;
    // 缓存配置
    private int cacheMode = WebSettings.LOAD_DEFAULT;
    private long cacheSize = 50 * 1024 * 1024; // 50MB
    // 显示配置
    private boolean supportZoom = true;
    private boolean builtInZoomControls = true;
    private boolean displayZoomControls = false;
    private boolean hardwareAcceleration = true;
    // 安全浏览
    private boolean safeBrowsingEnabled = true;
    // 域名限制
    private String[] allowedDomains = new String[0];
    private boolean strictDomainChecking = false;
    // 性能监控
    private boolean enablePerformanceMonitoring = true;
    private boolean enableMemoryMonitoring = true;
    private long slowRenderingThreshold = 500; // 500ms

    // Default constructor
    public DefaultWebViewConfig() {
        // Initialize with default values
    }
    // Constructor for Parcelable
    protected DefaultWebViewConfig(Parcel in) {
        javaScriptEnabled = in.readByte() != 0;
        domStorageEnabled = in.readByte() != 0;
        databaseEnabled = in.readByte() != 0;
        useWideViewPort = in.readByte() != 0;
        loadWithOverviewMode = in.readByte() != 0;
        allowFileAccess = in.readByte() != 0;
        allowContentAccess = in.readByte() != 0;
        allowFileAccessFromFileURLs = in.readByte() != 0;
        allowUniversalAccessFromFileURLs = in.readByte() != 0;
        savePassword = in.readByte() != 0;
        cacheMode = in.readInt();
        cacheSize = in.readLong();
        supportZoom = in.readByte() != 0;
        builtInZoomControls = in.readByte() != 0;
        displayZoomControls = in.readByte() != 0;
        hardwareAcceleration = in.readByte() != 0;
        safeBrowsingEnabled = in.readByte() != 0;
        allowedDomains = in.createStringArray();
        strictDomainChecking = in.readByte() != 0;
        enablePerformanceMonitoring = in.readByte() != 0;
        enableMemoryMonitoring = in.readByte() != 0;
        slowRenderingThreshold = in.readLong();
    }

    // Getters
    @Override
    public boolean isJavaScriptEnabled() { return javaScriptEnabled; }

    @Override
    public boolean isDomStorageEnabled() { return domStorageEnabled; }

    @Override
    public boolean isDatabaseEnabled() { return databaseEnabled; }

    @Override
    public boolean isUseWideViewPort() { return useWideViewPort; }

    @Override
    public boolean isLoadWithOverviewMode() { return loadWithOverviewMode; }

    @Override
    public boolean isAllowFileAccess() { return allowFileAccess; }

    @Override
    public boolean isAllowContentAccess() { return allowContentAccess; }

    @Override
    public boolean isAllowFileAccessFromFileURLs() { return allowFileAccessFromFileURLs; }

    @Override
    public boolean isAllowUniversalAccessFromFileURLs() { return allowUniversalAccessFromFileURLs; }

    @Override
    public boolean isSavePassword() { return savePassword; }

    @Override
    public int getCacheMode() { return cacheMode; }

    @Override
    public long getCacheSize() { return cacheSize; }

    @Override
    public boolean isSupportZoom() { return supportZoom; }

    @Override
    public boolean isBuiltInZoomControls() { return builtInZoomControls; }

    @Override
    public boolean isDisplayZoomControls() { return displayZoomControls; }

    @Override
    public boolean isHardwareAcceleration() { return hardwareAcceleration; }

    @Override
    public boolean isSafeBrowsingEnabled() { return safeBrowsingEnabled; }

    @Override
    public String[] getAllowedDomains() { return allowedDomains.clone(); }

    @Override
    public boolean isStrictDomainChecking() { return strictDomainChecking; }

    @Override
    public boolean isEnablePerformanceMonitoring() { return enablePerformanceMonitoring; }

    @Override
    public boolean isEnableMemoryMonitoring() { return enableMemoryMonitoring; }

    @Override
    public long getSlowRenderingThreshold() { return slowRenderingThreshold; }

    @Override
    public void applyTo(@NonNull WebSettings webSettings) {
        // 基础配置
        webSettings.setJavaScriptEnabled(javaScriptEnabled);
        webSettings.setDomStorageEnabled(domStorageEnabled);
        webSettings.setDatabaseEnabled(databaseEnabled);
        webSettings.setUseWideViewPort(useWideViewPort);
        webSettings.setLoadWithOverviewMode(loadWithOverviewMode);

        // 安全配置
        webSettings.setAllowFileAccess(allowFileAccess);
        webSettings.setAllowContentAccess(allowContentAccess);
        webSettings.setAllowFileAccessFromFileURLs(allowFileAccessFromFileURLs);
        webSettings.setAllowUniversalAccessFromFileURLs(allowUniversalAccessFromFileURLs);
        webSettings.setSavePassword(savePassword);

        // 缓存配置
        webSettings.setCacheMode(cacheMode);
        // Note: setAppCacheMaxSize is deprecated in API 18, but keeping for backward compatibility
        try {
            webSettings.getClass().getMethod("setAppCacheMaxSize", long.class).invoke(webSettings, cacheSize);
        } catch (Exception e) {
            // Method not available, ignore
        }

        // 显示配置
        webSettings.setSupportZoom(supportZoom);
        webSettings.setBuiltInZoomControls(builtInZoomControls);
        webSettings.setDisplayZoomControls(displayZoomControls);

        // 安全浏览
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(safeBrowsingEnabled);
        }
    }

    @Override
    public void validate() throws IllegalArgumentException {
        if (cacheSize < 0) {
            throw new IllegalArgumentException("Cache size cannot be negative");
        }
        if (slowRenderingThreshold < 0) {
            throw new IllegalArgumentException("Slow rendering threshold cannot be negative");
        }
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByte((byte) (javaScriptEnabled ? 1 : 0));
        dest.writeByte((byte) (domStorageEnabled ? 1 : 0));
        dest.writeByte((byte) (databaseEnabled ? 1 : 0));
        dest.writeByte((byte) (useWideViewPort ? 1 : 0));
        dest.writeByte((byte) (loadWithOverviewMode ? 1 : 0));
        dest.writeByte((byte) (allowFileAccess ? 1 : 0));
        dest.writeByte((byte) (allowContentAccess ? 1 : 0));
        dest.writeByte((byte) (allowFileAccessFromFileURLs ? 1 : 0));
        dest.writeByte((byte) (allowUniversalAccessFromFileURLs ? 1 : 0));
        dest.writeByte((byte) (savePassword ? 1 : 0));
        dest.writeInt(cacheMode);
        dest.writeLong(cacheSize);
        dest.writeByte((byte) (supportZoom ? 1 : 0));
        dest.writeByte((byte) (builtInZoomControls ? 1 : 0));
        dest.writeByte((byte) (displayZoomControls ? 1 : 0));
        dest.writeByte((byte) (hardwareAcceleration ? 1 : 0));
        dest.writeByte((byte) (safeBrowsingEnabled ? 1 : 0));
        dest.writeStringArray(allowedDomains);
        dest.writeByte((byte) (strictDomainChecking ? 1 : 0));
        dest.writeByte((byte) (enablePerformanceMonitoring ? 1 : 0));
        dest.writeByte((byte) (enableMemoryMonitoring ? 1 : 0));
        dest.writeLong(slowRenderingThreshold);
    }

    /**
     * Builder模式构建配置
     */
    public static class Builder {
        private final DefaultWebViewConfig config;

        public Builder() {
            this.config = new DefaultWebViewConfig();
            this.config.javaScriptEnabled = true;
            this.config.domStorageEnabled = true;
            this.config.databaseEnabled = true;
            this.config.useWideViewPort = true;
            this.config.loadWithOverviewMode = true;
            this.config.allowFileAccess = false;
            this.config.allowContentAccess = false;
            this.config.allowFileAccessFromFileURLs = false;
            this.config.allowUniversalAccessFromFileURLs = false;
            this.config.savePassword = false;
            this.config.cacheMode = WebSettings.LOAD_DEFAULT;
            this.config.cacheSize = 50 * 1024 * 1024;
            this.config.supportZoom = true;
            this.config.builtInZoomControls = true;
            this.config.displayZoomControls = false;
            this.config.hardwareAcceleration = true;
            this.config.safeBrowsingEnabled = true;
            this.config.allowedDomains = new String[0];
            this.config.strictDomainChecking = false;
            this.config.enablePerformanceMonitoring = false;
            this.config.enableMemoryMonitoring = true;
            this.config.slowRenderingThreshold = 500;
        }

        public Builder setJavaScriptEnabled(boolean enabled) {
            config.javaScriptEnabled = enabled;
            return this;
        }

        public Builder setDomStorageEnabled(boolean enabled) {
            config.domStorageEnabled = enabled;
            return this;
        }

        public Builder setDatabaseEnabled(boolean enabled) {
            config.databaseEnabled = enabled;
            return this;
        }

        public Builder setUseWideViewPort(boolean enabled) {
            config.useWideViewPort = enabled;
            return this;
        }

        public Builder setLoadWithOverviewMode(boolean enabled) {
            config.loadWithOverviewMode = enabled;
            return this;
        }

        public Builder setAllowFileAccess(boolean allow) {
            config.allowFileAccess = allow;
            return this;
        }

        public Builder setAllowContentAccess(boolean allow) {
            config.allowContentAccess = allow;
            return this;
        }

        public Builder setAllowFileAccessFromFileURLs(boolean allow) {
            config.allowFileAccessFromFileURLs = allow;
            return this;
        }

        public Builder setAllowUniversalAccessFromFileURLs(boolean allow) {
            config.allowUniversalAccessFromFileURLs = allow;
            return this;
        }

        public Builder setSavePassword(boolean save) {
            config.savePassword = save;
            return this;
        }

        public Builder setCacheMode(int mode) {
            config.cacheMode = mode;
            return this;
        }

        public Builder setCacheSize(long size) {
            config.cacheSize = size;
            return this;
        }

        public Builder setSupportZoom(boolean support) {
            config.supportZoom = support;
            return this;
        }

        public Builder setBuiltInZoomControls(boolean enabled) {
            config.builtInZoomControls = enabled;
            return this;
        }

        public Builder setDisplayZoomControls(boolean display) {
            config.displayZoomControls = display;
            return this;
        }

        public Builder setHardwareAcceleration(boolean enabled) {
            config.hardwareAcceleration = enabled;
            return this;
        }

        public Builder setSafeBrowsingEnabled(boolean enabled) {
            config.safeBrowsingEnabled = enabled;
            return this;
        }

        public Builder setAllowedDomains(String[] domains) {
            config.allowedDomains = domains != null ? domains.clone() : new String[0];
            return this;
        }

        public Builder setStrictDomainChecking(boolean strict) {
            config.strictDomainChecking = strict;
            return this;
        }

        public Builder setEnablePerformanceMonitoring(boolean enabled) {
            config.enablePerformanceMonitoring = enabled;
            return this;
        }

        public Builder setEnableMemoryMonitoring(boolean enabled) {
            config.enableMemoryMonitoring = enabled;
            return this;
        }

        public Builder setSlowRenderingThreshold(long threshold) {
            config.slowRenderingThreshold = threshold;
            return this;
        }

        public DefaultWebViewConfig build() {
            config.validate();
            return config;
        }
    }
}
