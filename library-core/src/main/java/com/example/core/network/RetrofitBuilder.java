package com.example.core.network;

import androidx.annotation.NonNull;

import com.example.core.base.BaseResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitBuilder {

    private static final String BASE_URL = "https://www.wanandroid.com/";
    private static final long CONNECT_TIMEOUT_SECONDS = 10L;
    private static final long READ_TIMEOUT_SECONDS = 10L;
    private static RetrofitBuilder instance;
    private final Retrofit retrofit;

    private RetrofitBuilder() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .sslSocketFactory(
                        SSLSocketManager.getSSLSocketFactory(),
                        SSLSocketManager.getTrustManager()[0]
                )
                .hostnameVerifier(SSLSocketManager.getHostnameVerifier())
                .addInterceptor(new ResponseHeaderInterceptor())
                .addInterceptor(new RequestHeaderInterceptor())
                .addInterceptor(new ResponseErrorInterceptor())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static synchronized RetrofitBuilder getInstance() {
        if (instance == null) {
            instance = new RetrofitBuilder();
        }
        return instance;
    }

    /**
     * 构造错误响应（返回 200 状态码，但 body 中包含错误信息）
     */
    private static Response buildErrorResponse(Request request, int errorCode, String errorMsg) {
        BaseResponse<?> errorResponse = new BaseResponse<>();
        errorResponse.setErrorCode(errorCode);
        errorResponse.setErrorMsg(errorMsg);
        errorResponse.setData(null);

        Gson gson = new Gson();
        String json = gson.toJson(errorResponse);
        MediaType mediaType = MediaType.get("application/json; charset=UTF-8");
        ResponseBody body = ResponseBody.create(json, mediaType);

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * 请求头拦截器：添加 Cookie
     */
    public static class RequestHeaderInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // 这里假设 DataStoreHelper 提供同步方法（如 shared preferences 封装）
//            Set<String> cookies = DataStoreHelper.getStringSetSync(LOCAL_TOKEN);
//            if (cookies != null) {
//                for (String cookie : cookies) {
//                    request.addHeader("Cookie", cookie);
//                }
//            }

            return chain.proceed(request.newBuilder().build());
        }
    }

    /**
     * 响应头拦截器：持久化 Set-Cookie
     */
    public static class ResponseHeaderInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            String requestUrl = request.url().toString();
            if (requestUrl.contains("/user/login")) {
//                Set<String> cookieSet = new HashSet<>(response.headers("Set-Cookie"));
            }
            return response;
        }
    }

    /**
     * 响应错误拦截器：统一错误处理
     */
    public static class ResponseErrorInterceptor implements Interceptor {
        @NonNull
        @Override
        public Response intercept(Chain chain) {
            Request request = chain.request();
            try {
                Response response = chain.proceed(request);
                if (response.isSuccessful()) {
                    return response;
                } else {
                    return buildErrorResponse(request, -100, "请求失败");
                }
            } catch (Exception e) {
                return buildErrorResponse(request, -200, "请求异常: " + e.getMessage());
            }
        }
    }
}