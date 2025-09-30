package com.rapid.android.presentation.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseActivity;
import com.core.ui.navigation.BottomTabNavigator;
import com.core.utils.ui.ToastUtils;
import com.core.utils.ui.WindowInsetsUtils;
import com.core.webview.core.WebViewPrewarmer;
import com.rapid.android.R;
import com.rapid.android.data.session.AuthSessionManager;
import com.rapid.android.databinding.ActivityMainBinding;
import com.rapid.android.presentation.ui.base.ITabNavigator;
import com.rapid.android.presentation.ui.fragment.ExploreFragment;
import com.rapid.android.presentation.ui.fragment.HomeFragment;
import com.rapid.android.presentation.ui.fragment.MineFragment;
import com.rapid.android.presentation.ui.fragment.PlazaFragment;
import com.rapid.android.presentation.viewmodel.MainViewModel;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements ITabNavigator {

    private BottomTabNavigator navigator;
    private long exitTime = 0L;

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
        WebViewPrewarmer.prewarmInIdle(this, 1);
    }

    private void setBackToTask() {
        //连续两次返回退到桌面
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    exitTime = System.currentTimeMillis();
                    String msg = getString(R.string.back_twice_to_launcher);
                    ToastUtils.showShortToast(msg);
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
    }

    private void setTabLayout(Bundle savedInstanceState) {
        // TabLayout 下沉到导航栏
        WindowInsetsUtils.removeOnApplyWindowInsetsListener(binding.getRoot());
        WindowInsetsUtils.applyTopSystemWindowInsets(binding.getRoot());
        WindowInsetsUtils.applyBottomSystemWindowInsets(binding.tabContainer);
        // TabLayout 与 ViewPager2 关联
        navigator = new BottomTabNavigator(this, binding.tabLayout, binding.fragmentContainer)
                .addTab(new BottomTabNavigator.TabItem(
                        "首页",
                        R.drawable.home_24px,
                        R.drawable.home_fill_24px,
                        HomeFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        "广场",
                        R.drawable.article_24px,
                        R.drawable.article_fill_24px,
                        PlazaFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        "发现",
                        R.drawable.explore_24px,
                        R.drawable.explore_fill_24px,
                        ExploreFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        "我的",
                        R.drawable.person_24px,
                        R.drawable.person_fill_24px,
                        MineFragment.class))
                .setOnTabSelectInterceptor((position) -> {
                    switch (position) {
                        case 3:
                            return checkLoginState();
                        case 0:
                        case 1:
                        case 2:
                    }
                    return true;
                })
                .build();
        // 恢复之前选中的 Tab
        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt("CURRENT_TAB", 0);
            navigator.selectTab(position);
        }
    }

    private boolean checkLoginState() {
        Boolean isLoggedIn = AuthSessionManager.loginState().getValue();
        if (Boolean.TRUE.equals(isLoggedIn)) {
            return true;
        }
        startActivity(new Intent(this, LoginActivity.class));
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshLoginState();
    }

    @Override
    protected void setupObservers() {
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });

        AuthSessionManager.loginState().observe(this, loggedIn -> {
            if (navigator == null) {
                return;
            }
            navigator.enableTab(3);
            if (!Boolean.TRUE.equals(loggedIn) && navigator.getCurrentPosition() == 3) {
                navigator.selectTab(0);
            }
        });

        AuthSessionManager.authEvents().observe(this, event -> {
            if (event == null || navigator == null) return;
            if (event.getType() == AuthSessionManager.EventType.LOGOUT
                    || event.getType() == AuthSessionManager.EventType.UNAUTHORIZED) {
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
}
