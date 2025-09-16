package com.rapid.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.core.base.ui.BaseActivity;
import com.core.net.base.BaseResponse;
import com.core.ui.navigation.BottomTabNavigator;
import com.core.utils.ui.ToastUtils;
import com.core.utils.ui.WindowInsetsUtils;
import com.rapid.android.R;
import com.rapid.android.auth.AuthManager;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.databinding.ActivityMainBinding;
import com.rapid.android.ui.base.ITabNavigator;
import com.rapid.android.ui.fragment.ExploreFragment;
import com.rapid.android.ui.fragment.HomeFragment;
import com.rapid.android.ui.fragment.MineFragment;
import com.rapid.android.ui.fragment.PlazaFragment;
import com.rapid.android.viewmodel.MainViewModel;
import com.webview.core.WebViewPrewarmer;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableObserver;

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
        AuthManager.getInstance().reLogin(new DisposableObserver<>() {
            @Override
            public void onNext(@NonNull BaseResponse<LoginBean> loginBeanBaseResponse) {
                if (loginBeanBaseResponse != null && loginBeanBaseResponse.getData() != null) {
                    LoginBean loginBean = loginBeanBaseResponse.getData();
                    ToastUtils.showShortToast(loginBean.getUsername());
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                ToastUtils.showShortToast(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        });
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
        if (AuthManager.getInstance().isLoggedIn()) {
            return true;
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthManager.getInstance().isLoggedIn()) {
            if (navigator.getCurrentPosition() == 3) {
                navigator.selectTab(0);
            }
        }
    }

    @Override
    protected void setupObservers() {
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