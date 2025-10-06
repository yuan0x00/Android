package com.core.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.core.network.client.NetworkConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.HttpUrl;

/**
 * 持久化Cookie存储，使用SharedPreferences保存Cookie信息
 * 以确保应用重启后Cookie仍然有效
 */
public class PersistentCookieStore implements NetworkConfig.CookieStore {
    private static final String PREF_NAME = "persistent_cookies";
    private static final String KEY_COOKIE_MAP = "cookie_map";
    
    private static PersistentCookieStore instance;
    
    private final Map<String, List<String>> cookieMap = new ConcurrentHashMap<>();
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();
    private boolean loaded = false;

    public PersistentCookieStore(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        instance = this; // 设置单例引用
        loadCookies();
    }
    
    /**
     * 获取实例，用于外部访问（如登出时清除Cookie）
     */
    public static PersistentCookieStore getInstance() {
        return instance;
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<String> cookies) {
        if (!cookies.isEmpty()) {
            String host = url.host();
            cookieMap.put(host, new ArrayList<>(cookies));
            saveCookies();
        }
    }

    @NonNull
    @Override
    public List<String> loadForRequest(@NonNull HttpUrl url) {
        if (!loaded) {
            loadCookies();
        }
        
        String host = url.host();
        List<String> cookies = cookieMap.get(host);
        return cookies != null ? cookies : Collections.emptyList();
    }

    private void loadCookies() {
        try {
            String cookieMapJson = sharedPreferences.getString(KEY_COOKIE_MAP, "{}");
            TypeToken<Map<String, List<String>>> typeToken = new TypeToken<Map<String, List<String>>>() {};
            Map<String, List<String>> loadedMap = gson.fromJson(cookieMapJson, typeToken.getType());
            
            if (loadedMap != null) {
                cookieMap.clear();
                cookieMap.putAll(loadedMap);
            } else {
                cookieMap.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            cookieMap.clear();
        }
        loaded = true;
    }

    private void saveCookies() {
        try {
            String cookieMapJson = gson.toJson(cookieMap);
            sharedPreferences.edit().putString(KEY_COOKIE_MAP, cookieMapJson).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除所有持久化Cookie
     */
    public void clearCookies() {
        cookieMap.clear();
        saveCookies();
    }
}