package com.example.core.base;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

import com.example.core.network.NetworkStateManager;
import com.example.core.utils.AdaptScreenUtils;
import com.example.core.utils.BarUtils;
import com.example.core.utils.ScreenUtils;

public abstract class BaseActivity<VM extends BaseViewModel, VB extends ViewBinding> extends AppCompatActivity {

    protected VM viewModel;
    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 设置状态栏透明和亮色字体
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT);
        BarUtils.setStatusBarLightMode(this, true);

        super.onCreate(savedInstanceState);

        getLifecycle().addObserver(NetworkStateManager.getInstance());
        viewModel = createViewModel();
        binding = createViewBinding();
        setContentView(binding.getRoot());

        // 应用安全区 padding
        applyStatusBarPadding();

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

    // ---------------- 安全区控制方法（子类可重写）----------------

    /**
     * 是否启用状态栏安全区 padding（默认 true）
     */
    protected boolean isStatusBarPaddingEnabled() {
        return true;
    }

    /**
     * 是否启用导航栏安全区 padding（默认 true）
     */
    protected boolean isNavigationBarPaddingEnabled() {
        return true;
    }

    /**
     * 是否只应用系统栏（状态栏 + 导航栏）的 padding，忽略输入法
     */
    protected boolean applyOnlySystemBarsInsets() {
        return true;
    }

    // ---------------- 内部实现 ----------------

    private void applyStatusBarPadding() {
        if (!isStatusBarPaddingEnabled() && !isNavigationBarPaddingEnabled()) {
            return;
        }

        View rootView = binding.getRoot();
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            int typeMask = applyOnlySystemBarsInsets() ?
                    WindowInsetsCompat.Type.systemBars() :
                    WindowInsetsCompat.Type.mandatorySystemGestures();

            androidx.core.graphics.Insets insets = windowInsets.getInsets(typeMask);

            int paddingTop = isStatusBarPaddingEnabled() ? insets.top : 0;
            int paddingBottom = isNavigationBarPaddingEnabled() ? insets.bottom : 0;

            v.setPadding(
                    v.getPaddingLeft(),
                    paddingTop,
                    v.getPaddingRight(),
                    paddingBottom
            );

            return windowInsets; // 不消费，继续传递
        });
    }

    // ---------------- 资源适配 ----------------

    @Override
    public Resources getResources() {
        if (ScreenUtils.isPortrait()) {
            return AdaptScreenUtils.adaptWidth(super.getResources(), 360);
        } else {
            return AdaptScreenUtils.adaptHeight(super.getResources(), 640);
        }
    }
}