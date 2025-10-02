package com.core.datastore;

/**
 * 数据存储接口定义
 */
public interface IDataStore {
    String getString(String key, String defaultValue);
    void putString(String key, String value);
    int getInt(String key, int defaultValue);
    void putInt(String key, int value);
    boolean getBoolean(String key, boolean defaultValue);
    void putBoolean(String key, boolean value);
    void remove(String key);
    void clear();
}