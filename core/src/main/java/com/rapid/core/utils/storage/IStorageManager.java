package com.rapid.core.utils.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 通用键值存储接口
 * 可被 MMKV / SharedPreferences / DataStore 等实现
 */
public interface IStorageManager {

    // —————— String ——————
    void putString(@NonNull String key, @Nullable String value);
    @Nullable
    String getString(@NonNull String key, @Nullable String defaultValue);

    // —————— Int ——————
    void putInt(@NonNull String key, int value);
    int getInt(@NonNull String key, int defaultValue);

    // —————— Boolean ——————
    void putBoolean(@NonNull String key, boolean value);
    boolean getBoolean(@NonNull String key, boolean defaultValue);

    // —————— Float ——————
    void putFloat(@NonNull String key, float value);
    float getFloat(@NonNull String key, float defaultValue);

    // —————— Long ——————
    void putLong(@NonNull String key, long value);
    long getLong(@NonNull String key, long defaultValue);

    // —————— 其他 ——————
    void remove(@NonNull String key);
    void clearAll();
    boolean contains(@NonNull String key);
}