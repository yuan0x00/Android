package com.rapid.android.feature.main.discover;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rapid.android.R;
import com.rapid.android.feature.main.discover.harmony.HarmonyFragment;
import com.rapid.android.feature.main.discover.navigation.NavigationFragment;
import com.rapid.android.feature.main.discover.project.ProjectFragment;
import com.rapid.android.feature.main.discover.routes.RoutesFragment;
import com.rapid.android.feature.main.discover.system.SystemFragment;
import com.rapid.android.feature.main.discover.tool.ToolsFragment;
import com.rapid.android.feature.main.discover.tutorial.TutorialFragment;
import com.rapid.android.feature.main.discover.wechat.WechatFragment;
import com.rapid.android.feature.main.discover.wenda.WendaFragment;

import java.util.ArrayList;
import java.util.List;

class DiscoverPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final FragmentActivity host;

    DiscoverPagerAdapter(@NonNull Fragment fragment, @NonNull Context context) {
        super(fragment);
        this.host = (FragmentActivity) context;
    }

    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }

    public void addFragments() {
        addFragment(new HarmonyFragment(), host.getString(R.string.discover_tab_harmony));
        addFragment(new RoutesFragment(), host.getString(R.string.discover_tab_routes));
        addFragment(new TutorialFragment(), host.getString(R.string.discover_tab_tutorial));
        addFragment(new WendaFragment(), host.getString(R.string.discover_tab_wenda));
        addFragment(new ProjectFragment(), host.getString(R.string.discover_tab_project));
        addFragment(new WechatFragment(), host.getString(R.string.discover_tab_wechat));
        addFragment(new ToolsFragment(), host.getString(R.string.discover_tab_tools));
        addFragment(new SystemFragment(), host.getString(R.string.discover_tab_system));
        addFragment(new NavigationFragment(), host.getString(R.string.discover_tab_navigation));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    CharSequence getPageTitle(int position) {
        if (position < 0 || position >= titles.size()) {
            return "";
        }
        return titles.get(position);
    }
}
