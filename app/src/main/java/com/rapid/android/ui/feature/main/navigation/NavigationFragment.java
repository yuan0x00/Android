package com.rapid.android.ui.feature.main.navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.common.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentNavigationBinding;

public class NavigationFragment extends BaseFragment<NavigationViewModel, FragmentNavigationBinding> {

    private NavigationAdapter adapter;

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
        adapter = new NavigationAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadNavigation(true));
    }

    @Override
    protected void loadData() {
        viewModel.loadNavigation(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getNavigationItems().observe(this, list -> {
            adapter.submitList(list);
            binding.emptyView.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            if (!isLoading) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                binding.progressBar.setVisibility(View.GONE);
            } else {
                if (!binding.swipeRefresh.isRefreshing()) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.showLongToast(msg);
            }
        });
    }
}
