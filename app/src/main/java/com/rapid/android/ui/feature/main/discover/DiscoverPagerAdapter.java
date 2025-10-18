package com.rapid.android.ui.feature.main.discover;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rapid.android.R;
import com.rapid.android.ui.feature.main.discover.harmony.DiscoverHarmonyFragment;
import com.rapid.android.ui.feature.main.discover.navigation.NavigationFragment;
import com.rapid.android.ui.feature.main.discover.project.ProjectFragment;
import com.rapid.android.ui.feature.main.discover.routes.DiscoverRoutesFragment;
import com.rapid.android.ui.feature.main.discover.system.SystemFragment;
import com.rapid.android.ui.feature.main.discover.tool.DiscoverToolsFragment;
import com.rapid.android.ui.feature.main.discover.tutorial.DiscoverTutorialFragment;
import com.rapid.android.ui.feature.main.discover.wechat.WechatFragment;
import com.rapid.android.ui.feature.main.discover.wenda.DiscoverWendaFragment;

class DiscoverPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_COUNT = 10;
    private final String[] tabTitles;

    DiscoverPagerAdapter(@NonNull Fragment fragment, @NonNull Context context) {
        super(fragment);
        tabTitles = new String[]{
                context.getString(R.string.discover_tab_harmony),
                context.getString(R.string.discover_tab_routes),
                context.getString(R.string.discover_tab_navigation),
                context.getString(R.string.discover_tab_tutorial),
                context.getString(R.string.discover_tab_wenda),
                context.getString(R.string.discover_tab_project),
                context.getString(R.string.discover_tab_wechat),
                context.getString(R.string.discover_tab_tools),
                context.getString(R.string.discover_tab_system)
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DiscoverHarmonyFragment();
            case 1:
                return new DiscoverRoutesFragment();
            case 2:
                return new NavigationFragment();
            case 3:
                return new DiscoverTutorialFragment();
            case 4:
                return new DiscoverWendaFragment();
            case 5:
                return new ProjectFragment();
            case 6:
                return new WechatFragment();
            case 7:
                return new DiscoverToolsFragment();
            case 8:
                return new SystemFragment();
            default:
                return new DiscoverHarmonyFragment();
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
