package com.rapid.android.ui.feature.main.system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentSystemBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SystemFragment extends BaseFragment<SystemViewModel, FragmentSystemBinding> {

    private final List<CategoryNodeBean> categories = new ArrayList<>();
    private ContentStateController stateController;
    private int selectedIndex;
    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            selectedIndex = position;
        }
    };
    private SystemTabPagerAdapter pagerAdapter;
    private TabLayoutMediator tabMediator;

    @Override
    protected SystemViewModel createViewModel() {
        return new ViewModelProvider(this).get(SystemViewModel.class);
    }

    @Override
    protected FragmentSystemBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSystemBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadSystem(true));

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        pagerAdapter = new SystemTabPagerAdapter();
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback);

        binding.swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                pagerAdapter.canScrollUp(binding.viewPager.getCurrentItem()));

        tabMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    CategoryNodeBean item = pagerAdapter.getItem(position);
                    tab.setText(item != null ? item.getName() : "");
                });
        tabMediator.attach();
    }

    @Override
    protected void loadData() {
        viewModel.loadSystem(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getCategories().observe(this, list -> {
            categories.clear();
            if (list != null) {
                categories.addAll(list);
            }

            stateController.setEmpty(categories.isEmpty());
            updateTabs();
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        UiFeedback.observeError(this, viewModel.getErrorMessage());
    }

    private void updateTabs() {
        List<CategoryNodeBean> snapshot = categories.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(categories);

        pagerAdapter.submitList(snapshot);

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
            final int finalTargetIndex = targetIndex;
            binding.viewPager.post(() -> binding.viewPager.setCurrentItem(finalTargetIndex, false));
        }
    }

    @Override
    public void onDestroyView() {
        if (tabMediator != null) {
            tabMediator.detach();
            tabMediator = null;
        }
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        super.onDestroyView();
    }
}
