package com.rapid.android.core.storage;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 MMKV 的统一键值对存储工具类
 */
public class PreferenceHelper {

    private static final String DEFAULT_MMKV_ID = "default_config";
    private final MMKV mmkv;

    // 私有构造，防止外部 new
    private PreferenceHelper(String mmkvId) {
        this.mmkv = MMKV.mmkvWithID(mmkvId, MMKV.SINGLE_PROCESS_MODE);
    }

    // 获取默认实例（全局配置）
    public static PreferenceHelper getDefault() {
        return Holder.DEFAULT_INSTANCE;
    }

    // 获取指定 ID 的实例（如用户专属配置）
    public static PreferenceHelper with(@NonNull String mmkvId) {
        if (TextUtils.isEmpty(mmkvId)) {
            throw new IllegalArgumentException("mmkvId cannot be empty");
        }
        return new PreferenceHelper(mmkvId);
    }

    public void putString(@NonNull String key, @Nullable String value) {
        mmkv.encode(key, value);
    }

    // ========== 基础类型读写 ==========

    @Nullable
    public String getString(@NonNull String key, @Nullable String defValue) {
        return mmkv.decodeString(key, defValue);
    }

    public void putInt(@NonNull String key, int value) {
        mmkv.encode(key, value);
    }

    public int getInt(@NonNull String key, int defValue) {
        return mmkv.decodeInt(key, defValue);
    }

    public void putLong(@NonNull String key, long value) {
        mmkv.encode(key, value);
    }

    public long getLong(@NonNull String key, long defValue) {
        return mmkv.decodeLong(key, defValue);
    }

    public void putFloat(@NonNull String key, float value) {
        mmkv.encode(key, value);
    }

    public float getFloat(@NonNull String key, float defValue) {
        return mmkv.decodeFloat(key, defValue);
    }

    public void putBoolean(@NonNull String key, boolean value) {
        mmkv.encode(key, value);
    }

    public boolean getBoolean(@NonNull String key, boolean defValue) {
        return mmkv.decodeBool(key, defValue);
    }

    public void putStringSet(@NonNull String key, @Nullable Set<String> value) {
        if (value == null) {
            mmkv.removeValueForKey(key);
        } else {
            mmkv.encode(key, value);
        }
    }

    @NonNull
    public Set<String> getStringSet(@NonNull String key, @NonNull Set<String> defaultValue) {
        Set<String> set = mmkv.decodeStringSet(key);
        if (set != null) {
            return set;
        }
        return defaultValue;
    }

    public void putStringList(@NonNull String key, @Nullable List<String> value) {
        if (value == null) {
            mmkv.removeValueForKey(key);
        } else {
            // List 转 Set 保存
            Set<String> set = new HashSet<>(value);
            mmkv.encode(key, set);
        }
    }

    @NonNull
    public List<String> getStringList(@NonNull String key, @NonNull List<String> defaultValue) {
        Set<String> set = mmkv.decodeStringSet(key, null);
        if (set != null) {
            // Set 转 List
            return new ArrayList<>(set);
        }
        return defaultValue;
    }

    public void remove(@NonNull String key) {
        mmkv.removeValueForKey(key);
    }

    // ========== 其他 ==========

    public void clearAll() {
        mmkv.clearAll();
    }

    public boolean contains(@NonNull String key) {
        return mmkv.contains(key);
    }

    // 静态内部类单例（线程安全）
    private static class Holder {
        static final PreferenceHelper DEFAULT_INSTANCE = new PreferenceHelper(DEFAULT_MMKV_ID);
    }
}