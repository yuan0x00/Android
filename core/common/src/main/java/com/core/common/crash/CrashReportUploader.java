package com.core.common.crash;

import android.util.Log;

import androidx.annotation.Nullable;

import com.core.log.LogKit;
import com.core.network.client.NetworkClient;
import com.core.network.client.NetworkConfig;
import com.core.network.client.SSLTrustManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

/**
 * 崩溃日志上传器：将本地日志文件和堆栈信息上传至服务端。
 */
final class CrashReportUploader {

    private static final String TAG = "CrashUploader";
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=UTF-8");

    private CrashReportUploader() {
    }

    static void uploadAsync(@Nullable File crashFile, @Nullable Throwable throwable) {
        String endpoint = NetworkClient.getActiveConfig().getCrashReportEndpoint();
        if (endpoint == null || endpoint.isEmpty()) {
            LogKit.w(TAG, "Crash upload skipped: endpoint is empty");
            return;
        }
        new Thread(() -> performUpload(endpoint, crashFile, throwable), "CrashUploader").start();
    }

    private static void performUpload(String endpoint, @Nullable File crashFile, @Nullable Throwable throwable) {
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
                LogKit.d(TAG, "Crash upload success, code=%d", response.code());
            }
        } catch (IOException e) {
            LogKit.e(TAG, e, "Crash upload request error");
        }
    }

    private static OkHttpClient buildClient() {
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

    private static RequestBody buildRequestBody(@Nullable File crashFile, @Nullable Throwable throwable) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", throwable != null && throwable.getMessage() != null
                        ? throwable.getMessage()
                        : "unknown");

        if (throwable != null) {
            builder.addFormDataPart("stacktrace", Log.getStackTraceString(throwable));
        }

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
