package com.rapid.android.ui.feature.main.navigation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentNavigationBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

public class NavigationFragment extends BaseFragment<NavigationViewModel, FragmentNavigationBinding> {

    private NavigationAdapter adapter;
    private ContentStateController stateController;

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

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
    }

    @Override
    protected void loadData() {
        viewModel.loadNavigation(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getNavigationItems().observe(this, list -> {
            adapter.submitList(list);
            stateController.setEmpty(list == null || list.isEmpty());
        });

        viewModel.getLoading().observe(this, loading -> {
            stateController.setLoading(Boolean.TRUE.equals(loading));
        });

        UiFeedback.observeError(this, viewModel.getErrorMessage());
    }
}
