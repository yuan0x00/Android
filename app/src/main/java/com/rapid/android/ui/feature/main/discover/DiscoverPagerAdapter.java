package com.rapid.android.ui.feature.main.discover;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rapid.android.R;
import com.rapid.android.ui.feature.main.navigation.NavigationFragment;
import com.rapid.android.ui.feature.main.project.ProjectFragment;
import com.rapid.android.ui.feature.main.system.SystemFragment;
import com.rapid.android.ui.feature.main.wechat.WechatFragment;

class DiscoverPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_COUNT = 5;
    private final String[] tabTitles;

    DiscoverPagerAdapter(@NonNull Fragment fragment, @NonNull Context context) {
        super(fragment);
        tabTitles = new String[]{
                context.getString(R.string.discover_tab_hot),
                context.getString(R.string.discover_tab_navigation),
                context.getString(R.string.discover_tab_system),
                context.getString(R.string.discover_tab_project),
                context.getString(R.string.discover_tab_wechat)
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DiscoverHotFragment();
            case 2:
                return new SystemFragment();
            case 3:
                return new ProjectFragment();
            case 4:
                return new WechatFragment();
            case 1:
            default:
                return new NavigationFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }

    CharSequence getPageTitle(int position) {
        if (position < 0 || position >= tabTitles.length) {
            return "";
        }
        return tabTitles[position];
    }
}
