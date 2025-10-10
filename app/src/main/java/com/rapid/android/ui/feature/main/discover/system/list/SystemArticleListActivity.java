package com.rapid.android.ui.feature.main.discover.system.list;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivitySystemArticleListBinding;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

public class SystemArticleListActivity extends BaseActivity<SystemArticleListViewModel, ActivitySystemArticleListBinding> {

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private SystemArticleListAdapter adapter;
    private ContentStateController stateController;
    private BackToTopController backToTopController;

    public static void start(@NonNull Context context, int categoryId, @NonNull String categoryName) {
        Intent intent = new Intent(context, SystemArticleListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(EXTRA_CATEGORY_NAME, categoryName);
        context.startActivity(intent);
    }

    @Override
    protected SystemArticleListViewModel createViewModel() {
        return new ViewModelProvider(this).get(SystemArticleListViewModel.class);
    }

    @Override
    protected ActivitySystemArticleListBinding createViewBinding() {
        return ActivitySystemArticleListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        String title = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        binding.toolbar.setTitle(title != null ? title : getString(R.string.system_title_article_default));

        adapter = new SystemArticleListAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView, R.dimen.app_spacing_md);
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
        int categoryId = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);
        viewModel.initialize(categoryId);
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
