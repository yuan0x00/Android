package com.rapid.android.ui.feature.web;

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
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.components.dialog.DialogHost;
import com.rapid.android.core.ui.components.dialog.ScopedDialogHost;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.core.webview.WebView;
import com.rapid.android.core.webview.WebViewFragment;
import com.rapid.android.core.webview.config.DefaultWebViewConfig;
import com.rapid.android.core.webview.config.WebViewConfiguration;
import com.rapid.android.core.webview.event.WebViewEventListener;
import com.rapid.android.core.webview.utils.StatusBarUtils;
import com.rapid.android.ui.feature.login.LoginActivity;

import org.jetbrains.annotations.NotNull;

public class ArticleWebViewActivity extends AppCompatActivity implements DialogHost, ScopedDialogHost {

    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_CONFIG = "extra_config";
    private static final String EXTRA_ARTICLE_ID = "extra_article_id";
    private static final String EXTRA_ARTICLE_COLLECTED = "extra_article_collected";

    private WebViewFragment webViewFragment;
    private Toolbar toolbar;
    private MenuItem backMenuItem;
    private MenuItem forwardMenuItem;
    private MenuItem reloadMenuItem;
    private MenuItem collectMenuItem;

    private ArticleWebViewViewModel viewModel;
    private int articleId;
    private DialogController dialogController;

    public static void start(@NonNull Context context, @NonNull ArticleListBean.Data data) {
        Intent intent = new Intent(context, ArticleWebViewActivity.class);
        intent.putExtra(EXTRA_URL, data.getLink());
        intent.putExtra(EXTRA_TITLE, data.getTitle());
        intent.putExtra(EXTRA_ARTICLE_ID, data.getId());
        intent.putExtra(EXTRA_ARTICLE_COLLECTED, data.isCollect());
        context.startActivity(intent);
    }

    public static void start(@NonNull Context context, @NonNull String url, @Nullable String title) {
        Intent intent = new Intent(context, ArticleWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        if (title != null) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        intent.putExtra(EXTRA_ARTICLE_ID, -1);
        intent.putExtra(EXTRA_ARTICLE_COLLECTED, false);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.rapid.android.core.webview.R.layout.activity_webview);
        dialogController = DialogController.from(this, findViewById(android.R.id.content));

        viewModel = new ViewModelProvider(this).get(ArticleWebViewViewModel.class);

        StatusBarUtils.setStatusBarIconDark(this, true);
        setupToolbar();
        setupWebViewFragment();
        articleId = getIntent().getIntExtra(EXTRA_ARTICLE_ID, -1);
        boolean collected = getIntent().getBooleanExtra(EXTRA_ARTICLE_COLLECTED, false);
        viewModel.init(articleId, collected);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getCollectState().observe(this, state -> invalidateOptionsMenu());
        viewModel.getToastMessage().observe(this, this::showShortToast);
        viewModel.getLoading().observe(this, loading -> {
            if (collectMenuItem != null) {
                collectMenuItem.setEnabled(!Boolean.TRUE.equals(loading));
            }
        });
    }

    private void setupToolbar() {
        toolbar = findViewById(com.rapid.android.core.webview.R.id.toolbar);
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
            public void onTitleChanged(@NonNull String title) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null && getIntent().getStringExtra(EXTRA_TITLE) == null) {
                    actionBar.setTitle(title);
                }
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.rapid.android.core.webview.R.id.fragment_container, webViewFragment)
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
        getMenuInflater().inflate(com.rapid.android.core.webview.R.menu.menu_webview, menu);
        getMenuInflater().inflate(R.menu.menu_article_webview, menu);
        backMenuItem = menu.findItem(com.rapid.android.core.webview.R.id.action_back);
        forwardMenuItem = menu.findItem(com.rapid.android.core.webview.R.id.action_forward);
        reloadMenuItem = menu.findItem(com.rapid.android.core.webview.R.id.action_reload);
        collectMenuItem = menu.findItem(R.id.action_collect_toggle);
        updateNavigationMenu();
        updateCollectMenu();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateCollectMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    private void updateCollectMenu() {
        if (collectMenuItem == null) {
            return;
        }
        boolean collected = Boolean.TRUE.equals(viewModel.getCollectState().getValue());
        collectMenuItem.setIcon(collected ? R.drawable.bookmark_fill_24px : R.drawable.bookmark_24px);
        collectMenuItem.setTitle(collected ? R.string.article_uncollect : R.string.article_collect);
        collectMenuItem.setVisible(articleId > 0);
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

        if (id == com.rapid.android.core.webview.R.id.action_back && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else if (id == com.rapid.android.core.webview.R.id.action_forward && webView.canGoForward()) {
            webView.goForward();
            return true;
        } else if (id == com.rapid.android.core.webview.R.id.action_reload) {
            webView.reload();
            return true;
        } else if (id == R.id.action_collect_toggle) {
            handleCollectToggle();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleCollectToggle() {
        if (articleId <= 0) {
            return;
        }
        if (!SessionManager.getInstance().isLoggedIn()) {
            showShortToast(getString(R.string.article_collect_need_login));
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        viewModel.toggleCollect();
    }

    @Override
    protected void onDestroy() {
        dialogController = null;
        super.onDestroy();
    }

    @Override
    public @NotNull DialogController getDialogController() {
        if (dialogController == null) {
            throw new IllegalStateException("DialogController is not available after destruction.");
        }
        return dialogController;
    }

    @Override
    public @NotNull DialogController provideDialogController() {
        return getDialogController();
    }

    private void showShortToast(String message) {
        ToastUtils.showShortToast(getDialogController(), message);
    }
}
