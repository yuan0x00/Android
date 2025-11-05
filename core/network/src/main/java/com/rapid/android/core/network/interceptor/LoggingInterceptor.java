package com.rapid.android.core.network.interceptor;

import androidx.annotation.NonNull;

import com.rapid.android.core.log.LogKit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LoggingInterceptor implements Interceptor {

    private static final String TAG = "NetworkClient";

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startNs = System.nanoTime();
        LogKit.d(TAG, "→ %s %s", request.method(), request.url());
        Response response = chain.proceed(request);
        long costMs = (System.nanoTime() - startNs) / 1_000_000L;
        LogKit.d(TAG, "← %s %s code=%d (%dms)",
                request.method(),
                response.request().url(),
                response.code(),
                costMs);
        return response;
    }
}

