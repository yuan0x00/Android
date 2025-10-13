package com.rapid.android.ui.feature.main.home.recommend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentRecommandBinding;
import com.rapid.android.ui.common.*;
import com.rapid.android.ui.feature.main.TabNavigator;
import com.rapid.android.ui.feature.main.home.ArticleAdapter;
import com.rapid.android.ui.feature.main.home.BannerAdapter;
import com.rapid.android.utils.AppPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class RecommendFragment extends BaseFragment<RecommendViewModel, FragmentRecommandBinding> {

    private static final int BOTTOM_BAR_SCROLL_THRESHOLD = 8;

    private BannerAdapter bannerAdapter;
    private ArticleAdapter articleAdapter;
    private HomePopularSectionRowAdapter popularSectionAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;
    private BackToTopController backToTopController;
    private final List<ArticleListBean.Data> topArticleItems = new ArrayList<>();
    private List<ArticleListBean.Data> articleItems = new ArrayList<>();
    private TabNavigator tabNavigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabNavigator) {
            tabNavigator = (TabNavigator) context;
        }
    }

    @Override
    public void onDetach() {
        tabNavigator = null;
        super.onDetach();
    }

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
        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getPagingError());
        viewModel.getBannerList().observe(this, bannerList -> {
            if (bannerList != null) {
                bannerAdapter.setData(bannerList);
            }
        });
        viewModel.getPopularSections().observe(this, sections -> {
            if (popularSectionAdapter != null) {
                popularSectionAdapter.submitList(sections);
            }
        });
        viewModel.getArticleItems().observe(this, this::updateArticleItems);
        viewModel.getTopArticles().observe(this, this::updateTopArticles);
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

        // RecyclerView 性能优化
        RecyclerViewOptimizer.applyOptimizations(binding.recyclerView);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        ArticleListBean articleListBean = new ArticleListBean();

        bannerAdapter = new BannerAdapter(new ArrayList<>());
        popularSectionAdapter = new HomePopularSectionRowAdapter();
        articleAdapter = new ArticleAdapter(getDialogController(), articleListBean);

        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, popularSectionAdapter, articleAdapter);
        binding.recyclerView.setAdapter(concatAdapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView);

        refreshArticleDisplay();

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshAll());

        backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView);

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                handleBottomBarVisibility(recyclerView, dy);
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

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    handleBottomBarVisibilityOnIdle(recyclerView);
                }
            }
        });
    }

    private void handleBottomBarVisibility(@NonNull RecyclerView recyclerView, int dy) {
        if (tabNavigator == null) {
            return;
        }
        if (!AppPreferences.isAutoHideBottomBarEnabled()) {
            if (!tabNavigator.isBottomBarVisible()) {
                tabNavigator.showBottomBar(false);
            }
            return;
        }
        if (dy > BOTTOM_BAR_SCROLL_THRESHOLD) {
            tabNavigator.hideBottomBar(true);
        } else if (dy < -BOTTOM_BAR_SCROLL_THRESHOLD) {
            tabNavigator.showBottomBar(true);
        }
        if (!recyclerView.canScrollVertically(-1)) {
            tabNavigator.showBottomBar(true);
        }
    }

    private void handleBottomBarVisibilityOnIdle(@NonNull RecyclerView recyclerView) {
        if (tabNavigator == null) {
            return;
        }
        if (!AppPreferences.isAutoHideBottomBarEnabled()) {
            if (!tabNavigator.isBottomBarVisible()) {
                tabNavigator.showBottomBar(false);
            }
            return;
        }
        if (!recyclerView.canScrollVertically(-1)) {
            tabNavigator.showBottomBar(true);
        }
    }

    private void updateArticleItems(List<ArticleListBean.Data> items) {
        articleItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
        refreshArticleDisplay();
    }

    private void updateTopArticles(List<ArticleListBean.Data> articles) {
        topArticleItems.clear();
        if (articles != null) {
            topArticleItems.addAll(articles);
        }
        refreshArticleDisplay();
    }

    private void refreshArticleDisplay() {
        if (articleAdapter == null) {
            return;
        }
        List<ArticleListBean.Data> combined = buildCombinedArticleList();
        Set<Integer> topIds = new LinkedHashSet<>();
        for (ArticleListBean.Data item : topArticleItems) {
            if (item == null) {
                continue;
            }
            topIds.add(item.getId());
        }
        articleAdapter.submitList(combined);
        articleAdapter.setTopArticleIds(topIds);
        if (stateController != null) {
            stateController.setEmpty(combined.isEmpty());
        }
    }

    private List<ArticleListBean.Data> buildCombinedArticleList() {
        List<ArticleListBean.Data> combined = new ArrayList<>();
        Set<Integer> seenIds = new LinkedHashSet<>();
        for (ArticleListBean.Data item : topArticleItems) {
            if (item == null) {
                continue;
            }
            if (seenIds.add(item.getId())) {
                combined.add(item);
            }
        }
        for (ArticleListBean.Data item : articleItems) {
            if (item == null) {
                continue;
            }
            if (seenIds.add(item.getId())) {
                combined.add(item);
            }
        }
        return combined;
    }

    @Override
    public void onDestroyView() {
        if (tabNavigator != null) {
            tabNavigator.showBottomBar(false);
        }
        if (backToTopController != null) {
            backToTopController.detach();
            backToTopController = null;
        }
        super.onDestroyView();
    }
}
