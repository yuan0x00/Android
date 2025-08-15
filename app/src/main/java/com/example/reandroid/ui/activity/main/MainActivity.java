package com.example.reandroid.ui.activity.main;

import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.example.core.base.BaseActivity;
import com.example.core.utils.ToastUtils;
import com.example.reandroid.R;
import com.example.reandroid.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

@Route(path = "/app/main")
public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding> {

    private MainFragmentAdapter fragmentAdapter;

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
        setupViewPager();
        setupTabLayout();
    }

    private void setupViewPager() {
        fragmentAdapter = new MainFragmentAdapter(this);
        binding.viewPager.setAdapter(fragmentAdapter);
    }

    private void setupTabLayout() {
        // TabLayout 与 ViewPager2 关联
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("首页");
                    tab.setIcon(R.drawable.ic_home);
                    break;
                case 1:
                    tab.setText("发现");
                    tab.setIcon(R.drawable.ic_discover);
                    break;
                case 2:
                    tab.setText("我的");
                    tab.setIcon(R.drawable.ic_mine);
                    break;
            }
        }).attach();
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
}