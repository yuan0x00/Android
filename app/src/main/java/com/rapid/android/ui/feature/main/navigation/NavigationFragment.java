package com.rapid.android.ui.feature.main.navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.rapid.android.core.domain.model.NavigationBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentNavigationBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NavigationFragment extends BaseFragment<NavigationViewModel, FragmentNavigationBinding> {

    private final List<NavigationBean> navigationItems = new ArrayList<>();
    private ContentStateController stateController;
    private NavigationTabPagerAdapter pagerAdapter;
    private TabLayoutMediator tabMediator;
    private int selectedIndex;
    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            selectedIndex = position;
        }
    };

    @Override
    protected NavigationViewModel createViewModel() {
        return new ViewModelProvider(this).get(NavigationViewModel.class);
    }

    @Override
    protected FragmentNavigationBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentNavigationBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadNavigation(true));

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        pagerAdapter = new NavigationTabPagerAdapter();
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback);

        binding.swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                pagerAdapter.canScrollUp(binding.viewPager.getCurrentItem()));

        tabMediator = new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    NavigationBean item = pagerAdapter.getItem(position);
                    tab.setText(item != null ? item.getName() : "");
                });
        tabMediator.attach();
    }

    @Override
    protected void loadData() {
        viewModel.loadNavigation(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getNavigationItems().observe(this, list -> {
            navigationItems.clear();
            if (list != null) {
                navigationItems.addAll(list);
            }
            stateController.setEmpty(navigationItems.isEmpty());
            updateTabs();
        });

        viewModel.getLoading().observe(this, loading -> {
            stateController.setLoading(Boolean.TRUE.equals(loading));
        });

        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
    }

    private void updateTabs() {
        List<NavigationBean> snapshot = navigationItems.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(navigationItems);

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
            final int finalTarget = targetIndex;
            binding.viewPager.post(() -> binding.viewPager.setCurrentItem(finalTarget, false));
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
