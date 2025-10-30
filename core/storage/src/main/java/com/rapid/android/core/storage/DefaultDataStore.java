package com.rapid.android.core.storage;

import com.tencent.mmkv.MMKV;

/**
 * 数据存储的默认实现
 */
public class DefaultDataStore implements IDataStore {
    private final MMKV mmkv;

    public DefaultDataStore() {
        this.mmkv = MMKV.defaultMMKV();
    }

    public DefaultDataStore(MMKV mmkv) {
        this.mmkv = mmkv != null ? mmkv : MMKV.defaultMMKV();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return mmkv.getString(key, defaultValue);
    }

    @Override
    public void putString(String key, String value) {
        mmkv.putString(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return mmkv.getInt(key, defaultValue);
    }

    @Override
    public void putInt(String key, int value) {
        mmkv.putInt(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return mmkv.getBoolean(key, defaultValue);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        mmkv.putBoolean(key, value);
    }

    @Override
    public void remove(String key) {
        mmkv.removeValueForKey(key);
    }

    @Override
    public void clear() {
        mmkv.clearAll();
    }
}