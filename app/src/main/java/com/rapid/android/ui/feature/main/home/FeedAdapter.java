package com.rapid.android.ui.feature.main.home;

import android.content.Intent;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.common.text.StringUtils;
import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.components.popup.BasePopupWindow;
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ItemFeedBinding;
import com.rapid.android.ui.feature.login.LoginActivity;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private final List<ArticleListBean.Data> items = new ArrayList<>();
    private final UserRepository userRepository = RepositoryProvider.getUserRepository();
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    private final DialogController dialogController;
    private final Set<Integer> topArticleIds = new HashSet<>();


    public FeedAdapter(@Nullable DialogController dialogController, ArticleListBean feeds) {
        this.dialogController = dialogController;
        setData(feeds);
    }

    public void setData(ArticleListBean newData) {
        submitList(newData != null ? newData.getDatas() : null);
    }

    public void submitList(List<ArticleListBean.Data> data) {
        List<ArticleListBean.Data> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FeedDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    public void appendList(List<ArticleListBean.Data> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = ItemFeedBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FeedViewHolder(binding, userRepository, disposables, dialogController);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        ArticleListBean.Data item = items.get(position);
        holder.bind(item, isTopArticle(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        disposables.clear();
    }

    public void setTopArticleIds(Set<Integer> ids) {
        topArticleIds.clear();
        if (ids != null) {
            topArticleIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    private boolean isTopArticle(@NonNull ArticleListBean.Data item) {
        return topArticleIds.contains(item.getId()) || item.getType() == 1;
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedBinding binding;
        private final UserRepository userRepository;
        private final CompositeDisposable disposables;
        @Nullable
        private final DialogController dialogController;
        private boolean collectRequestRunning = false;

        public FeedViewHolder(@NonNull ItemFeedBinding binding,
                              @NonNull UserRepository userRepository,
                              @NonNull CompositeDisposable disposables,
                              @Nullable DialogController dialogController) {
            super(binding.getRoot());
            this.binding = binding;
            this.userRepository = userRepository;
            this.disposables = disposables;
            this.dialogController = dialogController;
        }

        public void bind(ArticleListBean.Data feedItem, boolean isTopArticle) {
            String author;
            if (StringUtils.isEmpty(feedItem.getAuthor())) {
                author = feedItem.getShareUser();
            } else {
                author = feedItem.getAuthor();
            }
            binding.tvAuthor.setText(author);
            binding.tvTitle.setText(feedItem.getTitle());
            binding.tvTime.setText(" · " + feedItem.getNiceShareDate());
            binding.tvClass.setText(feedItem.getSuperChapterName());
            renderTopTag(isTopArticle);
            binding.ivFavorite.setEnabled(true);
            collectRequestRunning = false;
            renderFavorite(feedItem.isCollect());

            GestureDetector gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public void onLongPress(MotionEvent e) {
                    // Get touch coordinates from MotionEvent
                    float touchX = e.getRawX();
                    float touchY = e.getRawY();

                    // Create and configure BasePopupWindow
                    BasePopupWindow popup = new BasePopupWindow.Builder(itemView.getContext())
                            .setFocusable(true)
                            .build();

                    // Show popup at touch position
                    popup.showAtTouchPosition(binding.getRoot(), touchX, touchY);
                }
            });

            // Set OnTouchListener to pass events to GestureDetector
            binding.getRoot().setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                return false;
            });

            binding.getRoot().setOnClickListener(v -> ArticleWebViewActivity.start(binding.getRoot().getContext(), feedItem));

            binding.ivFavorite.setOnClickListener(v -> {
                handleFavoriteToggle(feedItem);
            });
        }

        private void handleFavoriteToggle(@NonNull ArticleListBean.Data feedItem) {
            if (collectRequestRunning) {
                return;
            }
            int articleId = feedItem.getId();
            if (articleId <= 0) {
                showShortToast(binding.getRoot().getContext().getString(R.string.article_collect_failed));
                return;
            }
            if (!SessionManager.getInstance().isLoggedIn()) {
                showShortToast(binding.getRoot().getContext().getString(R.string.article_collect_need_login));
                binding.getRoot().getContext().startActivity(new Intent(binding.getRoot().getContext(), LoginActivity.class));
                return;
            }
            boolean targetCollect = !feedItem.isCollect();
            binding.ivFavorite.setEnabled(false);
            collectRequestRunning = true;
            Disposable disposable;
            if (targetCollect) {
                disposable = userRepository.collectArticle(articleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> handleCollectResult(feedItem, true, result), this::handleCollectError);
            } else {
                disposable = userRepository.unCollectArticle(articleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> handleCollectResult(feedItem, false, result), this::handleCollectError);
            }
            disposables.add(disposable);
        }

        private void handleCollectResult(@NonNull ArticleListBean.Data feedItem,
                                         boolean targetCollect,
                                         DomainResult<String> result) {
            collectRequestRunning = false;
            binding.ivFavorite.setEnabled(true);
            if (result != null && result.isSuccess()) {
                feedItem.setCollect(targetCollect);
                renderFavorite(targetCollect);
                showShortToast(binding.getRoot().getContext().getString(
                        targetCollect ? R.string.article_collect_success : R.string.article_uncollect_success
                ));
            } else {
                showShortToast(binding.getRoot().getContext().getString(R.string.article_collect_failed));
            }
        }

        private void handleCollectError(@NonNull Throwable throwable) {
            collectRequestRunning = false;
            binding.ivFavorite.setEnabled(true);
            showShortToast(binding.getRoot().getContext().getString(R.string.article_collect_failed));
        }

        private void renderFavorite(boolean collected) {
            binding.ivFavorite.setImageResource(collected ? R.drawable.bookmark_fill_24px : R.drawable.bookmark_24px);
            binding.ivFavorite.setContentDescription(binding.getRoot().getContext().getString(
                    collected ? R.string.article_uncollect : R.string.article_collect
            ));
        }

        private void renderTopTag(boolean isTopArticle) {
            binding.tvTopTag.setVisibility(isTopArticle ? View.VISIBLE : View.GONE);
        }

        private void showShortToast(String message) {
            if (dialogController == null || message == null || message.isEmpty()) {
                return;
            }
            ToastUtils.showShortToast(dialogController, message);
        }
    }

    private static class FeedDiffCallback extends DiffUtil.Callback {
        private final List<ArticleListBean.Data> oldList;
        private final List<ArticleListBean.Data> newList;

        FeedDiffCallback(List<ArticleListBean.Data> oldList, List<ArticleListBean.Data> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ArticleListBean.Data oldItem = oldList.get(oldItemPosition);
            ArticleListBean.Data newItem = newList.get(newItemPosition);
            return oldItem.isCollect() == newItem.isCollect()
                    && StringUtils.equals(oldItem.getTitle(), newItem.getTitle())
                    && StringUtils.equals(oldItem.getAuthor(), newItem.getAuthor())
                    && StringUtils.equals(oldItem.getShareUser(), newItem.getShareUser())
                    && StringUtils.equals(oldItem.getNiceShareDate(), newItem.getNiceShareDate())
                    && StringUtils.equals(oldItem.getNiceDate(), newItem.getNiceDate())
                    && StringUtils.equals(oldItem.getSuperChapterName(), newItem.getSuperChapterName());
        }
    }
}
