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

import okhttp3.*;

/**
 * 针对鉴权的拦截器：支持 HTTP 状态码与业务错误码两层兜底，并在需要时触发 Token 刷新。
 */
public class AuthInterceptor implements Interceptor {

    private static final Type BASE_RESPONSE_TYPE = new TypeToken<BaseResponse<Object>>() {}.getType();

    private static final String BUSINESS_RETRY_HEADER = "X-Auth-Retry";

    private final NetworkConfig.AuthFailureListener authFailureListener;
    private final Set<Integer> unauthorizedCodes;
    private final Set<Integer> businessUnauthorizedCodes;
    private final Gson gson = new Gson();
    private final TokenRefreshHandler tokenRefreshHandler;

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener) {
        this(listener, null, null, null);
    }

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                           @Nullable int[] customUnauthorizedCodes) {
        this(listener, customUnauthorizedCodes, null, null);
    }

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                           @Nullable int[] customUnauthorizedCodes,
                           @Nullable Set<Integer> businessUnauthorizedCodes) {
        this(listener, customUnauthorizedCodes, businessUnauthorizedCodes, null);
    }

    public AuthInterceptor(@Nullable NetworkConfig.AuthFailureListener listener,
                           @Nullable int[] customUnauthorizedCodes,
                           @Nullable Set<Integer> businessUnauthorizedCodes,
                           @Nullable TokenRefreshHandler tokenRefreshHandler) {
        this.authFailureListener = listener != null ? listener : () -> {};
        this.unauthorizedCodes = new HashSet<>();
        this.businessUnauthorizedCodes = businessUnauthorizedCodes != null
                ? new HashSet<>(businessUnauthorizedCodes)
                : new HashSet<>();
        this.tokenRefreshHandler = tokenRefreshHandler;

        this.unauthorizedCodes.add(401);
        this.unauthorizedCodes.add(403);
        if (customUnauthorizedCodes != null) {
            for (int code : customUnauthorizedCodes) {
                this.unauthorizedCodes.add(code);
            }
        }

        // Token 刷新由 TokenAuthenticator 处理
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        boolean alreadyRetried = request.header(BUSINESS_RETRY_HEADER) != null;
        Response response = chain.proceed(request);

        BusinessCheckResult businessResult = evaluateBusinessUnauthorized(response);
        response = businessResult.response;

        if (!isUnauthorized(response.code()) && !businessResult.businessUnauthorized) {
            return response;
        }

        if (businessResult.businessUnauthorized) {
            if (!alreadyRetried) {
                Response retry = attemptBusinessRefresh(chain, request, response);
                if (retry != null) {
                    return retry;
                }
            }
            authFailureListener.onUnauthorized();
        }
        return response;
    }

    @Nullable
    private Response attemptBusinessRefresh(@NonNull Chain chain,
                                            @NonNull Request originalRequest,
                                            @NonNull Response originalResponse) throws IOException {
        if (tokenRefreshHandler == null || !tokenRefreshHandler.canRefresh()) {
            return null;
        }

        try {
            boolean refreshed = tokenRefreshHandler.refreshToken();
            if (!refreshed) {
                return null;
            }

            Request rebuilt = tokenRefreshHandler.rebuildRequest(originalRequest);
            if (rebuilt == null) {
                rebuilt = originalRequest;
            }

            Request retryRequest = rebuilt.newBuilder()
                    .header(BUSINESS_RETRY_HEADER, "1")
                    .build();

            originalResponse.close();
            return chain.proceed(retryRequest);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isUnauthorized(int code) {
        return unauthorizedCodes.contains(code);
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
