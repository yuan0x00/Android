package com.rapid.android.core.ui.presentation;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.viewbinding.ViewBinding;

import com.rapid.android.core.common.utils.WindowInsetsUtils;
import com.rapid.android.core.network.state.NetworkStateManager;
import com.rapid.android.core.ui.R;
import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.components.dialog.DialogHost;
import com.rapid.android.core.ui.components.dialog.ScopedDialogHost;

import org.jetbrains.annotations.NotNull;

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewBinding> extends AppCompatActivity implements DialogHost, ScopedDialogHost {

    protected VM viewModel;
    protected VB binding;
    private DialogController dialogController;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.core_ui_activity_container);

        if (shouldObserveNetworkState()) {
            getLifecycle().addObserver(NetworkStateManager.getInstance());
        }

        setContentViewAsync(getLayoutResId());
        viewModel = createViewModel();
    }

    private void setContentViewAsync(@LayoutRes int resid) {
        ViewGroup container = findViewById(R.id.container);
        new AsyncLayoutInflater(this)
                .inflate(resid, container, (view, resId, parent) -> {
                    container.addView(view);
                    binding = createViewBinding(view);
                    initView();
                }
        );
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

    @Override
    public @NotNull DialogController provideDialogController() {
        return getDialogController();
    }
}
