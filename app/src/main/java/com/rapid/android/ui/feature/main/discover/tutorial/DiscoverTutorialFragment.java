package com.rapid.android.ui.feature.main.discover.tutorial;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverTutorialBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.main.discover.tutorial.list.TutorialArticleListActivity;

import java.util.List;

public class DiscoverTutorialFragment extends BaseFragment<DiscoverTutorialViewModel, FragmentDiscoverTutorialBinding> {

    private ContentStateController stateController;
    private TutorialChapterAdapter adapter;

    @Override
    protected DiscoverTutorialViewModel createViewModel() {
        return new ViewModelProvider(this).get(DiscoverTutorialViewModel.class);
    }

    @Override
    protected FragmentDiscoverTutorialBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverTutorialBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);

        adapter = new TutorialChapterAdapter();
        adapter.setOnItemClickListener(this::openTutorial);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));
        viewModel.getColumns().observe(getViewLifecycleOwner(), this::renderChapters);
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

    private void renderChapters(List<CategoryNodeBean> chapters) {
        adapter.submitList(chapters);
        boolean empty = chapters == null || chapters.isEmpty();
        stateController.setEmpty(empty);
        if (!empty) {
            stateController.stopRefreshing();
        }
    }

    private void openTutorial(CategoryNodeBean chapter) {
        if (chapter == null) {
            return;
        }
        TutorialArticleListActivity.start(requireContext(), chapter.getId(), chapter.getName());
    }
}
