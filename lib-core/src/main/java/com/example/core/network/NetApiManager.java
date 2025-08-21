package com.example.core.network;

import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;

/**
 * 网络 API 管理器
 * - 支持业务方自定义传入 Retrofit
 * - 若未设置，则自动使用默认配置初始化（开箱即用）
 */
public class NetApiManager {

    private static final Object retrofitLock = new Object();
    // 缓存 API 实例
    private static final ConcurrentHashMap<Class<?>, Object> apiCache = new ConcurrentHashMap<>();
    private static final Object cacheLock = new Object();
    private static Retrofit retrofit;
    // 默认配置标志
    private static volatile boolean isDefaultRetrofitCreated = false;

    /**
     * 获取当前 Retrofit 实例（自动创建默认实例，如果尚未设置）
     */
    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (retrofitLock) {
                if (retrofit == null && !isDefaultRetrofitCreated) {
                    // 创建默认实例
                    retrofit = DefaultRetrofitBuilder.getInstance().getRetrofit();
                    isDefaultRetrofitCreated = true;
                }
            }
        }
        return retrofit;
    }

    /**
     * 设置自定义 Retrofit 实例（优先级最高）
     */
    public static void setRetrofit(@NonNull Retrofit retrofit) {
        synchronized (retrofitLock) {
            // 检查是否已经自动创建了默认实例
            if (isDefaultRetrofitCreated) {
                throw new IllegalStateException(
                        "NetApiManager: Cannot set custom Retrofit because default Retrofit has already been created.\n" +
                                "Hint: Call setRetrofit() before any createNetApi() is used, preferably in Application.onCreate()."
                );
            }

            // 安全起见：也检查是否有 API 实例已被创建（双重保险）
            if (!apiCache.isEmpty()) {
                throw new IllegalStateException(
                        "NetApiManager: Cannot set custom Retrofit because some API instances have already been created.\n" +
                                "Please call setRetrofit() before using any API."
                );
            }

            NetApiManager.retrofit = retrofit;
            isDefaultRetrofitCreated = true; // 标记已初始化（防止后续创建默认）
        }
    }

    /**
     * 创建或获取 API 接口实例（自动缓存）
     */
    public static <T> T createNetApi(Class<T> apiClass) {
        if (apiClass == null) {
            throw new IllegalArgumentException("API interface class cannot be null");
        }

        // 触发默认初始化（如果未设置）
        Retrofit currentRetrofit = getRetrofit();
        if (currentRetrofit == null) {
            throw new IllegalStateException("Retrofit initialization failed.");
        }

        @SuppressWarnings("unchecked")
        T existing = (T) apiCache.get(apiClass);
        if (existing != null) {
            return existing;
        }

        synchronized (cacheLock) {
            @SuppressWarnings("unchecked")
            T instance = (T) apiCache.get(apiClass);
            if (instance == null) {
                instance = currentRetrofit.create(apiClass);
                apiCache.put(apiClass, instance);
            }
            return instance;
        }
    }

    /**
     * 清除某个 API 缓存
     */
    public static void clear(Class<?> apiClass) {
        apiCache.remove(apiClass);
    }

    /**
     * 清除所有 API 缓存
     */
    public static void clearAll() {
        apiCache.clear();
    }

    /**
     * 是否已由用户设置 Retrofit
     */
    public static boolean isCustomRetrofitSet() {
        return isDefaultRetrofitCreated && retrofit != null;
    }

}