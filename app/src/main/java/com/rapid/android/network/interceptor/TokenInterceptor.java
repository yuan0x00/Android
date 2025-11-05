package com.rapid.android.network.interceptor;

import com.rapid.android.core.storage.AuthStorage;

import org.jspecify.annotations.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private final AuthStorage authStorage;

    public TokenInterceptor() {
        authStorage = AuthStorage.getInstance();
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = authStorage.peekToken();

        // 如果 token 存在，添加到请求头
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        // 没有 token，直接执行请求
        return chain.proceed(originalRequest);
    }

}