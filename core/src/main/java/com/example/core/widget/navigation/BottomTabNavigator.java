package com.example.core.widget.navigation;

import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部 Tab 导航
 */
public class BottomTabNavigator {

    private final TabLayout tabLayout;
    private final FrameLayout container;
    private final FragmentActivity activity;
    private final List<TabItem> tabs = new ArrayList<>();
    private final FragmentManager fm;
    private int currentPosition = -1;
    private OnTabSelectInterceptor onTabSelectInterceptor;

    public BottomTabNavigator(FragmentActivity activity, TabLayout tabLayout, FrameLayout container) {
        this.activity = activity;
        this.tabLayout = tabLayout;
        this.container = container;
        this.fm = activity.getSupportFragmentManager();
    }

    public BottomTabNavigator addTab(TabItem item) {
        tabs.add(item);
        return this;
    }

    public BottomTabNavigator setOnTabSelectInterceptor(OnTabSelectInterceptor interceptor) {
        this.onTabSelectInterceptor = interceptor;
        return this;
    }

    public BottomTabNavigator build() {
        if (tabs.isEmpty()) throw new IllegalStateException("至少一个 Tab");

        // 添加 Tab
        tabLayout.removeAllTabs();
        for (int i = 0; i < tabs.size(); i++) {
            TabItem item = tabs.get(i);
            TabLayout.Tab tab = tabLayout.newTab().setText(item.title).setIcon(item.iconNormal);
            tabLayout.addTab(tab);
            updateTabIcon(tab, i, false);
        }

        // 初始选中第一页
        setCurrentItem(0);

        // Tab 点击监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                boolean allow = onTabSelectInterceptor == null || onTabSelectInterceptor.shouldAllowTabSelection(pos);
                if (allow) {
                    setCurrentItem(pos);
                } else {
                    if (currentPosition != -1) {
                        TabLayout.Tab currentTab = tabLayout.getTabAt(currentPosition);
                        if (currentTab != null) currentTab.select();
                        updateTabIcon(tab, pos, false);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return this;
    }

    private void updateTabIcon(TabLayout.Tab tab, int position, boolean isSelected) {
        @DrawableRes int icon = isSelected ? tabs.get(position).iconSelected : tabs.get(position).iconNormal;
        tab.setIcon(icon);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private void setCurrentItem(int position) {
        if (position == currentPosition) return;

        FragmentTransaction ft = fm.beginTransaction();

        try {
            Fragment fragment = fm.findFragmentByTag("tab_" + position);
            if (fragment == null) {
                fragment = tabs.get(position).fragmentClass.newInstance();
                ft.add(container.getId(), fragment, "tab_" + position);
            } else {
                ft.show(fragment);
                if (fragment.getView() != null) {
                    fragment.getView().setAlpha(0.5f);
                    fragment.getView().setTranslationY(4);
                    fragment.getView().animate().alpha(1f).setDuration(100).start();
                    fragment.getView().animate().translationY(0).setDuration(100).start();
                }
            }

            if (currentPosition != -1) {
                Fragment old = fm.findFragmentByTag("tab_" + currentPosition);
                if (old != null) {
                    ft.hide(old);
                }
            }

            ft.commit();

            currentPosition = position;

            // 更新 Tab 图标
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                if (tab != null) {
                    updateTabIcon(tab, i, i == position);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void selectTab(int position) {
        if (position >= 0 && position < tabs.size()) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null) {
                tab.select();
            }
        }
    }

    public void disableTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.view.setEnabled(false);
            tab.view.setAlpha(0.6f);
        }
    }

    public void enableTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.view.setEnabled(true);
            tab.view.setAlpha(1.0f);
        }
    }

    public interface OnTabSelectInterceptor {
        boolean shouldAllowTabSelection(int position);
    }

    public static class TabItem {
        public final String title;
        @DrawableRes
        public final int iconNormal;
        @DrawableRes
        public final int iconSelected;
        public final Class<? extends Fragment> fragmentClass;

        public TabItem(String title, @DrawableRes int iconNormal,
                       @DrawableRes int iconSelected,
                       Class<? extends Fragment> fragmentClass) {
            this.title = title;
            this.iconNormal = iconNormal;
            this.iconSelected = iconSelected;
            this.fragmentClass = fragmentClass;
        }
    }
}