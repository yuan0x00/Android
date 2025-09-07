package com.rapid.core.base.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.rapid.core.base.vm.BaseViewModel;
import com.rapid.core.net.state.NetworkStateManager;
import com.rapid.core.utils.device.StatusBarUtils;
import com.rapid.core.utils.ui.WindowInsetsUtils;

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewBinding> extends AppCompatActivity {

    protected VM viewModel;
    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLifecycle().addObserver(NetworkStateManager.getInstance());
        viewModel = createViewModel();
        binding = createViewBinding();
        setContentView(binding.getRoot());
        WindowInsetsUtils.applySystemWindowInsets(binding.getRoot());
        StatusBarUtils.setStatusBarIconDark(this, true);

        initializeViews();
        setupObservers();
        loadData();
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