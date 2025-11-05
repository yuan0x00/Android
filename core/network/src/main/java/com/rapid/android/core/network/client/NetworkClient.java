package com.rapid.android.core.network.client;

import androidx.annotation.NonNull;

import com.rapid.android.core.network.interceptor.LoggingInterceptor;
import com.rapid.android.core.network.util.SslManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    private final Retrofit retrofit;
    private final OkHttpClient.Builder currentOkHttpBuilder;
    private final Retrofit.Builder currentRetrofitBuilder;
    private final String tag;

    private NetworkClient(Builder builder) {
        this.tag = builder.tag;

        OkHttpClient.Builder okHttpBuilder = builder.okHttpBuilder != null
                ? builder.okHttpBuilder
                : getDefaultOkHttpBuilder();

        Retrofit.Builder retrofitBuilder = builder.retrofitBuilder != null
                ? builder.retrofitBuilder
                : getDefaultRetrofitBuilder();

        retrofitBuilder.baseUrl(builder.baseUrl);

        this.currentOkHttpBuilder = okHttpBuilder;
        this.currentRetrofitBuilder = retrofitBuilder;

        OkHttpClient okHttpClient = okHttpBuilder.build();
        this.retrofit = retrofitBuilder
                .client(okHttpClient)
                .build();
    }

    // ========== 静态工具方法 ==========

    @NonNull
    public static OkHttpClient.Builder getDefaultOkHttpBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .sslSocketFactory(
                        SslManager.getSSLSocketFactory(),
                        SslManager.getTrustManager()[0]
                )
                .hostnameVerifier(SslManager.getHostnameVerifier());
    }

    @NonNull
    public static Retrofit.Builder getDefaultRetrofitBuilder() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create());
    }

    // ========== 公共 API ==========

    public String getTag() {
        return tag;
    }

    @NonNull
    public Retrofit getRetrofit() {
        return retrofit;
    }

    @NonNull
    public <T> T createService(@NonNull Class<T> service) {
        return retrofit.create(service);
    }

    @NonNull
    public OkHttpClient.Builder getCurrentOkHttpBuilder() {
        return currentOkHttpBuilder;
    }

    @NonNull
    public Retrofit.Builder getCurrentRetrofitBuilder() {
        return currentRetrofitBuilder;
    }

    // ========== Builder ==========

    public static class Builder {
        private final String baseUrl;
        private String tag;
        private OkHttpClient.Builder okHttpBuilder;
        private Retrofit.Builder retrofitBuilder;

        public Builder(@NonNull String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder okHttpBuilder(OkHttpClient.Builder okHttpBuilder) {
            this.okHttpBuilder = okHttpBuilder;
            return this;
        }

        public Builder retrofitBuilder(Retrofit.Builder retrofitBuilder) {
            this.retrofitBuilder = retrofitBuilder;
            return this;
        }

        public NetworkClient build() {
            return new NetworkClient(this);
        }
    }
}