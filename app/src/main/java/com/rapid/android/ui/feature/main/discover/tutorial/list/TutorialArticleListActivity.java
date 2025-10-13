package com.rapid.android.ui.feature.main.discover.tutorial.list;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityTutorialArticleListBinding;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

public class TutorialArticleListActivity extends BaseActivity<TutorialArticleListViewModel, ActivityTutorialArticleListBinding> {

    private static final String EXTRA_TUTORIAL_ID = "extra_tutorial_id";
    private static final String EXTRA_TUTORIAL_NAME = "extra_tutorial_name";

    private TutorialArticleListAdapter adapter;
    private ContentStateController stateController;
    private BackToTopController backToTopController;

    public static void start(@NonNull Context context, int tutorialId, @NonNull String tutorialName) {
        Intent intent = new Intent(context, TutorialArticleListActivity.class);
        intent.putExtra(EXTRA_TUTORIAL_ID, tutorialId);
        intent.putExtra(EXTRA_TUTORIAL_NAME, tutorialName);
        context.startActivity(intent);
    }

    @Override
    protected TutorialArticleListViewModel createViewModel() {
        return new ViewModelProvider(this).get(TutorialArticleListViewModel.class);
    }

    @Override
    protected ActivityTutorialArticleListBinding createViewBinding() {
        return ActivityTutorialArticleListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        String title = getIntent().getStringExtra(EXTRA_TUTORIAL_NAME);
        binding.toolbar.setTitle(title != null ? title : getString(R.string.tutorial_title_default));

        adapter = new TutorialArticleListAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView);

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0) {
                    return;
                }
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (!(layoutManager instanceof LinearLayoutManager)) {
                    return;
                }
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisible = linearLayoutManager.findLastVisibleItemPosition();
                if (totalItemCount == 0) {
                    return;
                }
                if (lastVisible >= totalItemCount - 3) {
                    viewModel.loadMore();
                }
            }
        });
    }

    @Override
    protected void setupObservers() {
        viewModel.getArticleItems().observe(this, items -> {
            adapter.submitNewList(items);
            stateController.setEmpty(items == null || items.isEmpty());
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        viewModel.getLoadingMore().observe(this, loading ->
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loading)
                        ? android.view.View.VISIBLE : android.view.View.GONE));

        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getPagingError());
    }

    @Override
    protected void loadData() {
        int tutorialId = getIntent().getIntExtra(EXTRA_TUTORIAL_ID, -1);
        viewModel.initialize(tutorialId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.swipeRefresh.setOnRefreshListener(null);
            binding.recyclerView.setAdapter(null);
        }
        if (backToTopController != null) {
            backToTopController.detach();
            backToTopController = null;
        }
    }
}
