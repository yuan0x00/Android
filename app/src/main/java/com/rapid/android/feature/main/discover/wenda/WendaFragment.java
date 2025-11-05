package com.rapid.android.feature.main.discover.wenda;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentWendaBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;

import java.util.List;

public class WendaFragment extends BaseFragment<WendaViewModel, FragmentWendaBinding> {

    private ContentStateController stateController;
    private WendaAdapter adapter;

    @Override
    protected WendaViewModel createViewModel() {
        return new ViewModelProvider(this).get(WendaViewModel.class);
    }

    @Override
    protected FragmentWendaBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentWendaBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        adapter = new WendaAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);

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
