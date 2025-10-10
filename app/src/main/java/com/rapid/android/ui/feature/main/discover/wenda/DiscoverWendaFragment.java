package com.rapid.android.ui.feature.main.discover.wenda;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentDiscoverWendaBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class DiscoverWendaFragment extends BaseFragment<DiscoverWendaViewModel, FragmentDiscoverWendaBinding> {

    private ContentStateController stateController;
    private WendaAdapter adapter;

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
        adapter = new WendaAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView, com.rapid.android.R.dimen.app_spacing_md);

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
        adapter.submitList(list);
        boolean empty = list == null || list.isEmpty();
        stateController.setEmpty(empty);
        stateController.stopRefreshing();
    }
}
