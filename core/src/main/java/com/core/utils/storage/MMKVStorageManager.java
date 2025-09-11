package com.core.utils.storage;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.mmkv.MMKV;

/**
 * 基于 MMKV 的 IStorageManager 实现
 */
public class MMKVStorageManager implements IStorageManager {

    private final MMKV mmkv;

    public MMKVStorageManager() {
        this.mmkv = MMKV.defaultMMKV();
    }

    // 可选：支持指定 MMKV 实例（如多进程、加密等）
    public MMKVStorageManager(MMKV mmkv) {
        this.mmkv = mmkv != null ? mmkv : MMKV.defaultMMKV();
    }

    @Override
    public void putString(@NonNull String key, @Nullable String value) {
        if (TextUtils.isEmpty(key)) return;
        if (value == null) {
            mmkv.remove(key);
        } else {
            mmkv.encode(key, value);
        }
    }

    @Override
    public String getString(@NonNull String key, @Nullable String defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return mmkv.decodeString(key, defaultValue);
    }

    @Override
    public void putInt(@NonNull String key, int value) {
        if (TextUtils.isEmpty(key)) return;
        mmkv.encode(key, value);
    }

    @Override
    public int getInt(@NonNull String key, int defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return mmkv.decodeInt(key, defaultValue);
    }

    @Override
    public void putBoolean(@NonNull String key, boolean value) {
        if (TextUtils.isEmpty(key)) return;
        mmkv.encode(key, value);
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return mmkv.decodeBool(key, defaultValue);
    }

    @Override
    public void putFloat(@NonNull String key, float value) {
        if (TextUtils.isEmpty(key)) return;
        mmkv.encode(key, value);
    }

    @Override
    public float getFloat(@NonNull String key, float defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return mmkv.decodeFloat(key, defaultValue);
    }

    @Override
    public void putLong(@NonNull String key, long value) {
        if (TextUtils.isEmpty(key)) return;
        mmkv.encode(key, value);
    }

    @Override
    public long getLong(@NonNull String key, long defaultValue) {
        if (TextUtils.isEmpty(key)) return defaultValue;
        return mmkv.decodeLong(key, defaultValue);
    }

    @Override
    public void remove(@NonNull String key) {
        if (TextUtils.isEmpty(key)) return;
        mmkv.remove(key);
    }

    @Override
    public void clearAll() {
        mmkv.clearAll();
    }

    @Override
    public boolean contains(@NonNull String key) {
        if (TextUtils.isEmpty(key)) return false;
        return mmkv.containsKey(key);
    }
}