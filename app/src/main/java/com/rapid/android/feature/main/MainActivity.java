package com.rapid.android.feature.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.common.utils.WindowInsetsUtils;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.components.navigation.BottomTabNavigator;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ActivityMainBinding;
import com.rapid.android.feature.login.LoginActivity;
import com.rapid.android.feature.main.home.HomeFragment;
import com.rapid.android.feature.main.mine.MineFragment;
import com.rapid.android.feature.main.plaza.PlazaFragment;

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
    protected ActivityMainBinding createViewBinding(View rootView) {
        return ActivityMainBinding.bind(rootView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initializeViews() {
        super.initializeViews();
        setBackToTask();
        setTabLayout(null);
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
        // 底部导航与 Fragment 管理关联
        navigator = new BottomTabNavigator(this, binding.bottomNavigation, binding.fragmentContainer)
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_home),
                        R.drawable.home_24px,
                        R.drawable.home_fill_24px,
                        HomeFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.discover_tab_square),
                        R.drawable.newsmode_24px,
                        R.drawable.newsmode_fill_24px,
                        PlazaFragment.class))
//                .addTab(new BottomTabNavigator.TabItem(
//                        getString(R.string.main_tab_discover),
//                        R.drawable.explore_24px,
//                        R.drawable.explore_fill_24px,
//                        DiscoverFragment.class))
//                .addTab(new BottomTabNavigator.TabItem(
//                        getString(R.string.main_tab_message),
//                        R.drawable.notifications_24px,
//                        R.drawable.notifications_fill_24px,
//                        MessageFragment.class))
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
        BottomTabNavigator.TabItem tabItem = navigator != null ? navigator.getTabItem(position) : null;
        if (tabItem == null) {
            return true;
        }
        if (!RequiresLoginTab.class.isAssignableFrom(tabItem.fragmentClass)) {
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

}
