package com.rapid.android.core.webview.utils;

import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import androidx.annotation.NonNull;

/**
 * WebView 初始化优化工具类
 * 提供一次性的全局优化配置
 */
public final class WebViewInitOptimizer {

    private static boolean initialized = false;

    private WebViewInitOptimizer() {
        // 工具类，禁止实例化
    }

    /**
     * 初始化 WebView 全局配置
     * 应在 Application.onCreate() 中调用一次
     *
     * @param context Application Context
     */
    public static synchronized void init(@NonNull Context context) {
        if (initialized) {
            return;
        }

        try {
            // 在独立进程中初始化 WebView，避免污染主进程
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                String processName = getProcessName(context);
                String packageName = context.getPackageName();
                if (!packageName.equals(processName)) {
                    WebView.setDataDirectorySuffix(processName);
                }
            }

            // 提前触发 WebView 初始化（在子线程执行以避免阻塞主线程）
            new Thread(() -> {
                try {
                    WebView webView = new WebView(context.getApplicationContext());
                    webView.destroy();
                } catch (Exception e) {
                    // 忽略初始化异常
                }
            }).start();

            initialized = true;
        } catch (Exception e) {
            // 忽略初始化异常
        }
    }

    /**
     * 获取当前进程名称
     */
    private static String getProcessName(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return android.app.Application.getProcessName();
        }

        // 低版本兼容
        int pid = android.os.Process.myPid();
        android.app.ActivityManager am = (android.app.ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            for (android.app.ActivityManager.RunningAppProcessInfo processInfo : am.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
        }
        return context.getPackageName();
    }

    /**
     * 启用 WebView 调试模式（仅在 Debug 构建中调用）
     */
    public static void enableDebugMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}
