package com.rapid.android.core.network.interceptor;

import androidx.annotation.Nullable;

import com.rapid.android.core.network.client.NetworkConfig;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * 使用 OkHttp {@link Authenticator} 统一调度 Token 刷新，避免在拦截器链上阻塞线程。
 */
public class TokenAuthenticator implements Authenticator {

    private final NetworkConfig config;

    public TokenAuthenticator(NetworkConfig config) {
        this.config = config;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, Response response) throws IOException {
        if (response == null) {
            return null;
        }

        if (responseCount(response) >= 2) {
            config.getAuthFailureListener().onUnauthorized();
            return null;
        }

        AuthInterceptor.TokenRefreshHandler handler = config.getTokenRefreshHandler();
        if (handler == null || !handler.canRefresh()) {
            config.getAuthFailureListener().onUnauthorized();
            return null;
        }

        try {
            boolean refreshed = handler.refreshToken();
            if (!refreshed) {
                config.getAuthFailureListener().onUnauthorized();
                return null;
            }
            Request rebuilt = handler.rebuildRequest(response.request());
            if (rebuilt == null) {
                rebuilt = response.request().newBuilder().build();
            }
            return rebuilt;
        } catch (Exception e) {
            config.getAuthFailureListener().onUnauthorized();
            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
