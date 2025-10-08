package com.rapid.android.ui.feature.main.discover.project;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.CategoryNodeBean;
import com.rapid.android.core.domain.model.ProjectPageBean;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.databinding.ItemProjectTabPageBinding;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.paging.PagingPayload;
import com.rapid.android.ui.feature.main.discover.project.list.ProjectListAdapter;

import java.util.*;

import io.reactivex.rxjava3.disposables.Disposable;

final class ProjectTabPagerAdapter extends RecyclerView.Adapter<ProjectTabPagerAdapter.ProjectPageViewHolder> {

    private final ProjectViewModel viewModel;
    private final HostCallbacks hostCallbacks;
    private final List<CategoryNodeBean> parents = new ArrayList<>();
    private final Map<Integer, PageState> states = new HashMap<>();
    ProjectTabPagerAdapter(ProjectViewModel viewModel, HostCallbacks hostCallbacks) {
        this.viewModel = viewModel;
        this.hostCallbacks = hostCallbacks;
    }

    void submitCategories(List<CategoryNodeBean> data) {
        parents.clear();
        for (PageState state : states.values()) {
            state.detach();
        }
        states.clear();
        if (data != null) {
            parents.addAll(data);
        }
        notifyDataSetChanged();
    }

    CategoryNodeBean getItem(int position) {
        if (position < 0 || position >= parents.size()) {
            return null;
        }
        return parents.get(position);
    }

    boolean canScrollUp(int position) {
        PageState state = getState(position);
        if (state == null || state.recyclerView == null) {
            return false;
        }
        return state.recyclerView.canScrollVertically(-1);
    }

    void refreshPage(int position) {
        PageState state = getState(position);
        if (state == null) {
            return;
        }
        if (state.selectedChildId <= 0 && !state.children.isEmpty()) {
            selectChild(state, state.children.get(0), true);
            return;
        }
        loadPage(state, state.selectedChildId, true);
    }

    void release() {
        for (PageState state : states.values()) {
            state.detach();
        }
        states.clear();
    }

    @NonNull
    @Override
    public ProjectPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProjectTabPageBinding binding = ItemProjectTabPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProjectPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectPageViewHolder holder, int position) {
        CategoryNodeBean parent = parents.get(position);
        PageState state = getOrCreateState(parent);
        holder.bind(state);
    }

    @Override
    public int getItemCount() {
        return parents.size();
    }

    private PageState getState(int position) {
        CategoryNodeBean parent = getItem(position);
        if (parent == null) {
            return null;
        }
        return states.get(parent.getId());
    }

    private PageState getOrCreateState(CategoryNodeBean parent) {
        PageState state = states.get(parent.getId());
        if (state != null) {
            return state;
        }
        state = new PageState(parent.getId());
        state.children.addAll(resolveChildren(parent));
        states.put(parent.getId(), state);
        return state;
    }

    private List<CategoryNodeBean> resolveChildren(CategoryNodeBean parent) {
        List<CategoryNodeBean> children = parent.getChildren();
        if (children != null && !children.isEmpty()) {
            return children;
        }
        CategoryNodeBean single = new CategoryNodeBean();
        single.setId(parent.getId());
        single.setName(parent.getName());
        single.setDesc(parent.getDesc());
        single.setLink(parent.getLink());
        single.setLisenseLink(parent.getLisenseLink());
        single.setAuthor(parent.getAuthor());
        single.setCover(parent.getCover());
        List<ArticleListBean.Data> articleList = parent.getArticleList();
        if (articleList != null) {
            single.setArticleList(new ArrayList<>(articleList));
        }
        return new ArrayList<>(Collections.singletonList(single));
    }

    private void selectChild(PageState state, CategoryNodeBean child, boolean forceRefresh) {
        if (child == null) {
            return;
        }
        if (!forceRefresh && state.selectedChildId == child.getId()) {
            return;
        }
        state.selectedChildId = child.getId();
        state.selectedChildName = child.getName();
        state.nextPage = 1;
        state.hasMore = true;
        state.items.clear();
        state.initialized = false;
        if (state.adapter != null) {
            state.adapter.submitNewList(new ArrayList<>(state.items));
        }
        if (state.recyclerView != null) {
            state.recyclerView.scrollToPosition(0);
        }
        loadPage(state, child.getId(), true);
    }

    private void loadPage(PageState state, int categoryId, boolean refresh) {
        if (refresh) {
            hostCallbacks.setRefreshing(true);
            state.isRefreshing = true;
            state.isLoadingMore = false;
            updateLoadMoreUi(state);
        } else {
            if (!state.hasMore || state.isLoadingMore) {
                return;
            }
            state.isLoadingMore = true;
            updateLoadMoreUi(state);
        }

        int requestPage = refresh ? 1 : state.nextPage;
        Disposable disposable = viewModel.fetchProjectArticles(requestPage, categoryId)
                .subscribe(result -> handleResult(state, result, refresh),
                        throwable -> handleError(state, throwable, refresh));
        viewModel.trackDisposable(disposable);
    }

    private void handleResult(PageState state, DomainResult<PagingPayload<ProjectPageBean.ProjectItemBean>> result,
                              boolean refresh) {
        if (refresh) {
            hostCallbacks.setRefreshing(false);
            state.isRefreshing = false;
        } else {
            state.isLoadingMore = false;
            updateLoadMoreUi(state);
        }

        if (result.isSuccess() && result.getData() != null) {
            PagingPayload<ProjectPageBean.ProjectItemBean> payload = result.getData();
            state.nextPage = payload.getNextPage();
            state.hasMore = payload.hasMore();
            state.initialized = true;
            List<ProjectPageBean.ProjectItemBean> newItems = payload.getItems();
            if (refresh) {
                state.items.clear();
                if (newItems != null) {
                    state.items.addAll(newItems);
                }
                if (state.adapter != null) {
                    state.adapter.submitNewList(new ArrayList<>(state.items));
                }
            } else if (newItems != null && !newItems.isEmpty()) {
                state.items.addAll(newItems);
                if (state.adapter != null) {
                    state.adapter.appendList(newItems);
                }
            }
        }

        if (refresh) {
            updateLoadMoreUi(state);
        }
    }

    private void handleError(PageState state, Throwable throwable, boolean refresh) {
        if (refresh) {
            hostCallbacks.setRefreshing(false);
            state.isRefreshing = false;
        } else {
            state.isLoadingMore = false;
            updateLoadMoreUi(state);
        }
        if (throwable != null && !TextUtils.isEmpty(throwable.getMessage())) {
            viewModel.emitArticleError(throwable.getMessage());
        }
    }

    private void updateLoadMoreUi(PageState state) {
        if (state.binding == null) {
            return;
        }
        state.binding.loadMoreProgress.setVisibility(state.isLoadingMore ? View.VISIBLE : View.GONE);
    }

    interface HostCallbacks {
        void setRefreshing(boolean refreshing);
    }

    private static class PageState {
        final int parentId;
        final List<CategoryNodeBean> children = new ArrayList<>();
        final List<ProjectPageBean.ProjectItemBean> items = new ArrayList<>();
        ProjectListAdapter adapter;
        ItemProjectTabPageBinding binding;
        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;
        RecyclerView.OnScrollListener scrollListener;
        BackToTopController backToTopController;
        int selectedChildId;
        String selectedChildName;
        int chipCheckedId;
        int nextPage = 1;
        boolean hasMore = true;
        boolean initialized = false;
        boolean isRefreshing = false;
        boolean isLoadingMore = false;

        PageState(int parentId) {
            this.parentId = parentId;
        }

        void detach() {
            if (recyclerView != null && scrollListener != null) {
                recyclerView.removeOnScrollListener(scrollListener);
            }
            if (backToTopController != null) {
                backToTopController.detach();
                backToTopController = null;
            }
            binding = null;
            recyclerView = null;
        }
    }

    class ProjectPageViewHolder extends RecyclerView.ViewHolder {

        private final ItemProjectTabPageBinding binding;

        ProjectPageViewHolder(@NonNull ItemProjectTabPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PageState state) {
            if (state == null) {
                return;
            }

            state.detach();

            if (state.layoutManager == null) {
                state.layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
            }
            state.binding = binding;
            binding.recyclerView.setLayoutManager(state.layoutManager);
            if (state.adapter == null) {
                state.adapter = new ProjectListAdapter();
            }
            binding.recyclerView.setAdapter(state.adapter);

            if (state.scrollListener == null) {
                state.scrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy <= 0 || state.isRefreshing || state.isLoadingMore || !state.hasMore) {
                            return;
                        }
                        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                        if (!(layoutManager instanceof LinearLayoutManager)) {
                            return;
                        }
                        LinearLayoutManager lm = (LinearLayoutManager) layoutManager;
                        int totalCount = lm.getItemCount();
                        if (totalCount == 0) {
                            return;
                        }
                        int lastVisible = lm.findLastVisibleItemPosition();
                        if (lastVisible >= totalCount - 3 && state.selectedChildId > 0) {
                            loadPage(state, state.selectedChildId, false);
                        }
                    }
                };
            }

            binding.recyclerView.addOnScrollListener(state.scrollListener);
            state.recyclerView = binding.recyclerView;

            if (!state.items.isEmpty()) {
                state.adapter.submitNewList(new ArrayList<>(state.items));
            }

            if (state.backToTopController != null) {
                state.backToTopController.detach();
            }
            state.backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView);

            selectChild(state, state.children.get(0), true);

            if (!state.initialized && state.selectedChildId > 0) {
                loadPage(state, state.selectedChildId, true);
            } else {
                updateLoadMoreUi(state);
            }
        }
    }
}
