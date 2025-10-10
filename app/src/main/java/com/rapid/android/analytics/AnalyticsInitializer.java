package com.rapid.android.analytics;

import android.content.Context;

import androidx.annotation.NonNull;

import com.rapid.android.core.analytics.AnalyticsManager;
import com.rapid.android.core.analytics.CrashReporter;

/**
 * 统计分析初始化器
 */
public final class AnalyticsInitializer {

    private AnalyticsInitializer() {
    }

    /**
     * 初始化统计分析模块
     */
    public static void init(@NonNull Context context) {
        // 初始化统计分析提供者
        AppAnalyticsProvider analyticsProvider = new AppAnalyticsProvider(context);
        AnalyticsManager.getInstance().addProvider(analyticsProvider);

        // 初始化崩溃报告处理器
        AppCrashHandler crashHandler = new AppCrashHandler();
        CrashReporter.getInstance().addHandler(crashHandler);
    }

    /**
     * 设置用户信息
     */
    public static void setUserInfo(@NonNull String userId) {
        AnalyticsManager.getInstance().setUserId(userId);
        CrashReporter.getInstance().setUserIdentifier(userId);
    }

    /**
     * 清除用户信息（用户登出时调用）
     */
    public static void clearUserInfo() {
        AnalyticsManager.getInstance().setUserId(null);
        CrashReporter.getInstance().setUserIdentifier("");
    }
}
