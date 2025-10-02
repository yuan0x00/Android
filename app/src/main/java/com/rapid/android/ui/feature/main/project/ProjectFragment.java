package com.rapid.android.ui.feature.main.project;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentProjectBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.main.project.list.ProjectListActivity;

public class ProjectFragment extends BaseFragment<ProjectViewModel, FragmentProjectBinding> {

    private ProjectCategoryAdapter adapter;
    private ContentStateController stateController;

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
        adapter = new ProjectCategoryAdapter(category ->
                ProjectListActivity.start(requireContext(), category.getId(), category.getName()));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadProjects(true));

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
    }

    @Override
    protected void loadData() {
        viewModel.loadProjects(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getProjectCategories().observe(this, list -> {
            adapter.submitList(list);
            stateController.setEmpty(list == null || list.isEmpty());
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        UiFeedback.observeError(this, viewModel.getErrorMessage());
    }
}
