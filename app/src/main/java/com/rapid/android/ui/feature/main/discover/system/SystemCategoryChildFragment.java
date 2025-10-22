package com.rapid.android.ui.feature.main.discover.system;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentSystemCategoryChildBinding;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.main.discover.system.list.SystemArticleListAdapter;
import com.rapid.android.ui.feature.main.discover.system.list.SystemArticleListViewModel;

public class SystemCategoryChildFragment extends BaseFragment<SystemArticleListViewModel, FragmentSystemCategoryChildBinding> {

    private static final String ARG_CATEGORY_ID = "arg_category_id";
    private static final String ARG_CATEGORY_NAME = "arg_category_name";

    private SystemArticleListAdapter adapter;
    private ContentStateController stateController;
    private BackToTopController backToTopController;
    private int categoryId;

    public static SystemCategoryChildFragment newInstance(int categoryId, @Nullable String name) {
        SystemCategoryChildFragment fragment = new SystemCategoryChildFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected SystemArticleListViewModel createViewModel() {
        return new ViewModelProvider(this).get(SystemArticleListViewModel.class);
    }

    @Override
    protected FragmentSystemCategoryChildBinding createViewBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentSystemCategoryChildBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        adapter = new SystemArticleListAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

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
    protected void loadData() {
        Bundle args = requireArguments();
        categoryId = args.getInt(ARG_CATEGORY_ID, -1);
        viewModel.initialize(categoryId);
    }

    @Override
    protected void setupObservers() {
        viewModel.getArticleItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitNewList(items);
            stateController.setEmpty(items == null || items.isEmpty());
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        viewModel.getLoadingMore().observe(getViewLifecycleOwner(), loading ->
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loading)
                        ? View.VISIBLE : View.GONE));

        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getPagingError());
    }

    boolean canScrollUp() {
        return binding.recyclerView.canScrollVertically(-1);
    }

    @Override
    public void onDestroyView() {
        if (backToTopController != null) {
            backToTopController.detach();
            backToTopController = null;
        }
        binding.recyclerView.setAdapter(null);
        adapter = null;
        super.onDestroyView();
    }
}
