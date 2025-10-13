package com.rapid.android.ui.feature.main.discover.routes;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverRoutesBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class DiscoverRoutesFragment extends BaseFragment<DiscoverRoutesViewModel, FragmentDiscoverRoutesBinding> {

    private ContentStateController stateController;
    private RouteAdapter adapter;

    @Override
    protected DiscoverRoutesViewModel createViewModel() {
        return new ViewModelProvider(this).get(DiscoverRoutesViewModel.class);
    }

    @Override
    protected FragmentDiscoverRoutesBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverRoutesBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        adapter = new RouteAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));
        viewModel.getRoutes().observe(getViewLifecycleOwner(), this::renderRoutes);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                stateController.stopRefreshing();
            }
        });
        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }

    private void renderRoutes(List<CategoryNodeBean> list) {
        adapter.submitList(list);
        boolean empty = list == null || list.isEmpty();
        stateController.setEmpty(empty);
        stateController.stopRefreshing();
    }
}
