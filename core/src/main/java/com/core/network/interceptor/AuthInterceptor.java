package com.core.network.interceptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.network.client.NetworkConfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 针对鉴权的拦截器：当服务端返回未授权状态码时，通知业务方处理（如跳转登录）。
 * 支持配置未授权状态码和自动 token 刷新功能。
 */
public class AuthInterceptor implements Interceptor {

    private final NetworkConfig.AuthFailureListener authFailureListener;
    private final Set<Integer> unauthorizedCodes;
    private final TokenRefreshHandler tokenRefreshHandler;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener) {
        this(listener, null);
    }

    /**
     * 构造函数，支持配置未授权状态码
     */
    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener, @Nullable int[] customUnauthorizedCodes) {
        this.authFailureListener = listener != null ? listener : () -> {};
        this.unauthorizedCodes = new HashSet<>();
        
        // 默认未授权状态码
        this.unauthorizedCodes.add(401);
        this.unauthorizedCodes.add(403);
        
        // 添加自定义状态码
        if (customUnauthorizedCodes != null) {
            for (int code : customUnauthorizedCodes) {
                this.unauthorizedCodes.add(code);
            }
        }
        
        this.tokenRefreshHandler = null;
    }

    /**
     * 构造函数，支持配置未授权状态码和 token 刷新处理器
     */
    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                          @Nullable int[] customUnauthorizedCodes,
                          @Nullable TokenRefreshHandler tokenRefreshHandler) {
        this.authFailureListener = listener != null ? listener : () -> {};
        this.unauthorizedCodes = new HashSet<>();
        
        // 默认未授权状态码
        this.unauthorizedCodes.add(401);
        this.unauthorizedCodes.add(403);
        
        // 添加自定义状态码
        if (customUnauthorizedCodes != null) {
            for (int code : customUnauthorizedCodes) {
                this.unauthorizedCodes.add(code);
            }
        }
        
        this.tokenRefreshHandler = tokenRefreshHandler;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (isUnauthorized(response.code())) {
            // 尝试刷新 token（如果配置了刷新处理器）
            if (tokenRefreshHandler != null && !isRefreshing.get()) {
                synchronized (isRefreshing) {
                    if (!isRefreshing.get() && tokenRefreshHandler.canRefresh()) {
                        isRefreshing.set(true);
                        try {
                            if (tokenRefreshHandler.refreshToken()) {
                                // 刷新成功，重新发起原始请求
                                Response newResponse = chain.proceed(request);
                                if (!isUnauthorized(newResponse.code())) {
                                    isRefreshing.set(false);
                                    return newResponse;
                                }
                            }
                        } catch (Exception e) {
                            // 刷新失败，继续执行原来的未授权处理逻辑
                        } finally {
                            isRefreshing.set(false);
                        }
                    }
                }
            }
            
            // 通知未授权事件
            authFailureListener.onUnauthorized();
        }

        return response;
    }

    private boolean isUnauthorized(int code) {
        return unauthorizedCodes.contains(code);
    }

    /**
     * Token 刷新处理器接口
     */
    public interface TokenRefreshHandler {
        /**
         * 判断是否可以刷新 token
         */
        boolean canRefresh();

        /**
         * 刷新 token
         * @return 刷新是否成功
         */
        boolean refreshToken() throws Exception;
    }
}
