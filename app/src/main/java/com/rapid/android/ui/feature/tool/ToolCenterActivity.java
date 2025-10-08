package com.rapid.android.ui.feature.tool;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityToolCenterBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

public class ToolCenterActivity extends BaseActivity<ToolCenterViewModel, ActivityToolCenterBinding> {

    private ContentStateController stateController;
    private ToolAdapter adapter;

    @Override
    protected ToolCenterViewModel createViewModel() {
        return new ViewModelProvider(this).get(ToolCenterViewModel.class);
    }

    @Override
    protected ActivityToolCenterBinding createViewBinding() {
        return ActivityToolCenterBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setupToolbar();
        setupList();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupList() {
        adapter = new ToolAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
    }

    @Override
    protected void setupObservers() {
        viewModel.getTools().observe(this, tools -> {
            adapter.submitList(tools);
            boolean empty = tools == null || tools.isEmpty();
            stateController.setEmpty(empty);
        });

        viewModel.getLoading().observe(this, isLoading -> stateController.setLoading(Boolean.TRUE.equals(isLoading)));

        viewModel.getErrorMessage().observe(this, message -> stateController.stopRefreshing());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }
}
