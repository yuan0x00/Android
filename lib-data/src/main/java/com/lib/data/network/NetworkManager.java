package com.lib.data.network;

import android.content.Context;

import com.core.network.client.NetworkClient;
import com.core.network.client.NetworkConfig;

import java.util.Collections;
import java.util.List;

import okhttp3.HttpUrl;

/**
 * 网络管理工具类，用于管理Cookie和认证相关操作
 */
public class NetworkManager {
    
    /**
     * 初始化网络管理器，设置持久化Cookie存储
     */
    public static void initialize(Context context) {
        if (context != null && PersistentCookieStore.getInstance() == null) {
            new PersistentCookieStore(context.getApplicationContext());
        }
    }
    
    /**
     * 清除持久化Cookie
     */
    public static void clearPersistentCookies() {
        PersistentCookieStore cookieStore = resolveCookieStore();
        if (cookieStore != null) {
            cookieStore.clearCookies();
        }
    }
    
    /**
     * 获取当前Cookie
     */
    public static List<String> getCookieForUrl(String url) {
        PersistentCookieStore cookieStore = resolveCookieStore();
        if (cookieStore == null || url == null) {
            return Collections.emptyList();
        }
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            return Collections.emptyList();
        }
        return cookieStore.loadForRequest(httpUrl);
    }

    private static PersistentCookieStore resolveCookieStore() {
        PersistentCookieStore existing = PersistentCookieStore.getInstance();
        if (existing != null) {
            return existing;
        }
        NetworkConfig.CookieStore configuredStore = NetworkClient.getActiveConfig().getCookieStore();
        if (configuredStore instanceof PersistentCookieStore) {
            return (PersistentCookieStore) configuredStore;
        }
        return null;
    }
}
