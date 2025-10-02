package com.core.network;

import androidx.annotation.NonNull;

import com.core.network.client.NetworkClient;

import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;

/**
 * 网络 API 管理器
 * - 支持业务方自定义传入 Retrofit
 * - 若未设置，则自动使用默认配置初始化（开箱即用）
 */
public class NetManager {

    private static final Object lock = new Object();
    private static final ConcurrentHashMap<Class<?>, Object> apiCache = new ConcurrentHashMap<>();
    private static volatile Retrofit retrofit;
    private static volatile boolean initialized = false;

    /**
     * 获取当前 Retrofit 实例（懒加载，自动创建默认实例）
     */
    @NonNull
    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (lock) {
                if (retrofit == null) {
                    retrofit = NetworkClient.getInstance().getRetrofit();
                    initialized = true;
                }
            }
        }
        return retrofit;
    }

    /**
     * 设置自定义 Retrofit 实例
     * 必须在任何 API 创建之前调用，建议在 Application.onCreate() 中调用
     *
     * @throws IllegalStateException 如果已经初始化或已有API实例被创建
     */
    public static void setRetrofit(@NonNull Retrofit customRetrofit) {
        if (customRetrofit == null) {
            throw new IllegalArgumentException("Retrofit cannot be null");
        }

        synchronized (lock) {
            if (initialized) {
                throw new IllegalStateException(
                        "Cannot set custom Retrofit: already initialized.\n" +
                                "Call setRetrofit() before any createNetApi() usage, preferably in Application.onCreate()."
                );
            }

            if (!apiCache.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot set custom Retrofit: API instances already created.\n" +
                                "Call setRetrofit() before using any API."
                );
            }

            retrofit = customRetrofit;
            initialized = true;
        }
    }

    /**
     * 创建或获取 API 接口实例（自动缓存，线程安全）
     */
    @NonNull
    public static <T> T createNetApi(@NonNull Class<T> apiClass) {
        if (apiClass == null) {
            throw new IllegalArgumentException("API interface class cannot be null");
        }

        @SuppressWarnings("unchecked")
        T existing = (T) apiCache.get(apiClass);
        if (existing != null) {
            return existing;
        }

        synchronized (lock) {
            @SuppressWarnings("unchecked")
            T instance = (T) apiCache.get(apiClass);
            if (instance == null) {
                instance = getRetrofit().create(apiClass);
                apiCache.put(apiClass, instance);
            }
            return instance;
        }
    }

    /**
     * 清除指定 API 缓存
     */
    public static void clearApi(@NonNull Class<?> apiClass) {
        apiCache.remove(apiClass);
    }

    /**
     * 清除所有 API 缓存
     */
    public static void clearAllApis() {
        apiCache.clear();
    }

    /**
     * 检查 Retrofit 是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }

}