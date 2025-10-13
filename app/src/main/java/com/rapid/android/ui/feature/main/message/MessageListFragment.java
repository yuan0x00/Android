package com.rapid.android.ui.feature.main.message;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.MessageBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.databinding.FragmentMessageListBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class MessageListFragment extends BaseFragment<MessageListViewModel, FragmentMessageListBinding> {

    private static final String ARG_CATEGORY = "arg_category";
    private MessageCategory category = MessageCategory.UNREAD;
    private MessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;
    private Host host;

    public static MessageListFragment newInstance(MessageCategory category) {
        MessageListFragment fragment = new MessageListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY, category.getPosition());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        if (args != null) {
            category = MessageCategory.fromPosition(args.getInt(ARG_CATEGORY, MessageCategory.UNREAD.getPosition()));
        }
        if (getParentFragment() instanceof Host) {
            host = (Host) getParentFragment();
        }
        if (host != null) {
            host.onListFragmentAttached(category, this);
        }
    }

    @Override
    public void onDetach() {
        if (host != null) {
            host.onListFragmentDetached(category);
            host = null;
        }
        super.onDetach();
    }

    @Override
    protected MessageListViewModel createViewModel() {
        return new ViewModelProvider(this, new MessageListViewModel.Factory(category))
                .get(MessageListViewModel.class);
    }

    @Override
    protected FragmentMessageListBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentMessageListBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        adapter = new MessageAdapter();
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
        RecyclerViewDecorations.addTopSpacing(binding.recyclerView);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);

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
    protected void setupObservers() {
        viewModel.getMessages().observe(this, this::renderMessages);
        viewModel.getLoading().observe(this, loading -> stateController.setLoading(Boolean.TRUE.equals(loading)));
        viewModel.getErrorMessage().observe(this, msg -> stateController.stopRefreshing());
        viewModel.getEmptyState().observe(this, empty -> stateController.setEmpty(Boolean.TRUE.equals(empty)));
        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());

        if (category == MessageCategory.UNREAD) {
            viewModel.getUnreadSyncSignal().observe(this, signal -> {
                if (signal != null && host != null) {
                    host.onUnreadMessagesConsumed();
                }
            });
        }
    }

    @Override
    protected void loadData() {
        viewModel.initialize();
    }

    private void renderMessages(List<MessageBean> messages) {
        adapter.submitList(messages);
    }

    void refreshList() {
        if (binding == null) {
            viewModel.refresh();
            return;
        }
        binding.swipeRefresh.post(() -> {
            if (binding == null) {
                viewModel.refresh();
                return;
            }
            binding.swipeRefresh.setRefreshing(true);
            viewModel.refresh();
        });
    }

    interface Host {
        void onListFragmentAttached(MessageCategory category, MessageListFragment fragment);

        void onListFragmentDetached(MessageCategory category);

        void onUnreadMessagesConsumed();
    }
}
