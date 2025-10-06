package com.rapid.android.ui.feature.main.home.lastproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.core.domain.model.ArticleListBean;
import com.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentLastProjectBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.main.home.FeedAdapter;


public class LastProjectFragment extends BaseFragment<LastProjectViewModel, FragmentLastProjectBinding> {

    private FeedAdapter feedAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;

    @Override
    protected LastProjectViewModel createViewModel() {
        return new ViewModelProvider(this).get(LastProjectViewModel.class);
    }

    @Override
    protected FragmentLastProjectBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentLastProjectBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.refresh();
    }

    @Override
    protected void setupObservers() {
        UiFeedback.observeError(this, viewModel.getErrorMessage());
        UiFeedback.observeError(this, viewModel.getPagingError());
        viewModel.getProjectItems().observe(this, items -> {
            feedAdapter.submitList(items);
            boolean empty = items == null || items.isEmpty();
            stateController.setEmpty(empty);
        });
        viewModel.getLoading().observe(this, isLoading -> {
            stateController.setLoading(Boolean.TRUE.equals(isLoading));
        });
        viewModel.getLoadingMore().observe(this, isLoadingMore ->
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(isLoadingMore) ? View.VISIBLE : View.GONE));
    }

    @Override
    protected void initializeViews() {

        // 初始化 RecyclerView
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        ArticleListBean feeds = new ArticleListBean();

        // 创建各模块 Adapter
        feedAdapter = new FeedAdapter(feeds);

        binding.recyclerView.setAdapter(feedAdapter);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                if (layoutManager == null) {
                    return;
                }
                int totalItemCount = layoutManager.getItemCount();
                if (totalItemCount == 0) {
                    return;
                }
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (lastVisible >= totalItemCount - 3) {
                    viewModel.loadMore();
                }
            }
        });

    }
}
