package com.example.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * SharedPreferences 工具类（无需每次传 Context）
 * 必须在 Application 中先调用 init() 初始化
 * 支持默认 SP 和多个自定义 SP 文件
 */
public class SPUtils {

    private static final String DEFAULT_SP_NAME = "app_config";
    private static final Map<String, SPUtils> instanceMap = new HashMap<>();
    private static final Object lock = new Object();
    private static Context appContext;
    private final String spName;
    private final SharedPreferences sp;

    /**
     * 私有构造
     */
    private SPUtils(String spName) {
        this.spName = spName;
        this.sp = appContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    // === 初始化方法（必须在 Application 中调用）===

    /**
     * 初始化（必须调用）
     */
    public static void init(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    /**
     * 获取默认 SP 实例（"app_config"）
     * 必须先调用 init(context)
     */
    public static SPUtils getInstance() {
        return getInstance(DEFAULT_SP_NAME);
    }

    /**
     * 获取指定名称的 SP 实例
     * 如果未初始化 context，会抛异常
     */
    public static SPUtils getInstance(@Nullable String spName) {
        if (appContext == null) {
            throw new IllegalStateException(
                    "SPUtils not initialized. Call SPUtils.init(context) in Application."
            );
        }
        String name = spName == null ? DEFAULT_SP_NAME : spName;

        SPUtils instance = instanceMap.get(name);
        if (instance == null) {
            synchronized (lock) {
                instance = instanceMap.get(name);
                if (instance == null) {
                    instance = new SPUtils(name);
                    instanceMap.put(name, instance);
                }
            }
        }
        return instance;
    }

    // === 数据操作方法 ===

    /**
     * 释放指定 SP 实例
     */
    public static void release(String spName) {
        String name = spName == null ? DEFAULT_SP_NAME : spName;
        synchronized (lock) {
            instanceMap.remove(name);
        }
    }

    /**
     * 释放所有
     */
    public static void releaseAll() {
        synchronized (lock) {
            instanceMap.clear();
        }
    }

    public void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public void putFloat(String key, float value) {
        sp.edit().putFloat(key, value).apply();
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    public void remove(String key) {
        sp.edit().remove(key).apply();
    }

    // === 辅助方法 ===

    public void clear() {
        sp.edit().clear().apply();
    }

    public boolean contains(String key) {
        return sp.contains(key);
    }

    public String getSpName() {
        return spName;
    }

    public SharedPreferences getSharedPreferences() {
        return sp;
    }
}