package com.core.network.client;

import androidx.annotation.NonNull;

import com.core.common.log.Logger;
import com.core.network.interceptor.AuthInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {

    private static volatile NetworkConfig config = NetworkConfig.defaultConfig();
    private static NetworkClient instance;
    private final Retrofit retrofit;

    private NetworkClient(NetworkConfig activeConfig) {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .connectTimeout(activeConfig.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(activeConfig.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(activeConfig.getWriteTimeoutSeconds(), TimeUnit.SECONDS);

        if (activeConfig.isAllowInsecureSsl()) {
            okHttpBuilder.sslSocketFactory(
                    SSLTrustManager.getSSLSocketFactory(),
                    SSLTrustManager.getTrustManager()[0]
            );
            okHttpBuilder.hostnameVerifier(SSLTrustManager.getHostnameVerifier());
        }

        okHttpBuilder.addInterceptor(new RequestHeaderInterceptor(activeConfig));
        okHttpBuilder.addInterceptor(new ResponseHeaderInterceptor(activeConfig));
        okHttpBuilder.addInterceptor(new AuthInterceptor(activeConfig.getAuthFailureListener()));
//        okHttpBuilder.addInterceptor(new ResponseErrorInterceptor());

        if (activeConfig.isLoggingEnabled()) {
            okHttpBuilder.addInterceptor(new LoggingInterceptor());
        }

        OkHttpClient okHttpClient = okHttpBuilder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(activeConfig.getBaseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient(config);
        }
        return instance;
    }

    public static synchronized void configure(@NonNull NetworkConfig newConfig) {
        config = newConfig;
        instance = null; // 配置更新后重新构建 Retrofit
    }

    @NonNull
    public static NetworkConfig getActiveConfig() {
        return config;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * 请求头拦截器：动态注入业务 Header 及 Cookie
     */
    public static class RequestHeaderInterceptor implements Interceptor {
        private final NetworkConfig config;

        public RequestHeaderInterceptor(NetworkConfig config) {
            this.config = config;
        }

        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder builder = original.newBuilder();

            Map<String, String> headers = config.getHeaderProvider().provideHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }

            List<String> cookies = config.getCookieStore().loadForRequest(original.url());
            if (!cookies.isEmpty()) {
                builder.header("Cookie", mergeCookies(cookies));
            }

            return chain.proceed(builder.build());
        }

        @NonNull
        private String mergeCookies(@NonNull List<String> cookies) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < cookies.size(); i++) {
                if (i > 0) {
                    builder.append("; ");
                }
                builder.append(cookies.get(i));
            }
            return builder.toString();
        }
    }

    /**
     * 响应头拦截器：持久化 Cookie
     */
    public static class ResponseHeaderInterceptor implements Interceptor {
        private final NetworkConfig config;

        public ResponseHeaderInterceptor(NetworkConfig config) {
            this.config = config;
        }

        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            List<String> cookies = response.headers("Set-Cookie");
            if (!cookies.isEmpty()) {
                HttpUrl url = response.request().url();
                config.getCookieStore().saveFromResponse(url, cookies);
            }
            return response;
        }
    }

    /**
     * 响应错误拦截器：记录异常，保留原始状态码
     */
    public static class ResponseErrorInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            try {
                Response response = chain.proceed(request);
                if (!response.isSuccessful()) {
                    Logger.w("HTTP %d %s", response.code(), response.request().url());
                }
                return response;
            } catch (IOException e) {
                Logger.e(e, "Network error: %s", request.url());
                throw e;
            } catch (Exception e) {
                Logger.e(e, "Unexpected network error: %s", request.url());
                throw new IOException("Unexpected network error", e);
            }
        }
    }

    private static class LoggingInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long startNs = System.nanoTime();
            Logger.d("➡️ %s %s", request.method(), request.url());
            Response response = chain.proceed(request);
            long costMs = (System.nanoTime() - startNs) / 1_000_000L;
            Logger.d("⬅️ %s %s code=%d (%dms)",
                    request.method(),
                    response.request().url(),
                    response.code(),
                    costMs);
            return response;
        }
    }
}
