package com.example.core.widget;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部 Tab 导航
 */
public class BottomTabNavigator {

    private final TabLayout tabLayout;
    private final ViewPager2 viewPager;
    private final List<TabItem> tabs = new ArrayList<>();
    private OnTabSelectInterceptor onTabSelectInterceptor;

    public BottomTabNavigator(TabLayout tabLayout, ViewPager2 viewPager) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
    }

    //---------- 链式调用方法 ----------
    public BottomTabNavigator addTab(TabItem item) {
        tabs.add(item);
        return this;
    }

    public BottomTabNavigator setOnTabSelectInterceptor(OnTabSelectInterceptor interceptor) {
        this.onTabSelectInterceptor = interceptor;
        return this;
    }

    /**
     * 构建并绑定导航器
     */
    public BottomTabNavigator build(FragmentActivity activity) {
        if (tabs.isEmpty()) {
            throw new IllegalStateException("至少需要添加一个 Tab");
        }

        // 1. 设置 ViewPager2 适配器
        FragmentStateAdapter adapter = new FragmentStateAdapter(activity) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                try {
                    return tabs.get(position).fragmentClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("无法创建 Fragment: " + tabs.get(position).fragmentClass.getSimpleName(), e);
                }
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        };
        viewPager.setAdapter(adapter);

        // 2. 手动创建 Tab
        tabLayout.removeAllTabs();
        for (int i = 0; i < tabs.size(); i++) {
            TabItem item = tabs.get(i);
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(item.title);
            tab.setIcon(item.iconNormal);
            tabLayout.addTab(tab);

            // 初始图标状态
            updateTabIcon(tab, i, i == 0);
        }

        // 3. 同步 ViewPager2 滑动 → TabLayout 选中状态
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null && !tab.isSelected()) {
                    tab.select(); // 同步 UI
                }
                // 更新所有 Tab 图标
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab t = tabLayout.getTabAt(i);
                    if (t != null) {
                        updateTabIcon(t, i, i == position);
                    }
                }
            }
        });

        // 4. 拦截 Tab 点击 → 控制是否允许切换
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                boolean allowSelect = onTabSelectInterceptor == null ||
                        onTabSelectInterceptor.shouldAllowTabSelection(position);

                if (allowSelect) {
                    // 允许切换
                    if (viewPager.getCurrentItem() != position) {
                        viewPager.setCurrentItem(position, true);
                    }
                } else {
                    // 拦截：保持当前页面，还原 Tab 状态
                    int current = viewPager.getCurrentItem();
                    TabLayout.Tab currentTab = tabLayout.getTabAt(current);
                    if (currentTab != null) {
                        currentTab.select(); // 强制还原 UI
                        updateTabIcon(tab, position, false); // 恢复未选中图标
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不处理，由 onPageSelected 控制
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 可选：处理重选
            }
        });

        // 5. 初始选中第一页
        if (viewPager.getCurrentItem() == 0) {
            TabLayout.Tab firstTab = tabLayout.getTabAt(0);
            if (firstTab != null) {
                firstTab.select();
            }
        } else {
            viewPager.setCurrentItem(0, false);
        }

        return this;
    }

    //---------- 内部方法 ----------
    private void updateTabIcon(TabLayout.Tab tab, int position, boolean isSelected) {
        @DrawableRes int iconRes = isSelected ? tabs.get(position).iconSelected : tabs.get(position).iconNormal;
        tab.setIcon(iconRes);
    }

    /**
     * 获取当前选中位置
     */
    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }

    /**
     * 跳转到指定页面（受拦截器控制）
     */
    public BottomTabNavigator setCurrentItem(int position) {
        if (position < 0 || position >= tabs.size()) return this;

        boolean allowSelect = onTabSelectInterceptor == null ||
                onTabSelectInterceptor.shouldAllowTabSelection(position);

        if (allowSelect) {
            viewPager.setCurrentItem(position, true);
        }
        return this;
    }

    //---------- 外部控制方法 ----------

    /**
     * 强制跳转（绕过拦截器）
     */
    public BottomTabNavigator setCurrentItem(int position, boolean smoothScroll) {
        if (position >= 0 && position < tabs.size()) {
            viewPager.setCurrentItem(position, smoothScroll);
        }
        return this;
    }

    /**
     * 禁用某个 Tab
     */
    public BottomTabNavigator disableTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.view.setEnabled(false);
            tab.view.setAlpha(0.6f);
        }
        return this;
    }

    /**
     * 启用某个 Tab
     */
    public BottomTabNavigator enableTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null) {
            tab.view.setEnabled(true);
            tab.view.setAlpha(1.0f);
        }
        return this;
    }

    //---------- 拦截器接口 ----------
    public interface OnTabSelectInterceptor {
        /**
         * 是否允许选中指定位置的 Tab
         *
         * @param position 即将选中的位置
         * @return true 允许切换；false 拦截切换
         */
        boolean shouldAllowTabSelection(int position);
    }

    //---------- Tab 项定义 ----------
    public static class TabItem {
        public final String title;
        @DrawableRes
        public final int iconNormal;
        @DrawableRes
        public final int iconSelected;
        public final Class<? extends Fragment> fragmentClass;

        public TabItem(String title,
                       @DrawableRes int iconNormal,
                       @DrawableRes int iconSelected,
                       Class<? extends Fragment> fragmentClass) {
            this.title = title;
            this.iconNormal = iconNormal;
            this.iconSelected = iconSelected;
            this.fragmentClass = fragmentClass;
        }
    }
}