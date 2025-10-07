package com.rapid.android.ui.feature.main.mine.favorite;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityFavoriteBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

public class FavoriteActivity extends BaseActivity<FavoriteViewModel, ActivityFavoriteBinding> {

    private FavoriteAdapter adapter;
    private ContentStateController stateController;

    public static void start(@NonNull Context context) {
        context.startActivity(new Intent(context, FavoriteActivity.class));
    }

    @Override
    protected FavoriteViewModel createViewModel() {
        return new ViewModelProvider(this).get(FavoriteViewModel.class);
    }

    @Override
    protected ActivityFavoriteBinding createViewBinding() {
        return ActivityFavoriteBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(R.string.favorite_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new FavoriteAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);

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
        viewModel.getFavoriteItems().observe(this, items -> {
            adapter.submitNewList(items);
            stateController.setEmpty(items == null || items.isEmpty());
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        viewModel.getLoadingMore().observe(this, loadingMore ->
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loadingMore)
                        ? android.view.View.VISIBLE : android.view.View.GONE));

        UiFeedback.observeError(this, viewModel.getErrorMessage());
        UiFeedback.observeError(this, viewModel.getPagingError());
    }

    @Override
    protected void loadData() {
        viewModel.initialize();
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.swipeRefresh.setOnRefreshListener(null);
            binding.recyclerView.setAdapter(null);
        }
        super.onDestroy();
    }
}
