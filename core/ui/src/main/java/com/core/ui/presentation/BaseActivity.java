package com.core.ui.presentation;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.core.common.utils.WindowInsetsUtils;
import com.core.network.state.NetworkStateManager;

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewBinding> extends AppCompatActivity {

    protected VM viewModel;
    protected VB binding;

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = createViewModel();
        binding = createViewBinding();
        setContentView(binding.getRoot());

        if (shouldObserveNetworkState()) {
            getLifecycle().addObserver(NetworkStateManager.getInstance());
        }

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
    }

    protected boolean shouldObserveNetworkState() {
        return true;
    }

    protected boolean shouldApplyWindowInsets() {
        return true;
    }

    protected abstract VM createViewModel();

    protected abstract VB createViewBinding();

    protected void initializeViews() {
    }

    protected void setupObservers() {
    }

    protected void loadData() {
    }
}
