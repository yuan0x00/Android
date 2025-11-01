package com.rapid.android.feature.web;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.compose.core.webview.ui.WebViewActivity;

public class ArticleWebViewUtil {

    public static void start(@NonNull Context context, @NonNull String url, @Nullable String title) {
        WebViewActivity.start(context, url, title);

    }

    public static void start(@NonNull Context context, ArticleListBean.Data data) {
        WebViewActivity.start(context, data.getLink(), data.getTitle());
    }
}