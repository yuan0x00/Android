package com.rapid.android.ui.feature.main.system;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentSystemBinding;
import com.rapid.android.ui.common.CategoryTreeAdapter;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

public class SystemFragment extends BaseFragment<SystemViewModel, FragmentSystemBinding> {

    private CategoryTreeAdapter adapter;
    private ContentStateController stateController;

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
        adapter = new CategoryTreeAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadSystem(true));

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
    }

    @Override
    protected void loadData() {
        viewModel.loadSystem(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getCategories().observe(this, list -> {
            adapter.submitList(list);
            stateController.setEmpty(list == null || list.isEmpty());
        });

        viewModel.getLoading().observe(this, loading -> {
            stateController.setLoading(Boolean.TRUE.equals(loading));
        });

        UiFeedback.observeError(this, viewModel.getErrorMessage());
    }
}
