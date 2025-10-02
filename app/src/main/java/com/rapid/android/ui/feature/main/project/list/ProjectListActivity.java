package com.rapid.android.ui.feature.main.project.list;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityProjectListBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

public class ProjectListActivity extends BaseActivity<ProjectListViewModel, ActivityProjectListBinding> {

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    private ProjectListAdapter adapter;
    private ContentStateController stateController;

    public static void start(@NonNull Context context, int categoryId, @NonNull String categoryName) {
        Intent intent = new Intent(context, ProjectListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(EXTRA_CATEGORY_NAME, categoryName);
        context.startActivity(intent);
    }

    @Override
    protected ProjectListViewModel createViewModel() {
        return new ViewModelProvider(this).get(ProjectListViewModel.class);
    }

    @Override
    protected ActivityProjectListBinding createViewBinding() {
        return ActivityProjectListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        String title = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        binding.toolbar.setTitle(title != null ? title : "项目列表");

        adapter = new ProjectListAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
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
        viewModel.getProjectItems().observe(this, items -> {
            adapter.submitNewList(items);
            stateController.setEmpty(items == null || items.isEmpty());
        });

        viewModel.getLoading().observe(this, isLoading -> {
            stateController.setLoading(Boolean.TRUE.equals(isLoading));
        });

        viewModel.getLoadingMore().observe(this, isLoadingMore -> {
            binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(isLoadingMore) ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        UiFeedback.observeError(this, viewModel.getErrorMessage());
        UiFeedback.observeError(this, viewModel.getPagingError());
    }

    @Override
    protected void loadData() {
        int cid = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);
        viewModel.initialize(cid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.swipeRefresh.setOnRefreshListener(null);
        binding.recyclerView.setAdapter(null);
    }
}
