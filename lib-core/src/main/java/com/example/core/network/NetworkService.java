package com.example.core.network;

import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;

public class NetworkService {

    private static final Retrofit retrofit = RetrofitBuilder.getInstance().getRetrofit();

    // 缓存已创建的 service 实例
    private static final ConcurrentHashMap<Class<?>, Object> serviceCache = new ConcurrentHashMap<>();

    public static <T> T createService(Class<T> service) {
        // 先尝试从缓存获取
        @SuppressWarnings("unchecked")
        T existing = (T) serviceCache.get(service);
        if (existing != null) {
            return existing;
        }

        // 同步块内再次检查（防止并发重复创建）
        synchronized (serviceCache) {
            @SuppressWarnings("unchecked")
            T instance = (T) serviceCache.get(service);
            if (instance == null) {
                instance = retrofit.create(service);
                serviceCache.put(service, instance);
            }
            return instance;
        }
    }

    /**
     * （可选）清除某个 service 缓存，用于切换环境、登出等场景
     */
    public static void clearService(Class<?> serviceClass) {
        serviceCache.remove(serviceClass);
    }

    /**
     * （可选）清除所有 service 缓存
     */
    public static void clearAllServices() {
        serviceCache.clear();
    }
}