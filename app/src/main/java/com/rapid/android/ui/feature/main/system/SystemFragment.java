package com.rapid.android.ui.feature.main.system;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.common.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentSystemBinding;

public class SystemFragment extends BaseFragment<SystemViewModel, FragmentSystemBinding> {

    private SystemCategoryTreeAdapter adapter;

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
        adapter = new SystemCategoryTreeAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadSystem(true));
    }

    @Override
    protected void loadData() {
        viewModel.loadSystem(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getCategories().observe(this, list -> {
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
