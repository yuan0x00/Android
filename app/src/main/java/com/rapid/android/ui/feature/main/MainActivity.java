package com.rapid.android.ui.feature.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.core.common.utils.ToastUtils;
import com.core.common.utils.WindowInsetsUtils;
import com.core.data.session.SessionManager;
import com.core.ui.components.navigation.BottomTabNavigator;
import com.core.ui.presentation.BaseActivity;
import com.core.webview.core.WebViewPrewarmer;
import com.rapid.android.R;
import com.rapid.android.databinding.ActivityMainBinding;
import com.rapid.android.ui.feature.login.LoginActivity;
import com.rapid.android.ui.feature.main.home.HomeFragment;
import com.rapid.android.ui.feature.main.mine.MineFragment;
import com.rapid.android.ui.feature.main.navigation.NavigationFragment;
import com.rapid.android.ui.feature.main.project.ProjectFragment;
import com.rapid.android.ui.feature.main.system.SystemFragment;
import com.rapid.android.ui.feature.main.wechat.WechatFragment;

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
        isOnCreate = false;
    }

    private void setTabLayout(Bundle savedInstanceState) {
        // TabLayout 下沉到导航栏
        WindowInsetsUtils.removeOnApplyWindowInsetsListener(binding.getRoot());
        WindowInsetsUtils.applyTopSystemWindowInsets(binding.getRoot());
        WindowInsetsUtils.applyBottomSystemWindowInsets(binding.tabContainer);
        // TabLayout 与 ViewPager2 关联
        navigator = new BottomTabNavigator(this, binding.tabLayout, binding.fragmentContainer)
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_home),
                        R.drawable.home_24px,
                        R.drawable.home_fill_24px,
                        HomeFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_navigation),
                        R.drawable.signpost_24px,
                        R.drawable.signpost_fill_24px,
                        NavigationFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_system),
                        R.drawable.family_history_24px,
                        R.drawable.family_history_fill_24px,
                        SystemFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_wechat),
                        R.drawable.article_person_24px,
                        R.drawable.article_person_fill_24px,
                        WechatFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_project),
                        R.drawable.folder_code_24px,
                        R.drawable.folder_code_fill_24px,
                        ProjectFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        getString(R.string.main_tab_profile),
                        R.drawable.person_24px,
                        R.drawable.person_fill_24px,
                        MineFragment.class))
                .setOnTabSelectInterceptor((position) -> {
                    switch (position) {
                        case 4:
//                            return checkLoginState();
                        case 0:
                        case 1:
                        case 2:
                        case 3:
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
        Boolean isLoggedIn = SessionManager.getInstance().loginState().getValue();
        if (Boolean.TRUE.equals(isLoggedIn)) {
            return true;
        }
        startActivity(new Intent(this, LoginActivity.class));
        return false;
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
                ToastUtils.showLongToast(msg);
            }
        });

        SessionManager.getInstance().loginState().observe(this, loggedIn -> {
//            if (navigator == null) {
//                return;
//            }
//            if (!Boolean.TRUE.equals(loggedIn) && navigator.getCurrentPosition() == 3) {
//                navigator.selectTab(0);
//            }
        });

        SessionManager.getInstance().authEvents().observe(this, event -> {
//            if (event == null || navigator == null) return;
//            if (event.getType() == AuthSessionManager.EventType.LOGOUT
//                    || event.getType() == AuthSessionManager.EventType.UNAUTHORIZED) {
//                navigator.selectTab(0);
//            }
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
