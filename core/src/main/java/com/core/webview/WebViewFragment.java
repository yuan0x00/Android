package com.core.webview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.core.R;
import com.core.webview.config.DefaultWebViewConfig;
import com.core.webview.event.WebViewEventListener;

/**
 * 现代化WebView Fragment
 * 基于新的架构设计，提供更好的性能和用户体验
 */
public class WebViewFragment extends Fragment {
    private static final String ARG_URL = "arg_url";
    private static final String ARG_CONFIG = "arg_config";

    private WebView webView;
    private ProgressBar progressBar;
    private View errorView;
    private WebViewEventListener externalListener;

    /**
     * 创建Fragment实例
     * @param url 要加载的URL
     * @return Fragment实例
     */
    @NonNull
    public static WebViewFragment newInstance(@NonNull String url) {
        return newInstance(url, new DefaultWebViewConfig.Builder().build());
    }

    /**
     * 创建Fragment实例
     * @param url 要加载的URL
     * @param config WebView配置
     * @return Fragment实例
     */
    @NonNull
    public static WebViewFragment newInstance(@NonNull String url,
                                             @NonNull com.core.webview.config.WebViewConfiguration config) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        if (config instanceof DefaultWebViewConfig) {
            args.putParcelable(ARG_CONFIG, (DefaultWebViewConfig) config);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_URL);
            DefaultWebViewConfig config = getArguments().getParcelable(ARG_CONFIG);

            if (config == null) {
                config = new DefaultWebViewConfig.Builder().build();
            }

            // 创建WebView实例
            webView = WebView.with(requireContext(), getLifecycle())
                    .config(config)
                    .build();

            // 如果有URL，立即加载
            if (url != null && !url.isEmpty()) {
                webView.loadUrl(url);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_webview, container, false);

        // 初始化视图
        ViewGroup webViewContainer = rootView.findViewById(R.id.webview_container);
        progressBar = rootView.findViewById(R.id.progress_bar);
        errorView = rootView.findViewById(R.id.error_view);

        // 设置WebView
        if (this.webView != null && this.webView.getWebView() != null) {
            android.webkit.WebView actualWebView = this.webView.getWebView();

            // 将实际的WebView添加到容器中
            if (webViewContainer != null) {
                webViewContainer.addView(actualWebView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ));
                android.util.Log.d("WebViewFragment", "WebView added to container successfully");
            } else {
                android.util.Log.w("WebViewFragment", "WebView container is null");
            }
        } else {
            android.util.Log.w("WebViewFragment", "WebView or WebView.getWebView() is null - webView: " + this.webView + ", getWebView(): " + (this.webView != null ? this.webView.getWebView() : "null"));
        }

        return rootView;
    }

    /**
     * 设置外部事件监听器
     * @param listener 事件监听器
     */
    public void setEventListener(@Nullable WebViewEventListener listener) {
        this.externalListener = listener;
    }

    /**
     * 获取WebView实例
     * @return WebView实例
     */
    @Nullable
    public WebView getWebView() {
        return webView;
    }

    @Override
    public void onDestroy() {
        // 从父容器中移除WebView，防止内存泄露
        if (webView != null && webView.getWebView() != null) {
            android.webkit.WebView actualWebView = webView.getWebView();
            ViewGroup parent = (ViewGroup) actualWebView.getParent();
            if (parent != null) {
                parent.removeView(actualWebView);
                android.util.Log.d("WebViewFragment", "WebView removed from parent container");
            }
        }

        // 销毁WebView
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    /**
     * 内部事件监听器
     * 处理UI更新和外部回调转发
     */
    private class InternalEventListener implements WebViewEventListener {
        @Override
        public void onPageStarted(@NonNull String url) {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (errorView != null) {
                errorView.setVisibility(View.GONE);
            }
            if (externalListener != null) {
                externalListener.onPageStarted(url);
            }
        }

        @Override
        public void onPageFinished(@NonNull String url) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (externalListener != null) {
                externalListener.onPageFinished(url);
            }
        }

        @Override
        public void onProgressChanged(int progress) {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
            if (externalListener != null) {
                externalListener.onProgressChanged(progress);
            }
        }

        @Override
        public void onReceivedError(int errorCode, @NonNull String description, @NonNull String failingUrl) {
            if (errorView != null) {
                errorView.setVisibility(View.VISIBLE);
            }
            if (externalListener != null) {
                externalListener.onReceivedError(errorCode, description, failingUrl);
            }
        }

        @Override
        public void onTitleChanged(@NonNull String title) {
            if (externalListener != null) {
                externalListener.onTitleChanged(title);
            }
        }

        @Override
        public void onUrlBlocked(@NonNull String url, @NonNull String reason) {
            if (externalListener != null) {
                externalListener.onUrlBlocked(url, reason);
            }
        }

        @Override
        public void onDownloadRequested(@NonNull String url, String userAgent,
                                      String contentDisposition, String mimetype, long contentLength) {
            if (externalListener != null) {
                externalListener.onDownloadRequested(url, userAgent, contentDisposition,
                                                   mimetype, contentLength);
            }
        }
    }
}
