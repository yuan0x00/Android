package com.core.log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

/**
 * 核心日志工具类，封装 Timber，支持：
 * - Debug 模式：输出类名:行号 TAG + 完整堆栈
 * - Release 模式：仅输出 WARN/ERROR/ASSERT，可扩展上传或写文件
 * - 线程安全初始化
 * - 支持重置（用于测试或动态切换）
 */
public class CoreLogger {

    private static volatile boolean sInitialized = false;

    /**
     * 初始化日志系统
     *
     * @param isDebug 是否为调试模式（决定是否输出到控制台）
     */
    public static void init(boolean isDebug) {
        if (sInitialized) {
            return;
        }
        synchronized (CoreLogger.class) {
            if (sInitialized) {
                return;
            }

            if (isDebug) {
                Timber.plant(new DebugTree());
            } else {
                Timber.plant(new ReleaseTree());
            }

            sInitialized = true;
        }
    }

    /**
     * 重置日志系统（移除所有 Tree，可重新 init）
     * 适用于单元测试、动态切换 Debug/Release 模式
     */
    public static void reset() {
        synchronized (CoreLogger.class) {
            Timber.uprootAll();
            sInitialized = false;
        }
    }

    // ============ 便捷调用方法（代理 Timber） ============

    public static void v(@NonNull String message, Object... args) {
        Timber.v(message, args);
    }

    public static void v(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.v(t, message, args);
    }

    public static void d(@NonNull String message, Object... args) {
        Timber.d(message, args);
    }

    public static void d(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.d(t, message, args);
    }

    public static void i(@NonNull String message, Object... args) {
        Timber.i(message, args);
    }

    public static void i(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.i(t, message, args);
    }

    public static void w(@NonNull String message, Object... args) {
        Timber.w(message, args);
    }

    public static void w(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.w(t, message, args);
    }

    public static void e(@NonNull String message, Object... args) {
        Timber.e(message, args);
    }

    public static void e(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.e(t, message, args);
    }

}