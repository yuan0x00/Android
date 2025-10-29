package com.rapid.android.feature.search;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;
import com.rapid.android.R;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.HotKeyBean;
import com.rapid.android.core.ui.presentation.BaseActivity;
import com.rapid.android.databinding.ActivitySearchBinding;
import com.rapid.android.feature.main.home.ArticleAdapter;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;
import com.rapid.android.ui.common.UiFeedback;

import java.util.List;

public class SearchActivity extends BaseActivity<SearchViewModel, ActivitySearchBinding> {

    private ContentStateController stateController;
    private ArticleAdapter resultAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected SearchViewModel createViewModel() {
        return new ViewModelProvider(this).get(SearchViewModel.class);
    }

    @Override
    protected ActivitySearchBinding createViewBinding(View rootView) {
        return ActivitySearchBinding.bind(rootView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search;
    }

    @Override
    protected void initializeViews() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.searchInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    viewModel.submitSearch("");
                }
            }
        });

        layoutManager = new LinearLayoutManager(this);
        binding.resultRecyclerView.setLayoutManager(layoutManager);

        ArticleListBean listBean = new ArticleListBean();
        resultAdapter = new ArticleAdapter(getDialogController(), listBean);
        binding.resultRecyclerView.setAdapter(resultAdapter);
        RecyclerViewDecorations.addSpacing(binding.resultRecyclerView);

        binding.resultRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        binding.resultRefreshLayout.setOnRefreshListener(viewModel::retry);
        stateController = new ContentStateController(binding.resultRefreshLayout, binding.progressBar, binding.emptyView);

        binding.clearHistoryButton.setOnClickListener(v -> viewModel.clearHistory());
    }

    @Override
    protected void setupObservers() {
        viewModel.getHotKeys().observe(this, this::renderHotKeys);
        viewModel.getHistories().observe(this, this::renderHistories);
        viewModel.getShowSuggestions().observe(this, this::toggleSuggestions);
        viewModel.getSearchResults().observe(this, results -> {
            resultAdapter.submitList(results);
            updateEmptyState();
        });
        viewModel.getLoading().observe(this, loading -> stateController.setLoading(Boolean.TRUE.equals(loading)));
        viewModel.getLoadingMore().observe(this, loadingMore -> {
            if (!Boolean.TRUE.equals(loadingMore)) {
                binding.resultRefreshLayout.setRefreshing(false);
            }
        });
        viewModel.getErrorMessage().observe(this, msg -> stateController.stopRefreshing());
        viewModel.getPagingError().observe(this, msg -> stateController.stopRefreshing());
        viewModel.getEmptyState().observe(this, empty -> updateEmptyState());

        UiFeedback.observeError(this, provideDialogController(), viewModel.getErrorMessage());
        UiFeedback.observeError(this, provideDialogController(), viewModel.getPagingError());
    }

    @Override
    protected void loadData() {
        viewModel.initialize();
    }

    private void performSearch() {
        String keyword = binding.searchInput.getText() != null ? binding.searchInput.getText().toString() : "";
        viewModel.submitSearch(keyword);
        hideKeyboard();
    }

    private void renderHistories(List<String> histories) {
        binding.historyGroup.removeAllViews();
        if (histories == null || histories.isEmpty()) {
            binding.historyGroup.setVisibility(View.GONE);
            binding.clearHistoryButton.setVisibility(View.GONE);
            return;
        }
        binding.historyGroup.setVisibility(View.VISIBLE);
        binding.clearHistoryButton.setVisibility(View.VISIBLE);
        for (String item : histories) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Chip chip = createChip(item);
            chip.setOnClickListener(v -> {
                binding.searchInput.setText(item);
                binding.searchInput.setSelection(binding.searchInput.length());
                viewModel.submitSearch(item);
            });
            binding.historyGroup.addView(chip);
        }
    }

    private void renderHotKeys(List<HotKeyBean> hotKeys) {
        binding.hotGroup.removeAllViews();
        if (hotKeys == null || hotKeys.isEmpty()) {
            binding.hotGroup.setVisibility(View.GONE);
            return;
        }
        binding.hotGroup.setVisibility(View.VISIBLE);
        for (HotKeyBean bean : hotKeys) {
            if (bean == null || TextUtils.isEmpty(bean.getName())) {
                continue;
            }
            String keyword = bean.getName();
            Chip chip = createChip(keyword);
            chip.setOnClickListener(v -> {
                binding.searchInput.setText(keyword);
                binding.searchInput.setSelection(binding.searchInput.length());
                viewModel.submitSearch(keyword);
            });
            binding.hotGroup.addView(chip);
        }
    }

    private Chip createChip(String text) {
        Chip chip = new Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Assist);
        chip.setText(text);
        chip.setCloseIconVisible(false);
        return chip;
    }

    private void toggleSuggestions(Boolean show) {
        boolean visible = Boolean.TRUE.equals(show);
        binding.suggestionsContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.resultRefreshLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
        if (visible) {
            stateController.setEmpty(false);
            stateController.stopRefreshing();
        } else {
            updateEmptyState();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.searchInput.getWindowToken(), 0);
        }
    }

    private void updateEmptyState() {
        boolean showSuggestions = Boolean.TRUE.equals(viewModel.getShowSuggestions().getValue());
        if (showSuggestions) {
            stateController.setEmpty(false);
            return;
        }
        Boolean empty = viewModel.getEmptyState().getValue();
        stateController.setEmpty(Boolean.TRUE.equals(empty));
    }
}
