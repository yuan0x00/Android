package com.rapid.android.core.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.rapid.android.core.network.client.NetworkConfig;

import org.json.JSONArray;
import org.json.JSONException;

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
    private static final String KEY_PREFIX = "cookie_host_";

    private static PersistentCookieStore instance;

    private final Map<String, List<String>> cookieMap = new ConcurrentHashMap<>();
    private final SharedPreferences sharedPreferences;
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
            cookieMap.put(host, Collections.unmodifiableList(new ArrayList<>(cookies)));
            saveCookies(host, cookies);
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
        if (cookies != null) {
            return cookies;
        }

        String stored = sharedPreferences.getString(buildKey(host), null);
        if (stored == null || stored.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> restored = parseCookieList(stored);
        if (!restored.isEmpty()) {
            cookieMap.put(host, Collections.unmodifiableList(restored));
        }
        return restored;
    }

    private synchronized void loadCookies() {
        if (loaded) {
            return;
        }
        try {
            Map<String, ?> all = sharedPreferences.getAll();
            for (Map.Entry<String, ?> entry : all.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!(value instanceof String) || !key.startsWith(KEY_PREFIX)) {
                    continue;
                }
                String host = key.substring(KEY_PREFIX.length());
                List<String> restored = parseCookieList((String) value);
                if (!restored.isEmpty()) {
                    cookieMap.put(host, Collections.unmodifiableList(restored));
                }
            }
        } catch (Exception e) {
            cookieMap.clear();
        }
        loaded = true;
    }

    /**
     * 清除所有持久化Cookie
     */
    public void clearCookies() {
        cookieMap.clear();
        sharedPreferences.edit().clear().apply();
    }

    private void saveCookies(String host, List<String> cookies) {
        JSONArray array = new JSONArray();
        for (String cookie : cookies) {
            array.put(cookie);
        }
        sharedPreferences.edit().putString(buildKey(host), array.toString()).apply();
    }

    private String buildKey(String host) {
        return KEY_PREFIX + host;
    }

    private List<String> parseCookieList(String payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = new JSONArray(payload);
            List<String> result = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, null);
                if (value != null && !value.isEmpty()) {
                    result.add(value);
                }
            }
            return result;
        } catch (JSONException e) {
            return Collections.emptyList();
        }
    }
}
