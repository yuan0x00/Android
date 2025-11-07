package com.rapid.android.core.ui.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.rapid.android.core.analytics.tracker.Tracker;
import com.rapid.android.core.common.utils.WindowInsetsUtils;
import com.rapid.android.core.network.state.NetworkStateManager;
import com.rapid.android.core.ui.R;
import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.components.dialog.DialogHost;

import org.jetbrains.annotations.NotNull;

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewBinding> extends AppCompatActivity implements DialogHost {

    protected VM viewModel;
    protected VB binding;
    private DialogController dialogController;
    private ViewGroup container;

    private long pageCreateTime;    // 页面创建时间（可用于计算停留时长）

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pageCreateTime = System.currentTimeMillis();

        Tracker.trackPageCreate(this.getClass().getName());

        setContentView(R.layout.core_ui_activity_container);
        container = findViewById(R.id.container);

        if (shouldObserveNetworkState()) {
            getLifecycle().addObserver(NetworkStateManager.getInstance());
        }

        viewModel = createViewModel();

        setContentViewSync(getLayoutResId());
    }

    private void setContentViewSync(@LayoutRes int resid) {
        View view = LayoutInflater.from(this).inflate(resid, container, false);
        container.addView(view);
        binding = createViewBinding(view);
        initView();
    }

    private void initView() {
        dialogController = DialogController.from(this, binding.getRoot());
        if (shouldApplyWindowInsets()) {
            WindowInsetsUtils.applySystemWindowInsets(binding.getRoot());
        }
        initializeViews();
        setupObservers();
        loadData();
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();

        // 页面停留时长 = 当前时间 - 页面创建时间
        long duration = System.currentTimeMillis() - pageCreateTime;

        // 页面销毁埋点
        Tracker.trackPageDestroy(this.getClass().getName(), duration);

        binding = null;
        viewModel = null;
        dialogController = null;
    }

    protected boolean shouldObserveNetworkState() {
        return true;
    }

    protected boolean shouldApplyWindowInsets() {
        return true;
    }

    protected abstract VM createViewModel();

    protected abstract VB createViewBinding(View rootView);

    protected abstract int getLayoutResId();

    protected void initializeViews() {
    }

    protected void setupObservers() {
    }

    protected void loadData() {
    }

    @Override
    public @NotNull DialogController getDialogController() {
        if (dialogController == null) {
            throw new IllegalStateException("DialogController is not available after destruction.");
        }
        return dialogController;
    }

}
