package com.rapid.android.ui.feature.main.wechat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.WxChapterBean;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.databinding.ItemWechatTabPageBinding;
import com.rapid.android.ui.common.BackToTopController;
import com.rapid.android.ui.common.paging.PagingPayload;
import com.rapid.android.ui.feature.main.home.FeedAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.Disposable;

final class WechatTabPagerAdapter extends RecyclerView.Adapter<WechatTabPagerAdapter.WechatPageViewHolder> {

    private final WechatViewModel viewModel;
    private final HostCallbacks hostCallbacks;
    private final List<WxChapterBean> chapters = new ArrayList<>();
    private final Map<Integer, PageState> states = new HashMap<>();
    private final DialogController dialogController;

    WechatTabPagerAdapter(WechatViewModel viewModel, HostCallbacks hostCallbacks, DialogController dialogController) {
        this.viewModel = viewModel;
        this.hostCallbacks = hostCallbacks;
        this.dialogController = dialogController;
    }

    void submitChapters(List<WxChapterBean> data) {
        chapters.clear();
        for (PageState state : states.values()) {
            state.detach();
        }
        states.clear();
        if (data != null) {
            chapters.addAll(data);
        }
        notifyDataSetChanged();
    }

    WxChapterBean getItem(int position) {
        if (position < 0 || position >= chapters.size()) {
            return null;
        }
        return chapters.get(position);
    }

    boolean canScrollUp(int position) {
        WxChapterBean chapter = getItem(position);
        if (chapter == null) {
            return false;
        }
        PageState state = states.get(chapter.getId());
        if (state == null || state.recyclerView == null) {
            return false;
        }
        return state.recyclerView.canScrollVertically(-1);
    }

    void refreshPage(int position) {
        WxChapterBean chapter = getItem(position);
        if (chapter == null) {
            return;
        }
        PageState state = obtainState(chapter);
        loadPage(state, chapter.getId(), true);
    }

    void release() {
        for (PageState state : states.values()) {
            state.detach();
        }
        states.clear();
    }

    @NonNull
    @Override
    public WechatPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWechatTabPageBinding binding = ItemWechatTabPageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WechatPageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WechatPageViewHolder holder, int position) {
        WxChapterBean chapter = chapters.get(position);
        PageState state = obtainState(chapter);
        holder.bind(state, chapter.getId());
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    private PageState obtainState(WxChapterBean chapter) {
        PageState state = states.get(chapter.getId());
        if (state != null) {
            return state;
        }
        state = new PageState(chapter.getId());
        states.put(chapter.getId(), state);
        return state;
    }

    private void loadPage(PageState state, int chapterId, boolean refresh) {
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

        int targetPage = refresh ? 1 : state.nextPage;
        Disposable disposable = viewModel.fetchWechatArticles(chapterId, targetPage)
                .subscribe(result -> handleResult(state, result, refresh), throwable -> handleError(state, throwable, refresh));
        viewModel.trackDisposable(disposable);
    }

    private void handleResult(PageState state, DomainResult<PagingPayload<ArticleListBean.Data>> result, boolean refresh) {
        if (refresh) {
            hostCallbacks.setRefreshing(false);
            state.isRefreshing = false;
        } else {
            state.isLoadingMore = false;
            updateLoadMoreUi(state);
        }

        if (result.isSuccess() && result.getData() != null) {
            PagingPayload<ArticleListBean.Data> payload = result.getData();
            state.nextPage = payload.getNextPage();
            state.hasMore = payload.hasMore();
            state.initialized = true;

            List<ArticleListBean.Data> newItems = payload.getItems();
            if (refresh) {
                state.items.clear();
                if (newItems != null) {
                    state.items.addAll(newItems);
                }
                if (state.adapter != null) {
                    state.adapter.submitList(new ArrayList<>(state.items));
                }
                if (state.recyclerView != null) {
                    state.recyclerView.scrollToPosition(0);
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
            viewModel.getArticleErrorMessage().setValue(throwable.getMessage());
        }
    }

    private void updateLoadMoreUi(PageState state) {
        ItemWechatTabPageBinding binding = state.binding;
        if (binding == null) {
            return;
        }
        CircularProgressIndicator indicator = binding.loadMoreProgress;
        if (indicator != null) {
            indicator.setVisibility(state.isLoadingMore ? View.VISIBLE : View.GONE);
        }
    }

    interface HostCallbacks {
        void setRefreshing(boolean refreshing);
    }

    private static class PageState {
        final int chapterId;
        final List<ArticleListBean.Data> items = new ArrayList<>();
        FeedAdapter adapter;
        ItemWechatTabPageBinding binding;
        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;
        RecyclerView.OnScrollListener scrollListener;
        BackToTopController backToTopController;
        int nextPage = 2;
        boolean hasMore = true;
        boolean initialized = false;
        boolean isRefreshing = false;
        boolean isLoadingMore = false;

        PageState(int chapterId) {
            this.chapterId = chapterId;
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

    class WechatPageViewHolder extends RecyclerView.ViewHolder {

        private final ItemWechatTabPageBinding binding;

        WechatPageViewHolder(@NonNull ItemWechatTabPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PageState state, int chapterId) {
            state.detach();
            state.binding = binding;
            if (state.layoutManager == null) {
                state.layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
            }
            binding.recyclerView.setLayoutManager(state.layoutManager);

            if (state.adapter == null) {
                state.adapter = new FeedAdapter(dialogController, new ArticleListBean());
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
                        int total = lm.getItemCount();
                        if (total == 0) {
                            return;
                        }
                        int lastVisible = lm.findLastVisibleItemPosition();
                        if (lastVisible >= total - 3) {
                            loadPage(state, chapterId, false);
                        }
                    }
                };
            }

            binding.recyclerView.addOnScrollListener(state.scrollListener);
            state.recyclerView = binding.recyclerView;

            if (!state.items.isEmpty()) {
                state.adapter.submitList(new ArrayList<>(state.items));
            }

            if (state.backToTopController != null) {
                state.backToTopController.detach();
            }
            state.backToTopController = BackToTopController.attach(binding.fabBackToTop, binding.recyclerView);

            if (!state.initialized) {
                loadPage(state, chapterId, true);
            } else {
                updateLoadMoreUi(state);
            }
        }
    }
}
