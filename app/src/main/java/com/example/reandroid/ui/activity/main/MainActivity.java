package com.example.reandroid.ui.activity.main;

import android.view.Gravity;

import androidx.lifecycle.ViewModelProvider;

import com.example.core.base.BaseActivity;
import com.example.core.utils.SafeAreaUtils;
import com.example.core.utils.ToastUtils;
import com.example.core.widget.BottomTabNavigator;
import com.example.reandroid.R;
import com.example.reandroid.databinding.ActivityMainBinding;
import com.example.reandroid.ui.activity.main.fragment.ExploreFragment;
import com.example.reandroid.ui.activity.main.fragment.HomeFragment;
import com.example.reandroid.ui.activity.main.fragment.MineFragment;
import com.example.reandroid.ui.dialog.TipDialogFragment;

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
                    if (position == 2) {
                        new TipDialogFragment()
                                .size(300, 200)
                                .gravity(Gravity.CENTER)
                                .cancelable(true)
                                .cancelableOutside(true)
                                .show(getSupportFragmentManager());
                        ToastUtils.showLongToast("请先登录");
                        return false; // 拦截
                    }
                    return true; // 允许
                })
                .build();
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