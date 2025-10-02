package com.rapid.android.ui.feature.main.home.recommend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.core.ui.presentation.BaseFragment;
import com.lib.domain.model.ArticleListBean;
import com.rapid.android.databinding.FragmentRecommandBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.main.home.BannerAdapter;
import com.rapid.android.ui.feature.main.home.FeedAdapter;

import java.util.ArrayList;


public class RecommendFragment extends BaseFragment<RecommendViewModel, FragmentRecommandBinding> {

    private BannerAdapter bannerAdapter;
    private FeedAdapter feedAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;

    @Override
    protected RecommendViewModel createViewModel() {
        return new ViewModelProvider(this).get(RecommendViewModel.class);
    }

    @Override
    protected FragmentRecommandBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentRecommandBinding.inflate(inflater, container, false);
    }

    @Override
    protected void loadData() {
        super.loadData();
        viewModel.refreshAll();
    }

    @Override
    protected void setupObservers() {
        UiFeedback.observeError(this, viewModel.getErrorMessage());
        UiFeedback.observeError(this, viewModel.getPagingError());
        viewModel.getBannerList().observe(this, bannerList -> {
            if (bannerList != null) {
                bannerAdapter.setData(bannerList);
            }
        });
        viewModel.getArticleItems().observe(this, items -> {
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

        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        ArticleListBean feeds = new ArticleListBean();

        bannerAdapter = new BannerAdapter(new ArrayList<>());
        feedAdapter = new FeedAdapter(feeds);

        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, feedAdapter);
        binding.recyclerView.setAdapter(concatAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshAll());

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
                    viewModel.loadMoreArticles();
                }
            }
        });
    }
}
