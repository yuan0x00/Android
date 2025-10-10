package com.rapid.android.analytics;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.analytics.AnalyticsManager;
import com.rapid.android.core.log.LogKit;

import java.util.Map;

/**
 * 应用层统计分析提供者实现
 * 可以对接友盟、Firebase 等第三方 SDK
 */
public class AppAnalyticsProvider implements AnalyticsManager.AnalyticsProvider {

    private static final String TAG = "AppAnalytics";
    private boolean enabled = true;

    public AppAnalyticsProvider(@NonNull Context context) {
        // 这里可以初始化第三方统计 SDK，如友盟、Firebase 等
        // 示例：
        // UMConfigure.init(context, "appKey", "channel", UMConfigure.DEVICE_TYPE_PHONE, null);
        LogKit.i(TAG, "Analytics provider initialized");
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LogKit.i(TAG, "Analytics enabled: " + enabled);
    }

    @Override
    public void setUserId(@Nullable String userId) {
        if (!enabled) return;
        LogKit.d(TAG, "Set user ID: " + userId);
        // 示例：
        // MobclickAgent.onProfileSignIn(userId);
    }

    @Override
    public void setUserProperty(@NonNull String key, @Nullable String value) {
        if (!enabled) return;
        LogKit.d(TAG, "Set user property: " + key + " = " + value);
        // 示例：
        // MobclickAgent.onProfileSignIn(key, value);
    }

    @Override
    public void logEvent(@NonNull String eventName, @Nullable Map<String, Object> params) {
        if (!enabled) return;
        LogKit.d(TAG, "Log event: " + eventName + ", params: " + params);
        // 示例：
        // if (params != null) {
        //     Map<String, String> stringParams = new HashMap<>();
        //     for (Map.Entry<String, Object> entry : params.entrySet()) {
        //         stringParams.put(entry.getKey(), String.valueOf(entry.getValue()));
        //     }
        //     MobclickAgent.onEventObject(context, eventName, stringParams);
        // } else {
        //     MobclickAgent.onEvent(context, eventName);
        // }
    }
}
