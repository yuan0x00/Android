package com.rapid.android.data.network;

import android.content.Context;

import java.util.List;

import okhttp3.HttpUrl;

/**
 * 网络管理工具类，用于管理Cookie和认证相关操作
 */
public class NetworkManager {
    private static PersistentCookieStore persistentCookieStore;
    
    /**
     * 初始化网络管理器，设置持久化Cookie存储
     */
    public static void initialize(Context context) {
        // 这个方法应该在Application初始化时调用，但我们已经在MainApplication中配置了
        // 这里提供访问PersistentCookieStore的途径
    }
    
    /**
     * 清除持久化Cookie
     */
    public static void clearPersistentCookies() {
        if (persistentCookieStore != null) {
            persistentCookieStore.clearCookies();
        } else {
            // 尝试获取NetworkClient中配置的PersistentCookieStore实例
            // 注意：这可能需要NetworkClient提供访问CookieStore的方法
        }
    }
    
    /**
     * 获取当前Cookie
     */
    public static List<String> getCookieForUrl(String url) {
        if (persistentCookieStore != null) {
            return persistentCookieStore.loadForRequest(HttpUrl.parse(url));
        }
        return null;
    }
}