package com.rapid.android.core.network.client;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class NetworkClientManager {

    private static final String DEFAULT_TAG = "default";
    private static final Map<String, NetworkClient> clients = new ConcurrentHashMap<>();

    public static void initializeClient(String tag, String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://www.example.com/";
        }
        getOrCreate(tag, baseUrl);
    }

    public static void initializeDefaultClient(String baseUrl) {
        initializeClient(DEFAULT_TAG, baseUrl);
    }

    public static NetworkClient getDefaultClient() {
        return getClient(DEFAULT_TAG);
    }


    public static NetworkClient getClient(String tag) {
        NetworkClient client = clients.get(tag);
        if (client == null) {
            throw new IllegalArgumentException("NetworkClient with tag '" + tag + "' not found. Use getOrCreate() or reInitialize() first.");
        }
        return client;
    }

    public static NetworkClient getOrCreate(String tag, String baseUrl) {
        return clients.computeIfAbsent(tag, key ->
                new NetworkClient.Builder(baseUrl).tag(tag).build()
        );
    }

    public static NetworkClient getOrCreate(String tag, String baseUrl,
                                            OkHttpClient.Builder okHttpBuilder,
                                            Retrofit.Builder retrofitBuilder) {
        return clients.computeIfAbsent(tag, key ->
                new NetworkClient.Builder(baseUrl)
                        .tag(tag)
                        .okHttpBuilder(okHttpBuilder)
                        .retrofitBuilder(retrofitBuilder)
                        .build()
        );
    }

    /**
     * 重新初始化指定 tag 的客户端（完整配置）
     */
    public static void reInitialize(@NonNull String tag,
                                    @NonNull String baseUrl,
                                    @NonNull OkHttpClient.Builder okHttpBuilder,
                                    @NonNull Retrofit.Builder retrofitBuilder) {
        NetworkClient newClient = new NetworkClient.Builder(baseUrl)
                .tag(tag)
                .okHttpBuilder(okHttpBuilder)
                .retrofitBuilder(retrofitBuilder)
                .build();
        clients.put(tag, newClient);
    }

    /**
     * 重新初始化默认客户端（完整配置）
     */
    public static void reInitializeDefaultClient(@NonNull String baseUrl,
                                                 @NonNull OkHttpClient.Builder okHttpBuilder,
                                                 @NonNull Retrofit.Builder retrofitBuilder) {
        reInitialize(DEFAULT_TAG, baseUrl, okHttpBuilder, retrofitBuilder);
    }

    public static void remove(String tag) {
        clients.remove(tag);
    }

    public static Set<String> getAllTags() {
        return new HashSet<>(clients.keySet());
    }
}