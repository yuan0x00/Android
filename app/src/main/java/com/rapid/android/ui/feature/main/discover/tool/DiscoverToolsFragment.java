package com.rapid.android.ui.feature.main.discover.tool;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverToolsBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

public class DiscoverToolsFragment extends BaseFragment<ToolCenterViewModel, FragmentDiscoverToolsBinding> {

    private ContentStateController stateController;
    private ToolAdapter adapter;

    @Override
    protected ToolCenterViewModel createViewModel() {
        return new ViewModelProvider(this).get(ToolCenterViewModel.class);
    }

    @Override
    protected FragmentDiscoverToolsBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverToolsBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        adapter = new ToolAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
    }

    @Override
    protected void setupObservers() {
        viewModel.getTools().observe(getViewLifecycleOwner(), tools -> {
            adapter.submitList(tools);
            boolean empty = tools == null || tools.isEmpty();
            stateController.setEmpty(empty);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> stateController.stopRefreshing());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }
}
