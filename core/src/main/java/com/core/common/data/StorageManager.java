package com.core.common.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.mmkv.MMKV;

import java.util.Map;
import java.util.Set;

/**
 * 存储管理门面类（静态代理 IStorageManager）
 * 对外提供静态方法，内部委托给具体实现
 * 默认使用 MMKV 实现，支持替换为其他存储方案
 */
public class StorageManager {

    private static final Object lock = new Object();
    private static volatile IStorageManager implementation;

    /**
     * 初始化（在 Application.onCreate 中调用）
     * 使用默认 MMKV 实现
     */
    public static void init() {
        init(null);
    }

    /**
     * 初始化（可传入自定义 MMKV 实例）
     */
    public static void init(@Nullable MMKV customMMKV) {
        if (implementation == null) {
            synchronized (lock) {
                if (implementation == null) {
                    implementation = new MMKVStorageManager(customMMKV);
                }
            }
        }
    }

    /**
     * 设置自定义存储实现（用于测试或替换底层存储方案）
     * 必须在使用任何存储方法之前调用
     */
    public static void setImplementation(@NonNull IStorageManager storageManager) {
        if (storageManager == null) {
            throw new IllegalArgumentException("Storage implementation cannot be null");
        }
        synchronized (lock) {
            implementation = storageManager;
        }
    }

    @NonNull
    private static IStorageManager getStorage() {
        if (implementation == null) {
            throw new IllegalStateException(
                    "StorageManager not initialized. Call StorageManager.init() in Application.onCreate()"
            );
        }
        return implementation;
    }

    // —————— 基础操作 ——————

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

    // —————— 批量操作（新增）——————

    /**
     * 批量存储多个键值对
     */
    public static void putAll(@NonNull Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        IStorageManager storage = getStorage();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                storage.putString(key, (String) value);
            } else if (value instanceof Integer) {
                storage.putInt(key, (Integer) value);
            } else if (value instanceof Boolean) {
                storage.putBoolean(key, (Boolean) value);
            } else if (value instanceof Float) {
                storage.putFloat(key, (Float) value);
            } else if (value instanceof Long) {
                storage.putLong(key, (Long) value);
            }
        }
    }

    /**
     * 批量删除多个键
     */
    public static void removeAll(@NonNull Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        IStorageManager storage = getStorage();
        for (String key : keys) {
            storage.remove(key);
        }
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return implementation != null;
    }
}