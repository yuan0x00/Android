package com.example.core.base;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.example.core.network.state.NetworkStateManager;
import com.example.core.utils.AdaptScreenUtils;
import com.example.core.utils.ScreenUtils;
import com.gyf.immersionbar.ImmersionBar;

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewBinding> extends AppCompatActivity {

    protected VM viewModel;
    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ImmersionBar.with(this).statusBarDarkFont(true).init();

        super.onCreate(savedInstanceState);

        getLifecycle().addObserver(NetworkStateManager.getInstance());
        viewModel = createViewModel();
        binding = createViewBinding();
        setContentView(binding.getRoot());

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

    @Override
    public Resources getResources() {
        if (ScreenUtils.isPortrait()) {
            return AdaptScreenUtils.adaptWidth(super.getResources(), 360);
        } else {
            return AdaptScreenUtils.adaptHeight(super.getResources(), 640);
        }
    }
}