package com.rapid.android.init;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.common.crash.GlobalCrashHandler;
import com.rapid.android.core.log.LogKit;
import com.rapid.android.core.network.client.NetworkClient;
import com.rapid.android.core.network.client.NetworkConfig;
import com.rapid.android.core.network.client.SSLTrustManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

final class AppCrashReporter implements GlobalCrashHandler.CrashReporter {

    private static final String TAG = "CrashReporter";
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=UTF-8");

    @Override
    public void report(@Nullable File crashFile, @NonNull Throwable throwable) {
        String endpoint = NetworkClient.getActiveConfig().getCrashReportEndpoint();
        if (endpoint == null || endpoint.isEmpty()) {
            LogKit.w(TAG, "Crash upload skipped: endpoint empty");
            return;
        }
        new Thread(() -> performUpload(endpoint, crashFile, throwable), "CrashReporter").start();
    }

    private void performUpload(String endpoint, @Nullable File crashFile, @NonNull Throwable throwable) {
        OkHttpClient client = buildClient();
        RequestBody body = buildRequestBody(crashFile, throwable);
        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LogKit.w(TAG, "Crash upload failed: code=%d, message=%s", response.code(), response.message());
            } else {
                LogKit.d(TAG, "Crash upload success: code=%d", response.code());
            }
        } catch (IOException e) {
            LogKit.e(TAG, e, "Crash upload request error");
        }
    }

    private OkHttpClient buildClient() {
        NetworkConfig config = NetworkClient.getActiveConfig();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeoutSeconds(), TimeUnit.SECONDS);
        if (config.isAllowInsecureSsl()) {
            builder.sslSocketFactory(
                    SSLTrustManager.getSSLSocketFactory(),
                    SSLTrustManager.getTrustManager()[0]
            );
            builder.hostnameVerifier(SSLTrustManager.getHostnameVerifier());
        }
        return builder.build();
    }

    private RequestBody buildRequestBody(@Nullable File crashFile, @NonNull Throwable throwable) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", throwable.getMessage() != null ? throwable.getMessage() : "unknown")
                .addFormDataPart("stacktrace", Log.getStackTraceString(throwable));

        if (crashFile != null && crashFile.exists()) {
            builder.addFormDataPart(
                    "file",
                    crashFile.getName(),
                    RequestBody.create(MEDIA_TYPE_TEXT, crashFile)
            );
        }
        return builder.build();
    }
}
