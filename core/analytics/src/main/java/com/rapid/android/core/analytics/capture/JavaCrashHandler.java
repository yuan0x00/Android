package com.rapid.android.core.analytics.capture;

import android.util.Log;

public class JavaCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "JavaCrashHandler";
    private static Thread.UncaughtExceptionHandler defaultHandler;

    public static void install() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new JavaCrashHandler());
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 格式化异常
        String formatted = ExceptionFormatter.formatJavaException(t, e);

        // 立即打印日志
        Log.e(TAG, formatted);

        // 可以保留系统默认处理
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }
}
