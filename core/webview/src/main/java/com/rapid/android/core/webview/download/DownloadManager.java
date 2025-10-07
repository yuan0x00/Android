package com.rapid.android.core.webview.download;

import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * WebView文件下载管理器
 * 处理WebView中的文件下载请求
 */
public class DownloadManager {
    private static final String TAG = "WebViewDownload";

    private final Context context;
    private final android.app.DownloadManager downloadManager;
    private final Map<String, DownloadRequest> activeDownloads = new HashMap<>();

    private DownloadListener listener;
    private String downloadPath = Environment.DIRECTORY_DOWNLOADS;
    private boolean allowOverwrite = true;

    public DownloadManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * 设置下载监听器
     */
    public DownloadManager setDownloadListener(DownloadListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置下载路径
     * @param path 相对于外部存储器的路径
     */
    public DownloadManager setDownloadPath(@NonNull String path) {
        this.downloadPath = path;
        return this;
    }

    /**
     * 设置是否允许覆盖文件
     */
    public DownloadManager setAllowOverwrite(boolean allow) {
        this.allowOverwrite = allow;
        return this;
    }

    /**
     * 处理下载请求
     */
    public void handleDownloadRequest(@NonNull String url, @Nullable String userAgent,
                                    @Nullable String contentDisposition,
                                    @Nullable String mimetype, long contentLength) {
        DownloadRequest request = new DownloadRequest(url, userAgent, contentDisposition,
                                                    mimetype, contentLength);

        // 验证下载请求
        if (!validateDownloadRequest(request)) {
            if (listener != null) {
                listener.onDownloadFailed(request, "Download request validation failed");
            }
            return;
        }

        // 开始下载
        startDownload(request);
    }

    /**
     * 验证下载请求
     */
    private boolean validateDownloadRequest(DownloadRequest request) {
        // 检查URL
        if (request.url == null || request.url.trim().isEmpty()) {
            Log.w(TAG, "Invalid download URL");
            return false;
        }

        // 检查文件大小（可选的上限检查）
        if (request.contentLength > 0 && request.contentLength > 100 * 1024 * 1024) { // 100MB
            Log.w(TAG, "File too large: " + request.contentLength);
            return false;
        }

        return true;
    }

    /**
     * 开始下载
     */
    private void startDownload(DownloadRequest request) {
        try {
            Request downloadRequest = new Request(Uri.parse(request.url));

            // 设置请求头
            if (request.userAgent != null) {
                downloadRequest.addRequestHeader("User-Agent", request.userAgent);
            }

            // 设置文件信息
            String fileName = extractFileName(request);
            downloadRequest.setDestinationInExternalPublicDir(downloadPath, fileName);
            downloadRequest.setMimeType(request.mimetype);

            // 设置其他属性
            downloadRequest.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadRequest.setAllowedOverMetered(true);
            downloadRequest.setAllowedOverRoaming(true);

            if (!allowOverwrite) {
                downloadRequest.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
            }

            // 开始下载
            long downloadId = downloadManager.enqueue(downloadRequest);
            request.downloadId = downloadId;
            activeDownloads.put(request.url, request);

            Log.d(TAG, "Started download: " + request.url + " -> " + fileName);

            if (listener != null) {
                listener.onDownloadStarted(request);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error starting download: " + request.url, e);
            if (listener != null) {
                listener.onDownloadFailed(request, "Failed to start download: " + e.getMessage());
            }
        }
    }

    /**
     * 从Content-Disposition或URL中提取文件名
     */
    private String extractFileName(DownloadRequest request) {
        // 尝试从Content-Disposition提取
        if (request.contentDisposition != null) {
            String fileName = parseFileNameFromDisposition(request.contentDisposition);
            if (fileName != null) {
                return fileName;
            }
        }

        // 从URL提取
        try {
            Uri uri = Uri.parse(request.url);
            String path = uri.getLastPathSegment();
            if (path != null && !path.isEmpty()) {
                return path;
            }
        } catch (Exception e) {
            // 忽略解析错误
        }

        // 使用默认文件名
        return "download_" + System.currentTimeMillis();
    }

    /**
     * 从Content-Disposition解析文件名
     */
    @Nullable
    private String parseFileNameFromDisposition(String disposition) {
        if (disposition == null) {
            return null;
        }

        String[] parts = disposition.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String fileName = part.substring(9);
                if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }
                return fileName;
            }
        }
        return null;
    }

    /**
     * 取消下载
     */
    public void cancelDownload(String url) {
        DownloadRequest request = activeDownloads.get(url);
        if (request != null && request.downloadId != -1) {
            downloadManager.remove(request.downloadId);
            activeDownloads.remove(url);
            Log.d(TAG, "Cancelled download: " + url);
        }
    }

    /**
     * 取消所有下载
     */
    public void cancelAllDownloads() {
        for (DownloadRequest request : activeDownloads.values()) {
            if (request.downloadId != -1) {
                downloadManager.remove(request.downloadId);
            }
        }
        activeDownloads.clear();
        Log.d(TAG, "Cancelled all downloads");
    }

    /**
     * 获取活跃下载数量
     */
    public int getActiveDownloadCount() {
        return activeDownloads.size();
    }

    public interface DownloadListener {
        /**
         * 下载开始
         * @param request 下载请求
         */
        void onDownloadStarted(@NonNull DownloadRequest request);

        /**
         * 下载完成
         * @param request 下载请求
         * @param success 是否成功
         * @param filePath 文件路径（成功时）
         */
        void onDownloadCompleted(@NonNull DownloadRequest request, boolean success,
                               @Nullable String filePath);

        /**
         * 下载失败
         * @param request 下载请求
         * @param reason 失败原因
         */
        void onDownloadFailed(@NonNull DownloadRequest request, @NonNull String reason);

        /**
         * 下载进度更新
         * @param request 下载请求
         * @param bytesDownloaded 已下载字节数
         * @param bytesTotal 总字节数
         */
        void onDownloadProgress(@NonNull DownloadRequest request, long bytesDownloaded, long bytesTotal);
    }

    /**
     * 下载请求数据类
     */
    public static class DownloadRequest {
        public final String url;
        public final String userAgent;
        public final String contentDisposition;
        public final String mimetype;
        public final long contentLength;

        public long downloadId = -1;

        public DownloadRequest(String url, String userAgent, String contentDisposition,
                             String mimetype, long contentLength) {
            this.url = url;
            this.userAgent = userAgent;
            this.contentDisposition = contentDisposition;
            this.mimetype = mimetype;
            this.contentLength = contentLength;
        }

        @Override
        public String toString() {
            return "DownloadRequest{" +
                    "url='" + url + '\'' +
                    ", mimetype='" + mimetype + '\'' +
                    ", contentLength=" + contentLength +
                    '}';
        }
    }
}
