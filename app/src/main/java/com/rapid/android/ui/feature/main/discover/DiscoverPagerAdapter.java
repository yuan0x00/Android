package com.rapid.android.ui.feature.main.discover;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rapid.android.R;
import com.rapid.android.ui.feature.main.discover.harmony.HarmonyFragment;
import com.rapid.android.ui.feature.main.discover.navigation.NavigationFragment;
import com.rapid.android.ui.feature.main.discover.project.ProjectFragment;
import com.rapid.android.ui.feature.main.discover.routes.RoutesFragment;
import com.rapid.android.ui.feature.main.discover.system.SystemFragment;
import com.rapid.android.ui.feature.main.discover.tool.ToolsFragment;
import com.rapid.android.ui.feature.main.discover.tutorial.TutorialFragment;
import com.rapid.android.ui.feature.main.discover.wechat.WechatFragment;
import com.rapid.android.ui.feature.main.discover.wenda.WendaFragment;

class DiscoverPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTitles;

    DiscoverPagerAdapter(@NonNull Fragment fragment, @NonNull Context context) {
        super(fragment);
        tabTitles = new String[]{
                context.getString(R.string.discover_tab_harmony),
                context.getString(R.string.discover_tab_routes),
                context.getString(R.string.discover_tab_tutorial),
                context.getString(R.string.discover_tab_wenda),
                context.getString(R.string.discover_tab_project),
                context.getString(R.string.discover_tab_wechat),
                context.getString(R.string.discover_tab_tools),
                context.getString(R.string.discover_tab_system),
                context.getString(R.string.discover_tab_navigation),
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 1 -> new RoutesFragment();
            case 2 -> new TutorialFragment();
            case 3 -> new WendaFragment();
            case 4 -> new ProjectFragment();
            case 5 -> new WechatFragment();
            case 6 -> new ToolsFragment();
            case 7 -> new SystemFragment();
            case 8 -> new NavigationFragment();
            default -> new HarmonyFragment();
        };
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }

    CharSequence getPageTitle(int position) {
        if (position < 0 || position >= tabTitles.length) {
            return "";
        }
        return tabTitles[position];
    }
}
