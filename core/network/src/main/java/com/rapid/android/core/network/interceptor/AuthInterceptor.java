package com.rapid.android.core.network.interceptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.rapid.android.core.network.base.BaseResponse;
import com.rapid.android.core.network.client.NetworkConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.*;

/**
 * 针对鉴权的拦截器：支持 HTTP 状态码与业务错误码两层兜底，并在需要时触发 Token 刷新。
 */
public class AuthInterceptor implements Interceptor {

    private static final Type BASE_RESPONSE_TYPE = new TypeToken<BaseResponse<Object>>() {}.getType();

    private final NetworkConfig.AuthFailureListener authFailureListener;
    private final Set<Integer> unauthorizedCodes;
    private final Set<Integer> businessUnauthorizedCodes;
    private final TokenRefreshHandler tokenRefreshHandler;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private final Object refreshLock = new Object();
    private final Gson gson = new Gson();
    private volatile boolean lastRefreshSuccessful = false;

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener) {
        this(listener, null, null, null);
    }

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                           @Nullable int[] customUnauthorizedCodes) {
        this(listener, customUnauthorizedCodes, null, null);
    }

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                           @Nullable int[] customUnauthorizedCodes,
                           @Nullable TokenRefreshHandler tokenRefreshHandler) {
        this(listener, customUnauthorizedCodes, tokenRefreshHandler, null);
    }

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                           @Nullable int[] customUnauthorizedCodes,
                           @Nullable TokenRefreshHandler tokenRefreshHandler,
                           @Nullable Set<Integer> businessUnauthorizedCodes) {
        this.authFailureListener = listener != null ? listener : () -> {};
        this.unauthorizedCodes = new HashSet<>();
        this.businessUnauthorizedCodes = businessUnauthorizedCodes != null
                ? new HashSet<>(businessUnauthorizedCodes)
                : new HashSet<>();

        this.unauthorizedCodes.add(401);
        this.unauthorizedCodes.add(403);
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

        BusinessCheckResult businessResult = evaluateBusinessUnauthorized(response);
        response = businessResult.response;

        if (!isUnauthorized(response.code()) && !businessResult.businessUnauthorized) {
            return response;
        }

        if (tokenRefreshHandler == null) {
            authFailureListener.onUnauthorized();
            return response;
        }

        boolean refreshResult = attemptTokenRefresh();

        if (refreshResult) {
            response.close();
            Request retryRequest = tokenRefreshHandler.rebuildRequest(request);
            if (retryRequest == null) {
                retryRequest = request.newBuilder().build();
            }

            Response retriedResponse = chain.proceed(retryRequest);
            BusinessCheckResult retryBusinessResult = evaluateBusinessUnauthorized(retriedResponse);
            retriedResponse = retryBusinessResult.response;
            if (!isUnauthorized(retriedResponse.code()) && !retryBusinessResult.businessUnauthorized) {
                return retriedResponse;
            }

            authFailureListener.onUnauthorized();
            return retriedResponse;
        }

        authFailureListener.onUnauthorized();
        return response;
    }

    private boolean isUnauthorized(int code) {
        return unauthorizedCodes.contains(code);
    }

    private boolean attemptTokenRefresh() {
        if (tokenRefreshHandler == null) {
            return false;
        }

        if (isRefreshing.compareAndSet(false, true)) {
            boolean success = false;
            try {
                if (tokenRefreshHandler.canRefresh()) {
                    success = tokenRefreshHandler.refreshToken();
                }
            } catch (Exception ignored) {
                success = false;
            } finally {
                synchronized (refreshLock) {
                    lastRefreshSuccessful = success;
                    isRefreshing.set(false);
                    refreshLock.notifyAll();
                }
            }
            return success;
        } else {
            synchronized (refreshLock) {
                while (isRefreshing.get()) {
                    try {
                        refreshLock.wait(TimeUnit.SECONDS.toMillis(2));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                return lastRefreshSuccessful;
            }
        }
    }

    private BusinessCheckResult evaluateBusinessUnauthorized(@NonNull Response response) {
        if (businessUnauthorizedCodes.isEmpty()) {
            return new BusinessCheckResult(response, false);
        }

        ResponseBody body = response.body();
        if (body == null) {
            return new BusinessCheckResult(response, false);
        }

        MediaType contentType = body.contentType();
        String subType = contentType != null ? contentType.subtype() : null;
        if (subType == null || !subType.toLowerCase().contains("json")) {
            return new BusinessCheckResult(response, false);
        }

        String bodyString;
        try {
            bodyString = body.string();
        } catch (IOException e) {
            return new BusinessCheckResult(response, false);
        }

        boolean unauthorized = false;
        try {
            BaseResponse<?> baseResponse = gson.fromJson(bodyString, BASE_RESPONSE_TYPE);
            if (baseResponse != null
                    && businessUnauthorizedCodes.contains(baseResponse.getErrorCode())
                    && !baseResponse.isSuccess()) {
                unauthorized = true;
            }
        } catch (JsonSyntaxException ignored) {
            // 非标准结构，直接忽略
        }

        ResponseBody newBody = ResponseBody.create(bodyString, contentType);
        Response rebuilt = response.newBuilder().body(newBody).build();
        return new BusinessCheckResult(rebuilt, unauthorized);
    }

    /**
     * Token 刷新处理器接口。
     */
    public interface TokenRefreshHandler {
        boolean canRefresh();

        boolean refreshToken() throws Exception;

        @Nullable
        default Request rebuildRequest(@NonNull Request original) {
            return original;
        }
    }

    private static final class BusinessCheckResult {
        final Response response;
        final boolean businessUnauthorized;

        BusinessCheckResult(Response response, boolean businessUnauthorized) {
            this.response = response;
            this.businessUnauthorized = businessUnauthorized;
        }
    }
}
