package com.rapid.android.ui.feature.main.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.common.ToastUtils;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentProjectBinding;

public class ProjectFragment extends BaseFragment<ProjectViewModel, FragmentProjectBinding> {

    private ProjectCategoryTreeAdapter adapter;

    @Override
    protected ProjectViewModel createViewModel() {
        return new ViewModelProvider(this).get(ProjectViewModel.class);
    }

    @Override
    protected FragmentProjectBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentProjectBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        adapter = new ProjectCategoryTreeAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadProjects(true));
    }

    @Override
    protected void loadData() {
        viewModel.loadProjects(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getProjectCategories().observe(this, list -> {
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
