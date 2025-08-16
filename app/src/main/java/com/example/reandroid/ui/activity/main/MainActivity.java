package com.example.reandroid.ui.activity.main;

import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.core.base.BaseActivity;
import com.example.core.utils.SafeAreaUtils;
import com.example.core.utils.ToastUtils;
import com.example.core.widget.BottomTabNavigator;
import com.example.reandroid.R;
import com.example.reandroid.databinding.ActivityMainBinding;
import com.example.reandroid.ui.activity.main.fragment.DiscoverFragment;
import com.example.reandroid.ui.activity.main.fragment.HomeFragment;
import com.example.reandroid.ui.activity.main.fragment.MineFragment;

@Route(path = "/app/main")
public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> implements TabNavigator {

    private BottomTabNavigator navigator;

    @Override
    protected MainViewModel createViewModel() {
        return new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Override
    protected ActivityMainBinding createViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setupTabLayout();
    }

    private void setupTabLayout() {
        // 导航条padding
        SafeAreaUtils.applyBottom(binding.tabContainer);
        binding.viewPager.setUserInputEnabled(false);
        // TabLayout 与 ViewPager2 关联
        navigator = new BottomTabNavigator(binding.tabLayout, binding.viewPager)
                .addTab(new BottomTabNavigator.TabItem(
                        "首页",
                        R.drawable.ic_home,
                        R.drawable.ic_home,
                        HomeFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        "发现",
                        R.drawable.ic_discover,
                        R.drawable.ic_discover,
                        DiscoverFragment.class))
                .addTab(new BottomTabNavigator.TabItem(
                        "我的",
                        R.drawable.ic_mine,
                        R.drawable.ic_mine,
                        MineFragment.class))
                .setOnTabSelectInterceptor((position) -> {
                    if (position == 2) {
                        ToastUtils.showLongToast("请先登录");
                        return false; // 拦截
                    }
                    return true; // 允许
                })
                .build(this);
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
        navigator.setCurrentItem(position);
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