package com.core.webview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.core.R;
import com.core.webview.config.DefaultWebViewConfig;
import com.core.webview.config.WebViewConfiguration;
import com.core.webview.event.WebViewEventListener;
import com.core.webview.utils.StatusBarUtils;

/**
 * 现代化WebView Activity
 * 提供完整的WebView功能和现代化的用户体验
 */
public class WebViewActivity extends AppCompatActivity {
    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_CONFIG = "extra_config";

    private WebViewFragment webViewFragment;
    private Toolbar toolbar;
    private MenuItem backMenuItem;
    private MenuItem forwardMenuItem;
    private MenuItem reloadMenuItem;

    /**
     * 启动Activity并加载URL
     *
     * @param context 上下文
     * @param url     要加载的URL
     */
    public static void start(@NonNull Context context, @NonNull String url) {
        start(context, url, null, null);
    }

    /**
     * 启动Activity并加载URL
     *
     * @param context 上下文
     * @param url     要加载的URL
     * @param title   页面标题
     */
    public static void start(@NonNull Context context, @NonNull String url, @Nullable String title) {
        start(context, url, title, null);
    }

    /**
     * 启动Activity并加载URL
     *
     * @param context 上下文
     * @param url     要加载的URL
     * @param config  WebView配置
     */
    public static void start(@NonNull Context context, @NonNull String url,
                             @NonNull WebViewConfiguration config) {
        start(context, url, null, config);
    }

    /**
     * 启动Activity并加载URL
     *
     * @param context 上下文
     * @param url     要加载的URL
     * @param title   页面标题
     * @param config  WebView配置
     */
    public static void start(@NonNull Context context, @NonNull String url,
                             @Nullable String title, @Nullable WebViewConfiguration config) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        if (title != null) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        if (config != null && config instanceof DefaultWebViewConfig) {
            intent.putExtra(EXTRA_CONFIG, (DefaultWebViewConfig) config);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        StatusBarUtils.setStatusBarIconDark(this, true);
        setupToolbar();
        setupWebViewFragment();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);

            String title = getIntent().getStringExtra(EXTRA_TITLE);
            if (title != null) {
                actionBar.setTitle(title);
            }
        }
    }

    private void setupWebViewFragment() {
        String url = getIntent().getStringExtra(EXTRA_URL);
        DefaultWebViewConfig config = getIntent().getParcelableExtra(EXTRA_CONFIG);

        if (config == null) {
            config = new DefaultWebViewConfig.Builder().build();
        }

        webViewFragment = WebViewFragment.newInstance(url, (WebViewConfiguration) config);
        webViewFragment.setEventListener(new WebViewEventListener() {
            @Override
            public void onPageStarted(@NonNull String url) {
                updateNavigationMenu();
            }

            @Override
            public void onPageFinished(@NonNull String url) {
                updateNavigationMenu();
            }

            @Override
            public void onReceivedError(int errorCode, @NonNull String description,
                                        @NonNull String failingUrl) {
                updateNavigationMenu();
            }

            @Override
            public void onProgressChanged(int progress) {
                // 可以在这里添加进度显示逻辑
            }

            @Override
            public void onTitleChanged(@NonNull String title) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null && getIntent().getStringExtra(EXTRA_TITLE) == null) {
                    actionBar.setTitle(title);
                }
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, webViewFragment)
                .commit();
    }

    private void updateNavigationMenu() {
        WebView webView = webViewFragment.getWebView();
        if (webView == null) return;

        if (backMenuItem != null) {
            backMenuItem.setEnabled(webView.canGoBack());
        }
        if (forwardMenuItem != null) {
            forwardMenuItem.setEnabled(webView.canGoForward());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_webview, menu);
        backMenuItem = menu.findItem(R.id.action_back);
        forwardMenuItem = menu.findItem(R.id.action_forward);
        reloadMenuItem = menu.findItem(R.id.action_reload);
        updateNavigationMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        WebView webView = webViewFragment.getWebView();
        if (webView == null) return false;

        if (id == R.id.action_back && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else if (id == R.id.action_forward && webView.canGoForward()) {
            webView.goForward();
            return true;
        } else if (id == R.id.action_reload) {
            webView.reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        WebView webView = webViewFragment.getWebView();
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        // 清理WebViewFragment
        if (webViewFragment != null) {
            webViewFragment = null;
        }
        super.onDestroy();
    }
}
