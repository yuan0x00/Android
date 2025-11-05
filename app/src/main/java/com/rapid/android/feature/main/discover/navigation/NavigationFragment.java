package com.rapid.android.feature.main.discover.navigation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.NavigationBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentNavigationBinding;
import com.rapid.android.feature.web.ArticleWebViewUtil;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;

import java.util.ArrayList;
import java.util.List;

public class NavigationFragment extends BaseFragment<NavigationViewModel, FragmentNavigationBinding> {

    private final List<NavigationBean> navigationItems = new ArrayList<>();
    private ContentStateController stateController;
    private NavigationCategoryAdapter adapter;

    @Override
    protected NavigationViewModel createViewModel() {
        return new ViewModelProvider(this).get(NavigationViewModel.class);
    }

    @Override
    protected FragmentNavigationBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentNavigationBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadNavigation(true));

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        adapter = new NavigationCategoryAdapter(new NavigationCategoryAdapter.Callbacks() {
            @Override
            public void onCategoryClick(@NonNull NavigationBean category) {
            }

            @Override
            public void onArticleClick(@NonNull ArticleListBean.Data article) {
                ArticleWebViewUtil.start(requireContext(), article);
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);
    }

    @Override
    protected void loadData() {
        viewModel.loadNavigation(false);
    }

    @Override
    protected void setupObservers() {
        viewModel.getNavigationItems().observe(this, list -> {
            navigationItems.clear();
            if (list != null) {
                navigationItems.addAll(list);
            }
            stateController.setEmpty(navigationItems.isEmpty());
            adapter.submitList(new ArrayList<>(navigationItems));
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

    }

    @Override
    public void onDestroyView() {
        binding.recyclerView.setAdapter(null);
        adapter = null;
        super.onDestroyView();
    }
}
