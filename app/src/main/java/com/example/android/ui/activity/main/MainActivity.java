package com.example.android.ui.activity.main;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.android.R;
import com.example.android.databinding.ActivityMainBinding;
import com.example.android.ui.dialog.TipDialogFragment;
import com.example.android.ui.fragment.explore.ExploreFragment;
import com.example.android.ui.fragment.home.HomeFragment;
import com.example.android.ui.fragment.mine.MineFragment;
import com.example.core.base.BaseActivity;
import com.example.core.utils.SafeAreaUtils;
import com.example.core.utils.ToastUtils;
import com.example.core.widget.BottomTabNavigator;
import com.example.core.widget.Loading;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements TabNavigator {

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
        viewModel.login("123", "123");
    }

    private void setTabLayout(Bundle savedInstanceState) {
        // 导航条padding
        SafeAreaUtils.applyBottom(binding.tabContainer);
        // TabLayout 与 ViewPager2 关联
        navigator = new BottomTabNavigator(this, binding.tabLayout, binding.fragmentContainer)
                .addTab(new BottomTabNavigator.TabItem(
                        "首页",
                        R.drawable.home_24px,
                        R.drawable.home_fill_24px,
                        HomeFragment.class))
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
                        case 2:
                            return checkLoginState();
                        case 0:
                        case 1:
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
        boolean isLogin = false;
        if (isLogin) {
            return true;
        } else {
            Loading loading = new Loading();
            loading.show(getSupportFragmentManager());
            binding.getRoot().postDelayed(() -> {
                loading.dismissSafely();
                new TipDialogFragment()
                        .setContentText("需要登陆")
                        .size(300, 200)
                        .show(getSupportFragmentManager());
            }, 2000);
            return false;
        }
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                ToastUtils.showLongToast("登录成功");
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
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