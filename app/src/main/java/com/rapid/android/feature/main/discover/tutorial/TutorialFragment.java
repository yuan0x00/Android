package com.rapid.android.feature.main.discover.tutorial;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentTutorialBinding;
import com.rapid.android.feature.main.discover.tutorial.list.TutorialArticleListActivity;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class TutorialFragment extends BaseFragment<TutorialViewModel, FragmentTutorialBinding> {

    private ContentStateController stateController;
    private TutorialChapterAdapter adapter;

    @Override
    protected TutorialViewModel createViewModel() {
        return new ViewModelProvider(this).get(TutorialViewModel.class);
    }

    @Override
    protected FragmentTutorialBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTutorialBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);

        adapter = new TutorialChapterAdapter();
        adapter.setOnItemClickListener(this::openTutorial);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);
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
