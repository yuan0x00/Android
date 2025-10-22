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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class RecommendFragment extends BaseFragment<RecommendViewModel, FragmentRecommandBinding> {

    private BannerAdapter bannerAdapter;
    private final Set<Integer> topArticleIds = new LinkedHashSet<>();
    private ArticleAdapter articleAdapter;
    private HomePopularSectionRowAdapter popularSectionAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;
    private BackToTopController backToTopController;
    private TabNavigator tabNavigator;
    private ArticleAdapter topArticleAdapter;

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
        viewModel.getArticleItems().observe(this, this::renderArticleItems);
        viewModel.getTopArticles().observe(this, this::renderTopArticles);
        viewModel.getArticleEmptyState().observe(this, isEmpty -> updateEmptyState());
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
        topArticleAdapter = new ArticleAdapter(getDialogController(), articleListBean, true);
        articleAdapter = new ArticleAdapter(getDialogController(), articleListBean);

        ConcatAdapter concatAdapter = new ConcatAdapter(bannerAdapter, popularSectionAdapter, topArticleAdapter, articleAdapter);
        binding.recyclerView.setAdapter(concatAdapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refreshAll());

        backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView, tabNavigator);

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

    private void renderTopArticles(List<ArticleListBean.Data> articles) {
        if (topArticleAdapter == null) {
            return;
        }
        topArticleIds.clear();
        List<ArticleListBean.Data> tops = new ArrayList<>();
        if (articles != null) {
            for (ArticleListBean.Data item : articles) {
                if (item == null) {
                    continue;
                }
                topArticleIds.add(item.getId());
                tops.add(item);
            }
        }
        topArticleAdapter.submitList(tops);
        renderArticleItems(viewModel.getArticleItems().getValue());
        updateEmptyState();
    }

    private void renderArticleItems(List<ArticleListBean.Data> items) {
        if (articleAdapter == null) {
            return;
        }
        List<ArticleListBean.Data> filtered = new ArrayList<>();
        if (items != null) {
            for (ArticleListBean.Data item : items) {
                if (item == null) {
                    continue;
                }
                if (!topArticleIds.contains(item.getId())) {
                    filtered.add(item);
                }
            }
        }
        articleAdapter.submitList(filtered);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (stateController == null) {
            return;
        }
        boolean noTop = topArticleAdapter == null || topArticleAdapter.getItemCount() == 0;
        Boolean pagingEmpty = viewModel.getArticleEmptyState().getValue();
        boolean empty = Boolean.TRUE.equals(pagingEmpty) && noTop;
        stateController.setEmpty(empty);
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
