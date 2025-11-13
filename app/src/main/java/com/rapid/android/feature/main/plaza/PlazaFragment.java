package com.rapid.android.feature.main.plaza;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.ui.presentation.BaseFragment;
import com.rapid.android.core.ui.utils.ToastViewUtils;
import com.rapid.android.databinding.FragmentPlazaBinding;
import com.rapid.android.feature.login.LoginActivity;
import com.rapid.android.feature.main.TabNavigator;
import com.rapid.android.feature.main.discover.share.ShareArticleActivity;
import com.rapid.android.feature.main.home.ArticleAdapter;
import com.rapid.android.feature.search.SearchActivity;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.ContentStateController;
import com.rapid.android.ui.common.RecyclerViewDecorations;

public class PlazaFragment extends BaseFragment<PlazaViewModel, FragmentPlazaBinding> {

    private ArticleAdapter articleAdapter;
    private LinearLayoutManager layoutManager;
    private ContentStateController stateController;
    private BackToTopController backToTopController;
    private TabNavigator tabNavigator;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof TabNavigator) {
            tabNavigator = (TabNavigator) context;
        }
    }

    @Override
    public void onDetach() {
        tabNavigator = null;
        super.onDetach();
    }

    @Override
    protected PlazaViewModel createViewModel() {
        return new ViewModelProvider(this).get(PlazaViewModel.class);
    }

    @Override
    protected FragmentPlazaBinding createViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentPlazaBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initializeViews() {
        layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        articleAdapter = new ArticleAdapter(getDialogController(), new ArticleListBean());
        binding.recyclerView.setAdapter(articleAdapter);
        RecyclerViewDecorations.addSpacing(binding.recyclerView);

        stateController = new ContentStateController(binding.swipeRefresh, binding.progressBar, binding.emptyView);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView, tabNavigator);

        binding.fabShareArticle.setOnClickListener(v -> {
            if (!SessionManager.getInstance().isLoggedIn()) {
                showShortToast(getString(R.string.mine_toast_require_login));
                startActivity(new Intent(requireContext(), LoginActivity.class));
                return;
            }
            ShareArticleActivity.start(requireContext());
        });

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

        binding.toolbar.setNavigationIcon(R.drawable.dehaze_24px);
        binding.toolbar.setNavigationOnClickListener(v -> tabNavigator.onHomeNavigationClick());
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                startActivity(new Intent(requireContext(), SearchActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void loadData() {
        viewModel.refresh();
    }

    @Override
    protected void setupObservers() {

        viewModel.getPlazaItems().observe(this, items -> {
            articleAdapter.submitList(items);
            stateController.setEmpty(items == null || items.isEmpty());
        });

        viewModel.getLoading().observe(this, loading ->
                stateController.setLoading(Boolean.TRUE.equals(loading)));

        viewModel.getLoadingMore().observe(this, loading ->
                binding.loadMoreProgress.setVisibility(Boolean.TRUE.equals(loading)
                        ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onDestroyView() {
        if (backToTopController != null) {
            backToTopController.detach();
            backToTopController = null;
        }
        super.onDestroyView();
    }

    private void showShortToast(String message) {
        ToastViewUtils.showShortToast(getDialogController(), message);
    }
}
