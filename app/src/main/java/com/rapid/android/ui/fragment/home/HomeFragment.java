package com.rapid.android.ui.fragment.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.databinding.FragmentHomeBinding;
import com.rapid.android.ui.fragment.home.fragments.recommend.RecommendFragment;
import com.rapid.android.ui.fragment.home.fragments.topic.TopicFragment;
import com.rapid.core.base.ui.BaseFragment;

public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {

    @Override
    protected HomeViewModel createViewModel() {
        return new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Override
    protected FragmentHomeBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
    }

    @Override
    protected void initializeViews() {
        setupViewPager();
    }

    private void setupViewPager() {
        HomeViewPagerAdapter adapter = new HomeViewPagerAdapter(requireActivity());
        adapter.addFragment(new RecommendFragment(), "推荐");
        adapter.addFragment(new TopicFragment(), "话题");
        binding.viewPager.setAdapter(adapter);

        // 将 TabLayout 与 ViewPager2 绑定
        new TabLayoutMediator(binding.topTab, binding.viewPager, (tab, position) -> tab.setText(adapter.getPageTitle(position))).attach();
    }

}