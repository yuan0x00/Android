package com.rapid.android.feature.main.home.lastproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentLastProjectBinding;
import com.rapid.android.feature.main.TabNavigator;
import com.rapid.android.feature.main.home.ArticleAdapter;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;


public class LastProjectFragment extends BaseFragment<LastProjectViewModel, FragmentLastProjectBinding> {

    private ArticleAdapter articleAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;
    private BackToTopController backToTopController;
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
        viewModel.getProjectItems().observe(this, items -> {
            articleAdapter.submitList(items);
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

        ArticleListBean bean = new ArticleListBean();

        // 创建各模块 Adapter
        articleAdapter = new ArticleAdapter(getDialogController(), bean);

        binding.recyclerView.setAdapter(articleAdapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

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
                    viewModel.loadMore();
                }
            }

        });

    }

    @Override
    public void onDestroyView() {
        if (backToTopController != null) {
            backToTopController.detach();
            backToTopController = null;
        }
        super.onDestroyView();
    }
}
