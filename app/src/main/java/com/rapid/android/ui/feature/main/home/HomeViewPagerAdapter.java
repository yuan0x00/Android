package com.rapid.android.ui.feature.main.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rapid.android.ui.feature.main.home.lastproject.LastProjectFragment;
import com.rapid.android.ui.feature.main.home.recommend.RecommendFragment;

import java.util.ArrayList;
import java.util.List;



public class HomeViewPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final FragmentActivity host;

    public HomeViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.host = fragmentActivity;
    }

    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }

    public void addFragments() {
        addFragment(new RecommendFragment(), host.getString(com.rapid.android.R.string.home_page_recommend));
        addFragment(new LastProjectFragment(), host.getString(com.rapid.android.R.string.home_page_latest));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public String getPageTitle(int position) {
        return titles.get(position);
    }
}
