package com.rapid.android.feature.main.discover.wechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.core.domain.model.WxChapterBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentWechatBinding;
import com.rapid.android.feature.main.TabNavigator;
import com.rapid.android.ui.common.ContentStateController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WechatFragment extends BaseFragment<WechatViewModel, FragmentWechatBinding> {

    private final List<WxChapterBean> chapters = new ArrayList<>();
    private ContentStateController stateController;
    private WechatTabPagerAdapter pagerAdapter;
    private TabLayoutMediator tabMediator;
    private int selectedIndex;
    private TabNavigator tabNavigator;
    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            selectedIndex = position;
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabNavigator) {
            tabNavigator = (TabNavigator) context;
            if (pagerAdapter != null) {
                pagerAdapter.setTabNavigator(tabNavigator);
            }
        }
    }

    @Override
    public void onDetach() {
        if (pagerAdapter != null) {
            pagerAdapter.setTabNavigator(null);
        }
        tabNavigator = null;
        super.onDetach();
    }

    @Override
    protected WechatViewModel createViewModel() {
        return new ViewModelProvider(this).get(WechatViewModel.class);
    }

    @Override
    protected FragmentWechatBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentWechatBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        pagerAdapter = new WechatTabPagerAdapter(viewModel,
                refreshing -> binding.swipeRefresh.post(() -> binding.swipeRefresh.setRefreshing(refreshing)),
                getDialogController());
        pagerAdapter.setTabNavigator(tabNavigator);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback);

        binding.swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                pagerAdapter.canScrollUp(binding.viewPager.getCurrentItem()));

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (chapters.isEmpty()) {
                viewModel.loadWechatChapters(true);
            } else {
                pagerAdapter.refreshPage(binding.viewPager.getCurrentItem());
            }
        });

        tabMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    WxChapterBean item = pagerAdapter.getItem(position);
                    tab.setText(item != null ? item.getName() : "");
                });
        tabMediator.attach();
    }

    @Override
    protected void loadData() {
        viewModel.loadWechatChapters(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getChapters().observe(this, list -> {
            chapters.clear();
            if (list != null) {
                chapters.addAll(list);
            }
            stateController.setEmpty(chapters.isEmpty());
            updateTabs();
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

    }

    private void updateTabs() {
        List<WxChapterBean> snapshot = chapters.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(chapters);

        pagerAdapter.submitChapters(snapshot);

        binding.viewPager.setVisibility(snapshot.isEmpty() ? View.GONE : View.VISIBLE);
        if (snapshot.isEmpty()) {
            selectedIndex = 0;
            return;
        }

        int targetIndex = Math.min(selectedIndex, snapshot.size() - 1);
        if (targetIndex < 0) {
            targetIndex = 0;
        }
        if (binding.viewPager.getCurrentItem() != targetIndex) {
            final int finalIndex = targetIndex;
            binding.viewPager.post(() -> binding.viewPager.setCurrentItem(finalIndex, false));
        }
    }

    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        if (pagerAdapter != null) {
            pagerAdapter.release();
        }
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        super.onDestroyView();
    }
}
