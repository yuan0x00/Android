package com.rapid.android.core.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 崩溃报告器
 * 统一管理崩溃日志上报
 */
public class CrashReporter {

    private static volatile CrashReporter instance;
    private final List<CrashHandler> handlers = new ArrayList<>();

    private CrashReporter() {
    }

    public static CrashReporter getInstance() {
        if (instance == null) {
            synchronized (CrashReporter.class) {
                if (instance == null) {
                    instance = new CrashReporter();
                }
            }
        }
        return instance;
    }

    /**
     * 添加崩溃处理器
     */
    public CrashReporter addHandler(@NonNull CrashHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
        return this;
    }

    /**
     * 移除崩溃处理器
     */
    public CrashReporter removeHandler(@NonNull CrashHandler handler) {
        handlers.remove(handler);
        return this;
    }

    /**
     * 报告异常
     */
    public void reportException(@NonNull Throwable throwable) {
        reportException(throwable, null);
    }

    /**
     * 报告异常（带额外信息）
     */
    public void reportException(@NonNull Throwable throwable, @Nullable Map<String, String> extras) {
        CrashInfo crashInfo = new CrashInfo(throwable, extras);

        // 通知所有处理器
        for (CrashHandler handler : handlers) {
            try {
                handler.handleCrash(crashInfo);
            } catch (Exception e) {
                // 忽略处理器自身的异常
            }
        }

        // 记录到统计系统
        Map<String, Object> params = new HashMap<>();
        params.put("exception_type", throwable.getClass().getSimpleName());
        params.put("exception_message", throwable.getMessage());
        params.put("stack_trace", getStackTraceString(throwable));
        if (extras != null) {
            params.putAll(extras);
        }
        AnalyticsManager.getInstance().logEvent("crash", params);
    }

    /**
     * 报告非致命错误
     */
    public void reportNonFatalError(@NonNull String errorType, @NonNull String errorMessage) {
        reportNonFatalError(errorType, errorMessage, null);
    }

    /**
     * 报告非致命错误（带额外信息）
     */
    public void reportNonFatalError(@NonNull String errorType,
                                     @NonNull String errorMessage,
                                     @Nullable Map<String, String> extras) {
        Map<String, Object> params = new HashMap<>();
        params.put("error_type", errorType);
        params.put("error_message", errorMessage);
        params.put("fatal", false);
        if (extras != null) {
            params.putAll(extras);
        }
        AnalyticsManager.getInstance().logEvent("non_fatal_error", params);
    }

    /**
     * 设置用户标识
     */
    public void setUserIdentifier(@NonNull String userId) {
        for (CrashHandler handler : handlers) {
            handler.setUserIdentifier(userId);
        }
    }

    /**
     * 设置自定义键值
     */
    public void setCustomKey(@NonNull String key, @NonNull String value) {
        for (CrashHandler handler : handlers) {
            handler.setCustomKey(key, value);
        }
    }

    /**
     * 获取堆栈信息字符串
     */
    @NonNull
    private String getStackTraceString(@NonNull Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 崩溃处理器接口
     */
    public interface CrashHandler {
        void handleCrash(@NonNull CrashInfo crashInfo);
        void setUserIdentifier(@NonNull String userId);
        void setCustomKey(@NonNull String key, @NonNull String value);
    }

    /**
     * 崩溃信息
     */
    public static class CrashInfo {
        private final Throwable throwable;
        private final Map<String, String> extras;
        private final long timestamp;

        public CrashInfo(@NonNull Throwable throwable, @Nullable Map<String, String> extras) {
            this.throwable = throwable;
            this.extras = extras != null ? new HashMap<>(extras) : new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }

        @NonNull
        public Throwable getThrowable() {
            return throwable;
        }

        @NonNull
        public Map<String, String> getExtras() {
            return new HashMap<>(extras);
        }

        public long getTimestamp() {
            return timestamp;
        }

        @NonNull
        public String getExceptionType() {
            return throwable.getClass().getName();
        }

        @Nullable
        public String getExceptionMessage() {
            return throwable.getMessage();
        }

        @NonNull
        public String getStackTrace() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }
}
