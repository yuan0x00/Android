package com.rapid.android.analytics;

import androidx.annotation.NonNull;

import com.rapid.android.core.analytics.CrashReporter;
import com.rapid.android.core.log.LogKit;

/**
 * 应用层崩溃处理器实现
 * 可以对接 Bugly、Sentry 等第三方崩溃上报 SDK
 */
public class AppCrashHandler implements CrashReporter.CrashHandler {

    private static final String TAG = "AppCrashHandler";

    public AppCrashHandler() {
        // 这里可以初始化第三方崩溃上报 SDK，如 Bugly、Sentry 等
        // 示例：
        // CrashReport.initCrashReport(context, "appId", BuildConfig.DEBUG);
        LogKit.i(TAG, "Crash handler initialized");
    }

    @Override
    public void handleCrash(@NonNull CrashReporter.CrashInfo crashInfo) {
        // 上报崩溃信息到第三方 SDK
        LogKit.e(TAG, crashInfo.getThrowable(), "Crash: " + crashInfo.getExceptionType());

        // 示例：
        // CrashReport.postCatchedException(crashInfo.getThrowable());
    }

    @Override
    public void setUserIdentifier(@NonNull String userId) {
        LogKit.d(TAG, "Set user identifier: " + userId);
        // 示例：
        // CrashReport.setUserId(userId);
    }

    @Override
    public void setCustomKey(@NonNull String key, @NonNull String value) {
        LogKit.d(TAG, "Set custom key: " + key + " = " + value);
        // 示例：
        // CrashReport.putUserData(context, key, value);
    }
}
