package com.rapid.android.feature.main.discover.harmony;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.model.HarmonyIndexBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentHarmonyBinding;
import com.rapid.android.feature.web.ArticleWebViewActivity;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class HarmonyFragment extends BaseFragment<HarmonyViewModel, FragmentHarmonyBinding> {

    private ContentStateController stateController;

    @Override
    protected HarmonyViewModel createViewModel() {
        return new ViewModelProvider(this).get(HarmonyViewModel.class);
    }

    @Override
    protected FragmentHarmonyBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentHarmonyBinding.inflate(inflater, container, false);
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
        viewModel.getHarmonyIndex().observe(getViewLifecycleOwner(), this::renderHarmony);
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

    private void renderHarmony(HarmonyIndexBean data) {
        if (data == null) {
            stateController.setEmpty(true);
            return;
        }

        boolean hasLinks = renderSection(data.getLinks(), binding.sectionLinks, binding.chipGroupLinks);
        boolean hasOpenSource = renderSection(data.getOpenSources(), binding.sectionOpenSource, binding.chipGroupOpenSource);
        boolean hasTools = renderSection(data.getTools(), binding.sectionTools, binding.chipGroupTools);

        boolean empty = !hasLinks && !hasOpenSource && !hasTools;
        stateController.setEmpty(empty);
        if (!empty) {
            stateController.stopRefreshing();
        }
    }

    private boolean renderSection(CategoryNodeBean node, View section, com.google.android.material.chip.ChipGroup group) {
        group.removeAllViews();
        if (node == null) {
            section.setVisibility(View.GONE);
            return false;
        }

        boolean hasItems = false;
        List<CategoryNodeBean> children = node.getChildren();
        if (children != null) {
            for (CategoryNodeBean child : children) {
                if (child == null || child.getName() == null || child.getName().isEmpty()) {
                    continue;
                }
                String target = child.getLink();
                if (target == null || target.isEmpty()) {
                    continue;
                }
                group.addView(buildChip(child.getName(), target));
                hasItems = true;
            }
        }

        if (!hasItems) {
            List<ArticleListBean.Data> articles = node.getArticleList();
            if (articles != null) {
                for (ArticleListBean.Data article : articles) {
                    if (article == null || article.getTitle() == null || article.getTitle().isEmpty()) {
                        continue;
                    }
                    if (article.getLink() == null || article.getLink().isEmpty()) {
                        continue;
                    }
                    group.addView(buildChip(article.getTitle(), article.getLink()));
                    hasItems = true;
                }
            }
        }

        section.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        return hasItems;
    }

    private Chip buildChip(String text, String link) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(false);
        chip.setEnsureMinTouchTargetSize(false);
        chip.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), link, text));
        return chip;
    }
}
