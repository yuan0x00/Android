package com.rapid.android.ui.feature.main.discover.wenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverWendaBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.List;

public class DiscoverWendaFragment extends BaseFragment<DiscoverWendaViewModel, FragmentDiscoverWendaBinding> {

    private ContentStateController stateController;

    @Override
    protected DiscoverWendaViewModel createViewModel() {
        return new ViewModelProvider(this).get(DiscoverWendaViewModel.class);
    }

    @Override
    protected FragmentDiscoverWendaBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDiscoverWendaBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);
        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
    }

    @Override
    protected void setupObservers() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));
        viewModel.getItems().observe(getViewLifecycleOwner(), this::renderItems);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                stateController.stopRefreshing();
            }
        });
        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }

    private void renderItems(List<ArticleListBean.Data> list) {
        binding.wendaContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            stateController.setEmpty(true);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int count = Math.min(list.size(), 10);
        for (int i = 0; i < count; i++) {
            ArticleListBean.Data item = list.get(i);
            if (item == null) {
                continue;
            }
            View view = inflater.inflate(android.R.layout.simple_list_item_2, binding.wendaContainer, false);
            android.widget.TextView title = view.findViewById(android.R.id.text1);
            android.widget.TextView subtitle = view.findViewById(android.R.id.text2);
            title.setText(item.getTitle());
            if (subtitle != null) {
                String author = item.getAuthor();
                if (author == null || author.isEmpty()) {
                    author = item.getShareUser();
                }
                subtitle.setText(author != null ? author : "");
            }
            view.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), item));
            binding.wendaContainer.addView(view);
        }

        boolean empty = binding.wendaContainer.getChildCount() == 0;
        stateController.setEmpty(empty);
        if (!empty) {
            stateController.stopRefreshing();
        }
    }
}
