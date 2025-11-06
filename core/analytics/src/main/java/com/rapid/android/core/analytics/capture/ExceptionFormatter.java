package com.rapid.android.core.analytics.capture;

import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ExceptionFormatter {

    /**
     * 格式化 Java 异常
     */
    public static String formatJavaException(Thread thread, Throwable e) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "java_crash");
        map.put("thread_name", thread.getName());
        map.put("thread_id", thread.getId());
        map.put("exception_name", e.getClass().getName());
        map.put("message", e.getMessage());
        map.put("stack_trace", Log.getStackTraceString(e));
        map.put("os_version", Build.VERSION.RELEASE);
        map.put("platform", "Android");
        map.put("timestamp", System.currentTimeMillis());

        return map.toString();
    }

    /**
     * 格式化 Native Crash
     */
    public static String formatNativeException(String crashInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "native_crash");
        map.put("crash_info", crashInfo);
        map.put("os_version", Build.VERSION.RELEASE);
        map.put("platform", "Android");
        map.put("timestamp", System.currentTimeMillis());

        return map.toString();
    }
}
