package com.rapid.android.ui.feature.main.discover.routes;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverRoutesBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.List;

public class DiscoverRoutesFragment extends BaseFragment<DiscoverRoutesViewModel, FragmentDiscoverRoutesBinding> {

    private ContentStateController stateController;

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
        binding.routesGroup.removeAllViews();
        if (list == null || list.isEmpty()) {
            stateController.setEmpty(true);
            return;
        }

        for (CategoryNodeBean node : list) {
            if (node == null || node.getName() == null || node.getName().isEmpty()) {
                continue;
            }
            String target = node.getLink();
            if ((target == null || target.isEmpty()) && node.getChildren() != null && !node.getChildren().isEmpty()) {
                CategoryNodeBean firstChild = node.getChildren().get(0);
                target = firstChild != null ? firstChild.getLink() : null;
            }
            if (target == null || target.isEmpty()) {
                continue;
            }
            Chip chip = new Chip(requireContext());
            chip.setText(node.getName());
            chip.setCheckable(false);
            chip.setEnsureMinTouchTargetSize(false);
            String finalTarget = target;
            chip.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), finalTarget, node.getName()));
            binding.routesGroup.addView(chip);
        }

        boolean empty = binding.routesGroup.getChildCount() == 0;
        stateController.setEmpty(empty);
        if (!empty) {
            stateController.stopRefreshing();
        }
    }
}
