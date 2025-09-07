package com.rapid.core.utils.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.mmkv.MMKV;

/**
 * 存储管理门面类（静态代理 IStorageManager）
 * 对外提供静态方法，内部委托给具体实现
 * 默认使用 MMKV 实现，未来可替换
 */
public class MMKVManager {

    private static volatile IStorageManager sStorageManager;

    /**
     * 初始化（在 Application.onCreate 中调用）
     */
    public static void init() {
        init(null);
    }

    /**
     * 初始化（可传入自定义 MMKV 实例）
     */
    public static void init(@Nullable MMKV customMMKV) {
        if (sStorageManager == null) {
            synchronized (MMKVManager.class) {
                if (sStorageManager == null) {
                    // 默认使用 MMKV 实现
                    sStorageManager = new MMKVStorageManager(customMMKV);
                }
            }
        }
    }

    /**
     * ⚠️ 测试/替换用：允许外部注入实现（如单元测试时注入 Mock）
     */
    public static void setStorageManager(@NonNull IStorageManager storageManager) {
        sStorageManager = storageManager;
    }

    private static IStorageManager getStorage() {
        if (sStorageManager == null) {
            throw new IllegalStateException("MMKVManager not initialized. Call init() in Application.onCreate()");
        }
        return sStorageManager;
    }

    // —————— 代理方法 ——————

    public static void putString(@NonNull String key, @Nullable String value) {
        getStorage().putString(key, value);
    }

    @Nullable
    public static String getString(@NonNull String key, @Nullable String defaultValue) {
        return getStorage().getString(key, defaultValue);
    }

    public static void putInt(@NonNull String key, int value) {
        getStorage().putInt(key, value);
    }

    public static int getInt(@NonNull String key, int defaultValue) {
        return getStorage().getInt(key, defaultValue);
    }

    public static void putBoolean(@NonNull String key, boolean value) {
        getStorage().putBoolean(key, value);
    }

    public static boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return getStorage().getBoolean(key, defaultValue);
    }

    public static void putFloat(@NonNull String key, float value) {
        getStorage().putFloat(key, value);
    }

    public static float getFloat(@NonNull String key, float defaultValue) {
        return getStorage().getFloat(key, defaultValue);
    }

    public static void putLong(@NonNull String key, long value) {
        getStorage().putLong(key, value);
    }

    public static long getLong(@NonNull String key, long defaultValue) {
        return getStorage().getLong(key, defaultValue);
    }

    public static void remove(@NonNull String key) {
        getStorage().remove(key);
    }

    public static void clearAll() {
        getStorage().clearAll();
    }

    public static boolean contains(@NonNull String key) {
        return getStorage().contains(key);
    }
}