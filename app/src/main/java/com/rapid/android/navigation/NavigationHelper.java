package com.rapid.android.navigation;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.navigation.Router;

/**
 * 路由辅助类
 * 提供常用页面跳转的便捷方法
 */
public final class NavigationHelper {

    private NavigationHelper() {
    }

    /**
     * 跳转到搜索页
     */
    public static void navigateToSearch(@NonNull Context context) {
        Router.getInstance().navigate(context, AppRouter.PATH_SEARCH);
    }

    /**
     * 跳转到设置页
     */
    public static void navigateToSettings(@NonNull Context context) {
        Router.getInstance().navigate(context, AppRouter.PATH_SETTINGS);
    }

    /**
     * 跳转到 WebView 页
     */
    public static void navigateToWebView(@NonNull Context context,
                                         @NonNull String url) {
        navigateToWebView(context, url, null);
    }

    /**
     * 跳转到 WebView 页（带标题）
     */
    public static void navigateToWebView(@NonNull Context context,
                                         @NonNull String url,
                                         @Nullable String title) {
        Bundle extras = new Bundle();
        extras.putString(AppRouter.PARAM_URL, url);
        if (title != null) {
            extras.putString(AppRouter.PARAM_TITLE, title);
        }
        Router.getInstance().navigate(context, AppRouter.PATH_WEBVIEW, extras);
    }
}
