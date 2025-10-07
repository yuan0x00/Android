package com.rapid.android.core.log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

/**
 * 日志统一入口，基于 Timber 封装，提供统一初始化和多重重载。
 */
public final class LogKit {

    private static volatile boolean initialized = false;

    private LogKit() {
    }

    public static void init(boolean isDebug) {
        if (initialized) {
            return;
        }
        synchronized (LogKit.class) {
            if (initialized) {
                return;
            }
            if (isDebug) {
                Timber.plant(new DebugTree());
            } else {
                Timber.plant(new ReleaseTree());
            }
            initialized = true;
        }
    }

    public static void reset() {
        synchronized (LogKit.class) {
            Timber.uprootAll();
            initialized = false;
        }
    }

    // Verbose
    public static void v(@NonNull String message, Object... args) {
        Timber.v(message, args);
    }

    public static void v(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.v(t, message, args);
    }

    public static void v(@NonNull String tag, @NonNull String message, Object... args) {
        Timber.tag(tag).v(message, args);
    }

    public static void v(@NonNull String tag, @Nullable Throwable t, @NonNull String message, Object... args) {
        if (t != null) {
            Timber.tag(tag).v(t, message, args);
        } else {
            Timber.tag(tag).v(message, args);
        }
    }

    // Debug
    public static void d(@NonNull String message, Object... args) {
        Timber.d(message, args);
    }

    public static void d(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.d(t, message, args);
    }

    public static void d(@NonNull String tag, @NonNull String message, Object... args) {
        Timber.tag(tag).d(message, args);
    }

    public static void d(@NonNull String tag, @Nullable Throwable t, @NonNull String message, Object... args) {
        if (t != null) {
            Timber.tag(tag).d(t, message, args);
        } else {
            Timber.tag(tag).d(message, args);
        }
    }

    // Info
    public static void i(@NonNull String message, Object... args) {
        Timber.i(message, args);
    }

    public static void i(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.i(t, message, args);
    }

    public static void i(@NonNull String tag, @NonNull String message, Object... args) {
        Timber.tag(tag).i(message, args);
    }

    public static void i(@NonNull String tag, @Nullable Throwable t, @NonNull String message, Object... args) {
        if (t != null) {
            Timber.tag(tag).i(t, message, args);
        } else {
            Timber.tag(tag).i(message, args);
        }
    }

    // Warn
    public static void w(@NonNull String message, Object... args) {
        Timber.w(message, args);
    }

    public static void w(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.w(t, message, args);
    }

    public static void w(@NonNull String tag, @NonNull String message, Object... args) {
        Timber.tag(tag).w(message, args);
    }

    public static void w(@NonNull String tag, @Nullable Throwable t, @NonNull String message, Object... args) {
        if (t != null) {
            Timber.tag(tag).w(t, message, args);
        } else {
            Timber.tag(tag).w(message, args);
        }
    }

    // Error
    public static void e(@NonNull String message, Object... args) {
        Timber.e(message, args);
    }

    public static void e(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.e(t, message, args);
    }

    public static void e(@NonNull String tag, @NonNull String message, Object... args) {
        Timber.tag(tag).e(message, args);
    }

    public static void e(@NonNull String tag, @Nullable Throwable t, @NonNull String message, Object... args) {
        if (t != null) {
            Timber.tag(tag).e(t, message, args);
        } else {
            Timber.tag(tag).e(message, args);
        }
    }

    // Assert
    public static void wtf(@NonNull String message, Object... args) {
        Timber.wtf(message, args);
    }

    public static void wtf(@Nullable Throwable t, @NonNull String message, Object... args) {
        Timber.wtf(t, message, args);
    }

    public static void wtf(@NonNull String tag, @NonNull String message, Object... args) {
        Timber.tag(tag).wtf(message, args);
    }

    public static void wtf(@NonNull String tag, @Nullable Throwable t, @NonNull String message, Object... args) {
        if (t != null) {
            Timber.tag(tag).wtf(t, message, args);
        } else {
            Timber.tag(tag).wtf(message, args);
        }
    }
}
