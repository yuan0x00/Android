package com.core.net.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.HttpUrl;

/**
 * Retrofit 配置项，支持业务侧定制基础 URL、请求头、Cookie 管理等能力。
 */
public final class CoreRetrofitConfig {

    private final String baseUrl;
    private final long connectTimeoutSeconds;
    private final long readTimeoutSeconds;
    private final long writeTimeoutSeconds;
    private final boolean enableLogging;
    private final boolean allowInsecureSsl;
    private final HeaderProvider headerProvider;
    private final CookieStore cookieStore;
    private final CrashReportEndpointProvider crashReportEndpointProvider;
    private final AuthFailureListener authFailureListener;
    private CoreRetrofitConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.connectTimeoutSeconds = builder.connectTimeoutSeconds;
        this.readTimeoutSeconds = builder.readTimeoutSeconds;
        this.writeTimeoutSeconds = builder.writeTimeoutSeconds;
        this.enableLogging = builder.enableLogging;
        this.allowInsecureSsl = builder.allowInsecureSsl;
        this.headerProvider = builder.headerProvider;
        this.cookieStore = builder.cookieStore;
        this.crashReportEndpointProvider = builder.crashReportEndpointProvider;
        this.authFailureListener = builder.authFailureListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CoreRetrofitConfig defaultConfig() {
        return builder().build();
    }

    @NonNull
    public String getBaseUrl() {
        return baseUrl;
    }

    public long getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public long getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public long getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public boolean isLoggingEnabled() {
        return enableLogging;
    }

    public boolean isAllowInsecureSsl() {
        return allowInsecureSsl;
    }

    @NonNull
    public HeaderProvider getHeaderProvider() {
        return headerProvider;
    }

    @NonNull
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Nullable
    public String getCrashReportEndpoint() {
        return crashReportEndpointProvider.provideCrashReportEndpoint();
    }

    @NonNull
    public AuthFailureListener getAuthFailureListener() {
        return authFailureListener;
    }

    public interface HeaderProvider {
        @NonNull
        Map<String, String> provideHeaders();
    }

    public interface CookieStore {
        void saveFromResponse(@NonNull HttpUrl url, @NonNull List<String> cookies);

        @NonNull
        List<String> loadForRequest(@NonNull HttpUrl url);
    }

    public interface CrashReportEndpointProvider {
        @Nullable
        String provideCrashReportEndpoint();
    }

    public interface AuthFailureListener {
        void onUnauthorized();
    }

    public static final class Builder {
        private String baseUrl = "https://www.wanandroid.com/";
        private long connectTimeoutSeconds = 10L;
        private long readTimeoutSeconds = 10L;
        private long writeTimeoutSeconds = 10L;
        private boolean enableLogging = false;
        private boolean allowInsecureSsl = false;
        private HeaderProvider headerProvider = Collections::emptyMap;
        private CookieStore cookieStore = new InMemoryCookieStore();
        private CrashReportEndpointProvider crashReportEndpointProvider = () -> null;
        private AuthFailureListener authFailureListener = () -> {};

        private Builder() {
        }

        public Builder baseUrl(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeoutSeconds(long connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
            return this;
        }

        public Builder readTimeoutSeconds(long readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
            return this;
        }

        public Builder writeTimeoutSeconds(long writeTimeoutSeconds) {
            this.writeTimeoutSeconds = writeTimeoutSeconds;
            return this;
        }

        public Builder enableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        public Builder allowInsecureSsl(boolean allowInsecureSsl) {
            this.allowInsecureSsl = allowInsecureSsl;
            return this;
        }

        public Builder headerProvider(@NonNull HeaderProvider headerProvider) {
            this.headerProvider = headerProvider;
            return this;
        }

        public Builder cookieStore(@NonNull CookieStore cookieStore) {
            this.cookieStore = cookieStore;
            return this;
        }

        public Builder crashReportEndpointProvider(@NonNull CrashReportEndpointProvider provider) {
            this.crashReportEndpointProvider = provider;
            return this;
        }

        public Builder authFailureListener(@NonNull AuthFailureListener listener) {
            this.authFailureListener = listener;
            return this;
        }

        public CoreRetrofitConfig build() {
            return new CoreRetrofitConfig(this);
        }
    }

    /**
     * 简易内存 Cookie 存储，适用于默认场景。
     */
    private static final class InMemoryCookieStore implements CookieStore {
        private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

        @Override
        public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<String> cookies) {
            if (!cookies.isEmpty()) {
                cache.put(url.host(), Collections.unmodifiableList(cookies));
            }
        }

        @NonNull
        @Override
        public List<String> loadForRequest(@NonNull HttpUrl url) {
            List<String> cookies = cache.get(url.host());
            return cookies != null ? cookies : Collections.emptyList();
        }
    }
}
