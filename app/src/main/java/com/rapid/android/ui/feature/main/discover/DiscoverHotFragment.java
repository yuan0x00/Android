package com.rapid.android.ui.feature.main.discover;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.model.PopularColumnBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverHotBinding;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.List;

public class DiscoverHotFragment extends BaseFragment<DiscoverHotViewModel, FragmentDiscoverHotBinding> {

    @Override
    protected DiscoverHotViewModel createViewModel() {
        return new ViewModelProvider(this).get(DiscoverHotViewModel.class);
    }

    @Override
    protected FragmentDiscoverHotBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverHotBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoading().observe(this, loading -> binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(loading)));
        viewModel.getPopularWenda().observe(this, this::renderWenda);
        viewModel.getPopularColumns().observe(this, this::renderColumns);
        viewModel.getPopularRoutes().observe(this, this::renderRoutes);
        viewModel.getErrorMessage().observe(this, msg -> binding.swipeRefresh.setRefreshing(false));
        UiFeedback.observeError(this, getDialogController(), viewModel.getErrorMessage());
    }

    private void renderWenda(List<ArticleListBean.Data> list) {
        binding.sectionWenda.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        binding.wendaContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            toggleEmptyState();
            return;
        }
        toggleEmptyState();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int count = Math.min(list.size(), 5);
        for (int i = 0; i < count; i++) {
            ArticleListBean.Data item = list.get(i);
            View itemView = inflater.inflate(android.R.layout.simple_list_item_2, binding.wendaContainer, false);
            android.widget.TextView title = itemView.findViewById(android.R.id.text1);
            android.widget.TextView subtitle = itemView.findViewById(android.R.id.text2);
            title.setText(item.getTitle());
            if (subtitle != null) {
                String author = item.getAuthor();
                if (author == null || author.isEmpty()) {
                    author = item.getShareUser();
                }
                subtitle.setText(author != null ? author : "");
            }
            itemView.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), item));
            binding.wendaContainer.addView(itemView);
        }
    }

    private void renderColumns(List<PopularColumnBean> list) {
        binding.sectionColumns.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        binding.columnContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            toggleEmptyState();
            return;
        }
        toggleEmptyState();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int count = Math.min(list.size(), 5);
        for (int i = 0; i < count; i++) {
            PopularColumnBean item = list.get(i);
            View itemView = inflater.inflate(android.R.layout.simple_list_item_2, binding.columnContainer, false);
            android.widget.TextView title = itemView.findViewById(android.R.id.text1);
            android.widget.TextView subtitle = itemView.findViewById(android.R.id.text2);
            title.setText(item.getName());
            if (subtitle != null) {
                subtitle.setText(item.getChapterName());
            }
            itemView.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), item.getUrl(), item.getName()));
            binding.columnContainer.addView(itemView);
        }
    }

    private void renderRoutes(List<CategoryNodeBean> list) {
        binding.sectionRoutes.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
        binding.routeContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            toggleEmptyState();
            return;
        }
        toggleEmptyState();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int count = Math.min(list.size(), 6);
        for (int i = 0; i < count; i++) {
            CategoryNodeBean item = list.get(i);
            View itemView = inflater.inflate(android.R.layout.simple_list_item_1, binding.routeContainer, false);
            android.widget.TextView title = itemView.findViewById(android.R.id.text1);
            title.setText(item.getName());
            itemView.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), item.getLink(), item.getName()));
            binding.routeContainer.addView(itemView);
        }
    }

    private void toggleEmptyState() {
        boolean allEmpty = isEmptyBinding();
        binding.emptyView.setVisibility(allEmpty ? View.VISIBLE : View.GONE);
    }

    private boolean isEmptyBinding() {
        return binding.wendaContainer.getChildCount() == 0
                && binding.columnContainer.getChildCount() == 0
                && binding.routeContainer.getChildCount() == 0;
    }
}
