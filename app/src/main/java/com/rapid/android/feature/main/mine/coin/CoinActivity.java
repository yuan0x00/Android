package com.rapid.android.feature.main.mine.coin;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.domain.model.CoinRankBean;
import com.rapid.android.core.domain.model.CoinRecordBean;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivityCoinBinding;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class CoinActivity extends BaseActivity<CoinViewModel, ActivityCoinBinding> {

    private static final int TAB_RECORDS = 0;
    private static final int TAB_RANK = 1;

    private int currentTab = TAB_RECORDS;

    private CoinRecordAdapter recordAdapter;
    private CoinRankAdapter rankAdapter;
    private ContentStateController stateController;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, CoinActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected CoinViewModel createViewModel() {
        return new ViewModelProvider(this).get(CoinViewModel.class);
    }

    @Override
    protected ActivityCoinBinding createViewBinding(View rootView) {
        return ActivityCoinBinding.bind(rootView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_coin;
    }

    @Override
    protected void initializeViews() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.coin_title);
        }

        recordAdapter = new CoinRecordAdapter();
        rankAdapter = new CoinRankAdapter();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(recordAdapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);
        binding.recyclerView.setClipToPadding(false);
        binding.recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        binding.swipeRefresh.setOnRefreshListener(this::handleRefresh);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.coin_tab_records));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.coin_tab_rank));
        binding.emptyView.setText(R.string.coin_empty_records);

        binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (tab.getPosition() == currentTab) {
                    handleRefresh();
                }
            }
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) {
                    return;
                }
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (totalItemCount == 0) {
                    return;
                }
                if (lastVisible >= totalItemCount - 3) {
                    if (currentTab == TAB_RECORDS) {
                        viewModel.loadMoreRecords();
                    } else {
                        viewModel.loadMoreRank();
                    }
                }
            }
        });
    }

    @Override
    protected void setupObservers() {
        viewModel.getHeaderState().observe(this, state -> {
            if (state == null) {
                return;
            }
            binding.tvCoinBalance.setText(state.getCoinDisplay());
            binding.tvLevel.setText(state.getLevelDisplay());
            binding.tvRank.setText(state.getRankDisplay());
        });

        SessionManager.getInstance().state.observe(this, viewModel::applySessionState);

        viewModel.getRecordItems().observe(this, items -> {
            recordAdapter.submitNewList(items);
            if (currentTab == TAB_RECORDS) {
                stateController.setEmpty(isNullOrEmpty(items));
            }
        });

        viewModel.getRecordLoading().observe(this, loading -> {
            if (currentTab == TAB_RECORDS) {
                stateController.setLoading(Boolean.TRUE.equals(loading));
            }
        });

        viewModel.getRecordLoadingMore().observe(this, loadingMore -> {
            if (currentTab == TAB_RECORDS) {
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loadingMore) ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getRankItems().observe(this, items -> {
            rankAdapter.submitNewList(items);
            if (currentTab == TAB_RANK) {
                stateController.setEmpty(isNullOrEmpty(items));
            }
        });

        viewModel.getRankLoading().observe(this, loading -> {
            if (currentTab == TAB_RANK) {
                stateController.setLoading(Boolean.TRUE.equals(loading));
            }
        });

        viewModel.getRankLoadingMore().observe(this, loadingMore -> {
            if (currentTab == TAB_RANK) {
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loadingMore) ? View.VISIBLE : View.GONE);
            }
        });

        UiFeedback.observeError(this, provideDialogController(), viewModel.getRecordError());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getRecordPagingError());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getRankError());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getRankPagingError());
    }

    @Override
    protected void loadData() {
        viewModel.applySessionState(SessionManager.getInstance().getCurrentState());
        viewModel.ensureRecords();
    }

    private void switchTab(int index) {
        if (index == currentTab) {
            return;
        }
        currentTab = index;
        if (currentTab == TAB_RECORDS) {
            binding.recyclerView.setAdapter(recordAdapter);
            binding.emptyView.setText(R.string.coin_empty_records);
            viewModel.ensureRecords();
        } else {
            binding.recyclerView.setAdapter(rankAdapter);
            binding.emptyView.setText(R.string.coin_empty_rank);
            viewModel.ensureRank();
        }
        updateStateForCurrentTab();
    }

    private void handleRefresh() {
        if (currentTab == TAB_RECORDS) {
            viewModel.refreshRecords();
        } else {
            viewModel.refreshRank();
        }
    }

    private void updateStateForCurrentTab() {
        if (currentTab == TAB_RECORDS) {
            boolean loading = Boolean.TRUE.equals(viewModel.getRecordLoading().getValue());
            stateController.setLoading(loading);
            if (!loading) {
                List<CoinRecordBean> items = viewModel.getRecordItems().getValue();
                stateController.setEmpty(isNullOrEmpty(items));
            }
            binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(viewModel.getRecordLoadingMore().getValue()) ? View.VISIBLE : View.GONE);
        } else {
            boolean loading = Boolean.TRUE.equals(viewModel.getRankLoading().getValue());
            stateController.setLoading(loading);
            if (!loading) {
                List<CoinRankBean> items = viewModel.getRankItems().getValue();
                stateController.setEmpty(isNullOrEmpty(items));
            }
            binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(viewModel.getRankLoadingMore().getValue()) ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}
