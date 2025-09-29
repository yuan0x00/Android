package com.core.net.interceptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.net.client.CoreRetrofitConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 针对鉴权的拦截器：当服务端返回未授权状态码时，通知业务方处理（如跳转登录）。
 */
public class AuthInterceptor implements Interceptor {

    private final CoreRetrofitConfig.AuthFailureListener authFailureListener;

    public AuthInterceptor(@Nullable CoreRetrofitConfig.AuthFailureListener listener) {
        this.authFailureListener = listener != null ? listener : () -> {};
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (isUnauthorized(response.code())) {
            authFailureListener.onUnauthorized();
        }
        return response;
    }

    private boolean isUnauthorized(int code) {
        return code == 401 || code == 403;
    }
}
