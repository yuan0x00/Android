package com.rapid.android.core.analytics.capture;

import android.util.Log;

/**
 * 核心异常捕获入口
 */
public class ExceptionCapture {

    private static final String TAG = "ExceptionCapture";

    private static boolean isInitialized = false;

    /**
     * 初始化异常捕获
     * 建议在 Application.onCreate() 中调用
     */
    public static void init() {
        if (isInitialized) return;
        isInitialized = true;

        // 安装 Java Crash 捕获
        JavaCrashHandler.install();

        Log.i(TAG, "ExceptionCapture initialized");
    }
}
