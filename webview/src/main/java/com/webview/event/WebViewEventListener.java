package com.webview.event;

import androidx.annotation.NonNull;

/**
 * WebView事件监听器接口
 * 统一的现代化事件监听接口
 */
public interface WebViewEventListener {

    /**
     * 页面开始加载
     * @param url 加载的URL
     */
    default void onPageStarted(@NonNull String url) {}

    /**
     * 页面加载完成
     * @param url 加载完成的URL
     */
    default void onPageFinished(@NonNull String url) {}

    /**
     * 加载进度改变
     * @param progress 进度值 (0-100)
     */
    default void onProgressChanged(int progress) {}

    /**
     * 页面标题改变
     * @param title 新的标题
     */
    default void onTitleChanged(@NonNull String title) {}

    /**
     * 收到错误
     * @param errorCode 错误码
     * @param description 错误描述
     * @param failingUrl 出错的URL
     */
    default void onReceivedError(int errorCode, @NonNull String description, @NonNull String failingUrl) {}

    /**
     * URL加载被拦截
     * @param url 被拦截的URL
     * @param reason 拦截原因
     */
    default void onUrlBlocked(@NonNull String url, @NonNull String reason) {}

    /**
     * JavaScript执行结果
     * @param result 执行结果
     */
    default void onJavaScriptResult(@NonNull String result) {}

    /**
     * 文件下载请求
     * @param url 下载URL
     * @param userAgent User-Agent
     * @param contentDisposition Content-Disposition
     * @param mimetype MIME类型
     * @param contentLength 内容长度
     */
    default void onDownloadRequested(@NonNull String url, String userAgent,
                                   String contentDisposition, String mimetype, long contentLength) {}

    /**
     * 权限请求
     * @param url 请求权限的URL
     * @param resources 请求的权限资源
     * @return 是否授予权限
     */
    default boolean onPermissionRequested(@NonNull String url, @NonNull String[] resources) {
        return false;
    }

    /**
     * SSL证书错误
     * @param url 出错的URL
     * @param error 证书错误信息
     * @return 是否继续加载
     */
    default boolean onSslError(@NonNull String url, @NonNull String error) {
        return false;
    }
}
