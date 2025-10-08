package com.rapid.android.core.webview.download;

import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    private final Map<Long, DownloadRequest> activeDownloads = new HashMap<>();
    private final Map<String, Long> urlToDownloadId = new HashMap<>();
    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                return;
            }
            long downloadId = intent.getLongExtra(android.app.DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
            if (downloadId == -1L) {
                return;
            }
            handleSystemDownloadComplete(downloadId);
        }
    };

    private DownloadListener listener;
    private String downloadPath = Environment.DIRECTORY_DOWNLOADS;
    private boolean allowOverwrite = true;

    public DownloadManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.downloadManager = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        IntentFilter filter = new IntentFilter(android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.context.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            ContextCompat.registerReceiver(this.context, downloadReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        }
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

            Long existingId = urlToDownloadId.remove(request.url);
            if (existingId != null) {
                DownloadRequest existing = activeDownloads.remove(existingId);
                if (existing != null && existing.downloadId != -1) {
                    downloadManager.remove(existing.downloadId);
                }
            }

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
            activeDownloads.put(downloadId, request);
            urlToDownloadId.put(request.url, downloadId);

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
        Long downloadId = urlToDownloadId.remove(url);
        if (downloadId != null) {
            DownloadRequest request = activeDownloads.remove(downloadId);
            if (request != null && request.downloadId != -1) {
                downloadManager.remove(request.downloadId);
                Log.d(TAG, "Cancelled download: " + url);
            }
        }
    }

    /**
     * 取消所有下载
     */
    public void cancelAllDownloads() {
        for (Long downloadId : activeDownloads.keySet()) {
            downloadManager.remove(downloadId);
        }
        activeDownloads.clear();
        urlToDownloadId.clear();
        Log.d(TAG, "Cancelled all downloads");
    }

    /**
     * 获取活跃下载数量
     */
    public int getActiveDownloadCount() {
        return activeDownloads.size();
    }

    /**
     * 处理下载任务结果，可直接供外部调用
     */
    public void handleDownloadResult(long downloadId, boolean success, @Nullable String filePath,
                                     @Nullable String errorMessage) {
        DownloadRequest request = activeDownloads.remove(downloadId);
        if (request != null) {
            urlToDownloadId.remove(request.url);
            if (listener != null) {
                if (success) {
                    listener.onDownloadCompleted(request, true, filePath);
                } else {
                    String reason = errorMessage != null ? errorMessage : "Download failed";
                    listener.onDownloadFailed(request, reason);
                }
            }
        }
    }

    private void handleSystemDownloadComplete(long downloadId) {
        DownloadRequest request = activeDownloads.get(downloadId);
        if (request == null) {
            return;
        }
        android.app.DownloadManager.Query query = new android.app.DownloadManager.Query().setFilterById(downloadId);
        android.database.Cursor cursor = null;
        boolean success = false;
        String filePath = null;
        String errorMessage = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
                int status = statusIndex != -1 ? cursor.getInt(statusIndex) : android.app.DownloadManager.STATUS_FAILED;
                success = status == android.app.DownloadManager.STATUS_SUCCESSFUL;
                if (success) {
                    int uriIndex = cursor.getColumnIndex(android.app.DownloadManager.COLUMN_LOCAL_URI);
                    if (uriIndex != -1) {
                        String uriString = cursor.getString(uriIndex);
                        if (uriString != null) {
                            filePath = Uri.parse(uriString).getPath();
                        }
                    }
                } else {
                    int reasonIndex = cursor.getColumnIndex(android.app.DownloadManager.COLUMN_REASON);
                    if (reasonIndex != -1) {
                        errorMessage = "code=" + cursor.getInt(reasonIndex);
                    }
                }
            }
        } catch (Exception e) {
            errorMessage = "query_failed:" + e.getMessage();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        handleDownloadResult(downloadId, success, filePath, errorMessage);
    }

    public void release() {
        try {
            context.unregisterReceiver(downloadReceiver);
        } catch (IllegalArgumentException ignore) {
            // receiver already unregistered
        }
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
