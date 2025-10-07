package com.rapid.android.ui.feature.main.home.plaza;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentPlazaBinding;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.main.home.FeedAdapter;

public class PlazaFragment extends BaseFragment<PlazaViewModel, FragmentPlazaBinding> {

    private FeedAdapter feedAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;
    private BackToTopController backToTopController;

    @Override
    protected PlazaViewModel createViewModel() {
        return new ViewModelProvider(this).get(PlazaViewModel.class);
    }

    @Override
    protected FragmentPlazaBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentPlazaBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        feedAdapter = new FeedAdapter(new ArticleListBean());
        binding.recyclerView.setAdapter(feedAdapter);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView);

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
    protected void loadData() {
        viewModel.refresh();
    }

    @Override
    protected void setupObservers() {
        UiFeedback.observeError(this, viewModel.getErrorMessage());
        UiFeedback.observeError(this, viewModel.getPagingError());

        viewModel.getPlazaItems().observe(this, items -> {
            feedAdapter.submitList(items);
            stateController.setEmpty(items == null || items.isEmpty());
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        viewModel.getLoadingMore().observe(this, loading ->
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loading)
                        ? View.VISIBLE : View.GONE));
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
