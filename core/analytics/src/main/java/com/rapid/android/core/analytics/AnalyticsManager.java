package com.rapid.android.core.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计分析管理器
 * 统一管理多个统计 SDK（友盟、Firebase、自定义等）
 */
public class AnalyticsManager {

    private static volatile AnalyticsManager instance;
    private final List<AnalyticsProvider> providers = new ArrayList<>();
    private boolean enabled = true;

    private AnalyticsManager() {
    }

    public static AnalyticsManager getInstance() {
        if (instance == null) {
            synchronized (AnalyticsManager.class) {
                if (instance == null) {
                    instance = new AnalyticsManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加统计提供者
     */
    public AnalyticsManager addProvider(@NonNull AnalyticsProvider provider) {
        if (!providers.contains(provider)) {
            providers.add(provider);
        }
        return this;
    }

    /**
     * 移除统计提供者
     */
    public AnalyticsManager removeProvider(@NonNull AnalyticsProvider provider) {
        providers.remove(provider);
        return this;
    }

    /**
     * 启用/禁用统计
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        for (AnalyticsProvider provider : providers) {
            provider.setEnabled(enabled);
        }
    }

    /**
     * 设置用户 ID
     */
    public void setUserId(@Nullable String userId) {
        if (!enabled) return;
        for (AnalyticsProvider provider : providers) {
            provider.setUserId(userId);
        }
    }

    /**
     * 设置用户属性
     */
    public void setUserProperty(@NonNull String key, @Nullable String value) {
        if (!enabled) return;
        for (AnalyticsProvider provider : providers) {
            provider.setUserProperty(key, value);
        }
    }

    /**
     * 记录事件（无参数）
     */
    public void logEvent(@NonNull String eventName) {
        logEvent(eventName, null);
    }

    /**
     * 记录事件（带参数）
     */
    public void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        if (!enabled) return;
        for (AnalyticsProvider provider : providers) {
            provider.logEvent(eventName, params);
        }
    }

    /**
     * 记录页面浏览
     */
    public void logPageView(@NonNull String pageName) {
        logPageView(pageName, null);
    }

    /**
     * 记录页面浏览（带参数）
     */
    public void logPageView(@NonNull String pageName, @Nullable Map<String, Object> params) {
        if (!enabled) return;
        Map<String, Object> mergedParams = params != null ? new HashMap<>(params) : new HashMap<>();
        mergedParams.put("page_name", pageName);
        logEvent("page_view", mergedParams);
    }

    /**
     * 记录屏幕停留时长
     */
    public void logScreenTime(@NonNull String screenName, long durationMillis) {
        if (!enabled) return;
        Map<String, Object> params = new HashMap<>();
        params.put("screen_name", screenName);
        params.put("duration_ms", durationMillis);
        logEvent("screen_time", params);
    }

    /**
     * 统计提供者接口
     */
    public interface AnalyticsProvider {
        void setEnabled(boolean enabled);
        void setUserId(@Nullable String userId);
        void setUserProperty(@NonNull String key, @Nullable String value);
        void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params);
    }

    /**
     * Builder 模式记录事件
     */
    public static class EventBuilder {
        private final String eventName;
        private final Map<String, Object> params = new HashMap<>();

        public EventBuilder(@NonNull String eventName) {
            this.eventName = eventName;
        }

        public EventBuilder param(@NonNull String key, @Nullable String value) {
            params.put(key, value);
            return this;
        }

        public EventBuilder param(@NonNull String key, long value) {
            params.put(key, value);
            return this;
        }

        public EventBuilder param(@NonNull String key, double value) {
            params.put(key, value);
            return this;
        }

        public EventBuilder param(@NonNull String key, boolean value) {
            params.put(key, value);
            return this;
        }

        public void log() {
            AnalyticsManager.getInstance().logEvent(eventName, params);
        }
    }
}
