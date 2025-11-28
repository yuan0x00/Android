package com.rapid.android.feature.main.home.recommend;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PopularSectionPagerAdapter extends FragmentStateAdapter {

    private final List<PopularSection> sections = new ArrayList<>();
    private final Map<Integer, PopularSectionFragment> fragmentCache = new HashMap<>();

    PopularSectionPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    void submitList(List<PopularSection> data) {
        sections.clear();
        if (data != null) {
            sections.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PopularSectionFragment fragment = PopularSectionFragment.newInstance(sections.get(position));
        fragmentCache.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    String getTitle(int position) {
        if (position >= 0 && position < sections.size()) {
            PopularSection section = sections.get(position);
            return section != null ? section.getTitle() : "";
        }
        return "";
    }

    Fragment getFragmentAt(int position) {
        return fragmentCache.get(position);
    }

}
