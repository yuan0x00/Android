package com.rapid.android.network.cookie;

import com.google.gson.Gson;
import com.rapid.android.core.storage.PreferenceHelper;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {
    // 持久化存储的键名
    private static final String COOKIE_STORE_KEY = "cookie_store";
    private static final String COOKIE_EXPIRY_KEY = "cookie_expiry_times";
    private final PreferenceHelper preferenceHelper;
    private final Gson gson;
    private Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public MyCookieJar() {
        preferenceHelper = PreferenceHelper.with("cookie");
        gson = new Gson();
        loadCookiesFromStorage();
    }

    @Override
    public @NonNull List<Cookie> loadForRequest(@NonNull HttpUrl url) {
        String domain = url.host();
        List<Cookie> cookies = cookieStore.get(domain);

        // 过滤过期 cookie
        if (cookies != null) {
            List<Cookie> validCookies = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (Cookie cookie : cookies) {
                if (cookie.expiresAt() > now) {
                    validCookies.add(cookie);
                }
            }

            // 如果有效cookie数量变化，更新存储
            if (validCookies.size() != cookies.size()) {
                if (validCookies.isEmpty()) {
                    cookieStore.remove(domain);
                } else {
                    cookieStore.put(domain, validCookies);
                }
                saveCookiesToStorage();
            }

            return validCookies;
        }

        return new ArrayList<>();
    }

    @Override
    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
        String domain = url.host();

        // 处理服务器清除 cookie 的情况 (max-Age=0)
        List<Cookie> validCookies = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Cookie cookie : cookies) {
            if (cookie.expiresAt() > now) {
                validCookies.add(cookie);
            }
        }

        if (validCookies.isEmpty()) {
            cookieStore.remove(domain);
        } else {
            cookieStore.put(domain, validCookies);
        }

        saveCookiesToStorage();
    }

    /**
     * 从持久化存储加载 cookies
     */
    private void loadCookiesFromStorage() {
        try {
            String cookieJson = preferenceHelper.getString(COOKIE_STORE_KEY, "");
            if (cookieJson != null && !cookieJson.isEmpty()) {
                CookieStoreData storeData = gson.fromJson(cookieJson, CookieStoreData.class);
                if (storeData != null && storeData.cookies != null) {
                    for (Map.Entry<String, List<SerializableCookie>> entry : storeData.cookies.entrySet()) {
                        List<Cookie> domainCookies = new ArrayList<>();
                        for (SerializableCookie serializableCookie : entry.getValue()) {
                            Cookie cookie = serializableCookie.toCookie();
                            if (cookie != null && cookie.expiresAt() > System.currentTimeMillis()) {
                                domainCookies.add(cookie);
                            }
                        }
                        if (!domainCookies.isEmpty()) {
                            cookieStore.put(entry.getKey(), domainCookies);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果加载失败，清空存储
            clearAllCookies();
        }
    }

    /**
     * 保存 cookies 到持久化存储
     */
    private void saveCookiesToStorage() {
        try {
            CookieStoreData storeData = new CookieStoreData();
            storeData.cookies = new HashMap<>();

            for (Map.Entry<String, List<Cookie>> entry : cookieStore.entrySet()) {
                List<SerializableCookie> serializableCookies = new ArrayList<>();
                for (Cookie cookie : entry.getValue()) {
                    serializableCookies.add(new SerializableCookie(cookie));
                }
                storeData.cookies.put(entry.getKey(), serializableCookies);
            }

            String cookieJson = gson.toJson(storeData);
            preferenceHelper.putString(COOKIE_STORE_KEY, cookieJson);
        } catch (Exception e) {
        }
    }

    /**
     * 清除所有 cookies（内存和持久化存储）
     */
    public void clearAllCookies() {
        cookieStore.clear();
        preferenceHelper.remove(COOKIE_STORE_KEY);
    }

    /**
     * 清除指定域名的 cookies
     */
    public void clearCookiesForDomain(String domain) {
        cookieStore.remove(domain);
        saveCookiesToStorage();
    }

    /**
     * 获取所有存储的域名
     */
    public List<String> getStoredDomains() {
        return new ArrayList<>(cookieStore.keySet());
    }

    /**
     * 可序列化的 Cookie 包装类
     */
    private static class SerializableCookie {
        private String name;
        private String value;
        private long expiresAt;
        private String domain;
        private String path;
        private boolean secure;
        private boolean httpOnly;
        private boolean persistent;
        private boolean hostOnly;

        public SerializableCookie() {}

        public SerializableCookie(Cookie cookie) {
            this.name = cookie.name();
            this.value = cookie.value();
            this.expiresAt = cookie.expiresAt();
            this.domain = cookie.domain();
            this.path = cookie.path();
            this.secure = cookie.secure();
            this.httpOnly = cookie.httpOnly();
            this.persistent = cookie.persistent();
            this.hostOnly = cookie.hostOnly();
        }

        public Cookie toCookie() {
            try {
                Cookie.Builder builder = new Cookie.Builder()
                        .name(name)
                        .value(value)
                        .expiresAt(expiresAt)
                        .path(path);

                if (hostOnly) {
                    builder.hostOnlyDomain(domain);
                } else {
                    builder.domain(domain);
                }

                if (secure) {
                    builder.secure();
                }

                if (httpOnly) {
                    builder.httpOnly();
                }

                return builder.build();
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Cookie 存储数据结构
     */
    private static class CookieStoreData {
        Map<String, List<SerializableCookie>> cookies;
    }
}