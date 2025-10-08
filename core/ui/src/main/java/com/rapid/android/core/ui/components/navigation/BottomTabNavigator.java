package com.rapid.android.core.ui.components.navigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部 Tab 导航
 */
public class BottomTabNavigator {

    private final NavigationBarView navigationBarView;
    private final FrameLayout container;
    private final FragmentActivity activity;
    private final List<TabItem> tabs = new ArrayList<>();
    private final FragmentManager fm;
    private int currentPosition = -1;
    private OnTabSelectInterceptor onTabSelectInterceptor;
    private boolean isUpdatingSelection = false;

    public BottomTabNavigator(FragmentActivity activity, NavigationBarView navigationBarView, FrameLayout container) {
        this.activity = activity;
        this.navigationBarView = navigationBarView;
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

        // 添加菜单项
        Menu menu = navigationBarView.getMenu();
        menu.clear();
        for (int i = 0; i < tabs.size(); i++) {
            TabItem item = tabs.get(i);
            MenuItem menuItem = menu.add(Menu.NONE, i, i, item.title);
            menuItem.setIcon(createStateListDrawable(activity, item.iconNormal, item.iconSelected));
        }

        // 初始选中第一页
        setCurrentItem(0);

        navigationBarView.setOnItemSelectedListener(item -> {
            if (isUpdatingSelection) {
                return true;
            }
            int position = item.getItemId();
            boolean allow = onTabSelectInterceptor == null || onTabSelectInterceptor.shouldAllowTabSelection(position);
            if (allow) {
                setCurrentItem(position);
                return true;
            }
            restoreCurrentSelection();
            return false;
        });

        navigationBarView.setOnItemReselectedListener(item -> {
            // no-op for now
        });

        return this;
    }

    private Drawable createStateListDrawable(@NonNull Context context,
                                             @DrawableRes int iconNormal,
                                             @DrawableRes int iconSelected) {
        StateListDrawable drawable = new StateListDrawable();
        Drawable selected = ContextCompat.getDrawable(context, iconSelected);
        Drawable normal = ContextCompat.getDrawable(context, iconNormal);
        drawable.addState(new int[]{android.R.attr.state_checked}, selected);
        drawable.addState(new int[]{}, normal);
        return drawable;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private void setCurrentItem(int position) {
        if (position == currentPosition) return;
        isUpdatingSelection = true;

        FragmentTransaction ft = fm.beginTransaction();

        try {
            Fragment fragment = fm.findFragmentByTag("tab_" + position);
            if (fragment == null) {
                fragment = tabs.get(position).fragmentClass.newInstance();
                ft.add(container.getId(), fragment, "tab_" + position);
            } else {
                ft.show(fragment);
                if (fragment.getView() != null) {
                    fragment.getView().setAlpha(0.9f);
                    fragment.getView().setTranslationY(2);
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

            if (navigationBarView.getMenu().size() > position) {
                navigationBarView.getMenu().getItem(position).setChecked(true);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            isUpdatingSelection = false;
        }
    }

    public void selectTab(int position) {
        if (position >= 0 && position < tabs.size()) {
            setCurrentItem(position);
        }
    }

    public void disableTab(int position) {
        if (position >= 0 && position < navigationBarView.getMenu().size()) {
            MenuItem item = navigationBarView.getMenu().getItem(position);
            if (item != null) {
                item.setEnabled(false);
            }
        }
    }

    public void enableTab(int position) {
        if (position >= 0 && position < navigationBarView.getMenu().size()) {
            MenuItem item = navigationBarView.getMenu().getItem(position);
            if (item != null) {
                item.setEnabled(true);
            }
        }
    }

    private void restoreCurrentSelection() {
        if (currentPosition >= 0 && currentPosition < navigationBarView.getMenu().size()) {
            isUpdatingSelection = true;
            navigationBarView.getMenu().getItem(currentPosition).setChecked(true);
            isUpdatingSelection = false;
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
