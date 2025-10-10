package com.rapid.android.ui.feature.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.rapid.android.R;
import com.rapid.android.core.common.utils.WindowInsetsUtils;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.components.navigation.BottomTabNavigator;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.core.webview.core.WebViewPrewarmer;
import com.rapid.android.databinding.ActivityMainBinding;
import com.rapid.android.ui.feature.login.LoginActivity;
import com.rapid.android.ui.feature.main.discover.DiscoverFragment;
import com.rapid.android.ui.feature.main.home.HomeFragment;
import com.rapid.android.ui.feature.main.message.MessageFragment;
import com.rapid.android.ui.feature.main.mine.MineFragment;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding>
        implements TabNavigator {

    private BottomTabNavigator navigator;
    private long exitTime = 0L;

    private boolean isOnCreate = true;

    @Override
    protected MainViewModel createViewModel() {
        return new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    protected ActivityMainBinding createViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBackToTask();
        setTabLayout(savedInstanceState);

        // 主页空闲时预热WebView池，提升后续Web页面首开速度
        // 预热2个WebView实例以支持并发场景
        WebViewPrewarmer.prewarmInIdle(this, 2);
    }

    private void setBackToTask() {
        //连续两次返回退到桌面
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    exitTime = System.currentTimeMillis();
                    String msg = getString(R.string.back_twice_to_launcher);
                    ToastUtils.showShortToast(getDialogController(), msg);
                } else {
                    moveTaskToBack(true);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(android.os.@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (navigator != null) {
            outState.putInt("CURRENT_TAB", navigator.getCurrentPosition());
        }
    }

    @Override
    protected void loadData() {
        viewModel.refreshLoginState();
        isOnCreate = false;
    }

    private void setTabLayout(Bundle savedInstanceState) {
        // 处理系统栏 Insets
        WindowInsetsUtils.removeOnApplyWindowInsetsListener(binding.getRoot());
        WindowInsetsUtils.applyTopSystemWindowInsets(binding.getRoot());
        WindowInsetsUtils.applyBottomSystemWindowInsets(binding.bottomNavigation);
        applyNavigationBarSurface();
        // 底部导航与 Fragment 管理关联
        navigator = new BottomTabNavigator(this, binding.bottomNavigation, binding.fragmentContainer)
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_home),
                        R.drawable.home_24px,
                        R.drawable.home_fill_24px,
                        HomeFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_discover),
                        R.drawable.explore_24px,
                        R.drawable.explore_fill_24px,
                        DiscoverFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_message),
                        R.drawable.mail_24px,
                        R.drawable.mail_fill_24px,
                        MessageFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_profile),
                        R.drawable.person_24px,
                        R.drawable.person_fill_24px,
                        MineFragment.class))
                .setOnTabSelectInterceptor(this::shouldAllowTabSelection)
                .build();
        // 恢复之前选中的 Tab
        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt("CURRENT_TAB", 0);
            navigator.selectTab(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnCreate) {
            viewModel.refreshLoginState();
        }
    }

    @Override
    protected void setupObservers() {
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(getDialogController(), msg);
            }
        });

        SessionManager.getInstance().loginState().observe(this, loggedIn -> {
            if (navigator == null) {
                return;
            }
            if (!Boolean.TRUE.equals(loggedIn) && navigator.getCurrentPosition() >= 2) {
                ToastUtils.showShortToast(getDialogController(), getString(R.string.mine_toast_require_login));
                navigator.selectTab(0);
            }
        });

        SessionManager.getInstance().authEvents().observe(this, event -> {
            if (event == null || navigator == null) {
                return;
            }
            SessionManager.EventType type = event.getType();
            if (type == SessionManager.EventType.LOGOUT || type == SessionManager.EventType.UNAUTHORIZED) {
                navigator.selectTab(0);
            }
        });
    }

    @Override
    public void navigateTo(int position) {
        navigator.selectTab(position);
    }

    @Override
    public void disableTab(int position) {
        navigator.disableTab(position);
    }

    @Override
    public void enableTab(int position) {
        navigator.enableTab(position);
    }

    @Override
    public void hideBottomBar(boolean animated) {
        if (navigator != null) {
            navigator.hideBottomBar(animated);
        }
    }

    @Override
    public void showBottomBar(boolean animated) {
        if (navigator != null) {
            navigator.showBottomBar(animated);
        }
    }

    @Override
    public boolean isBottomBarVisible() {
        return navigator != null && navigator.isBottomBarVisible();
    }

    private boolean shouldAllowTabSelection(int position) {
        if (position < 2) {
            return true;
        }
        return ensureLoggedIn();
    }

    private boolean ensureLoggedIn() {
        if (SessionManager.getInstance().isLoggedIn()) {
            return true;
        }
        ToastUtils.showShortToast(getDialogController(), getString(R.string.mine_toast_require_login));
        startActivity(new Intent(this, LoginActivity.class));
        return false;
    }

    private void applyNavigationBarSurface() {
        float elevation = getResources().getDimension(R.dimen.navigation_bar_elevation);
        MaterialShapeDrawable drawable = new MaterialShapeDrawable();
        drawable.initializeElevationOverlay(this);
        int surfaceColor = SurfaceColors.getColorForElevation(this, elevation);
        drawable.setFillColor(ColorStateList.valueOf(surfaceColor));
        drawable.setElevation(elevation);
        binding.bottomNavigation.setBackground(drawable);
        binding.bottomNavigation.setElevation(elevation);
    }

}
