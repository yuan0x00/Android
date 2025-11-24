package com.rapid.android.feature.main;

import android.content.Intent;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.R;
import com.rapid.android.core.common.utils.WindowInsetsUtils;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.components.navigation.BottomTabNavigator;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.core.ui.utils.ToastViewUtils;
import com.rapid.android.databinding.ActivityMainBinding;
import com.rapid.android.feature.login.LoginActivity;
import com.rapid.android.feature.main.home.HomeFragment;
import com.rapid.android.feature.main.mine.MineFragment;
import com.rapid.android.feature.main.plaza.PlazaFragment;
import com.rapid.android.ui.view.DrawerLayoutHelper;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends BaseActivity<MainViewModel, ActivityMainBinding>
        implements TabNavigator {

    private BottomTabNavigator navigator;
    private long exitTime = 0L;

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
    protected void initializeViews() {
        setBackToTask();
        setTabLayout();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.drawerFragmentContainer, new MineFragment())
                .commit();

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout, (v, insets) -> {
            Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            binding.navigationView.setPadding(
                    binding.navigationView.getPaddingLeft(),
                    statusBars.top,
                    binding.navigationView.getPaddingRight(),
                    binding.navigationView.getPaddingBottom()
            );
            return insets;
        });

        DrawerLayoutHelper.setDrawerLeftEdgeSizeWithContentPush(this, binding.drawerLayout, 0.5f);
    }

    private void setBackToTask() {
        //连续两次返回退到桌面
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    exitTime = System.currentTimeMillis();
                    String msg = getString(R.string.back_twice_to_launcher);
                    ToastViewUtils.showShortToast(getDialogController(), msg);
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

    }

    private void setTabLayout() {
        // 处理系统栏 Insets
        WindowInsetsUtils.removeOnApplyWindowInsetsListener(binding.getRoot());
        WindowInsetsUtils.applyTopSystemWindowInsets(binding.contentView);
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
//                .addTab(new BottomTabNavigator.TabItem(
//                        getString(R.string.main_tab_profile),
//                        R.drawable.person_24px,
//                        R.drawable.person_fill_24px,
//                        MineFragment.class))
                .setOnTabSelectInterceptor(this::shouldAllowTabSelection)
                .build();
    }

    @Override
    protected void setupObservers() {
        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastViewUtils.showLongToast(getDialogController(), msg);
            }
        });

        SessionManager.getInstance().state.observe(this, state -> {
            if (state == null || navigator == null) {
                return;
            }
            if (!state.isLoggedIn()) {
                if (navigator.getCurrentPosition() >= 2) {
                    ToastViewUtils.showShortToast(getDialogController(), getString(R.string.mine_toast_require_login));
                }
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

    @Override
    public void onHomeNavigationClick() {
        binding.drawerLayout.open();
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
        ToastViewUtils.showShortToast(getDialogController(), getString(R.string.mine_toast_require_login));
        startActivity(new Intent(this, LoginActivity.class));
        return false;
    }

}
