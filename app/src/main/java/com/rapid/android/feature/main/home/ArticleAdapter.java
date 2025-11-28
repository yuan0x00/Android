package com.rapid.android.feature.main.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.rapid.android.core.ui.utils.ToastViewUtils;
import com.rapid.android.databinding.ItemArticleBinding;
import com.rapid.android.feature.login.LoginActivity;
import com.rapid.android.feature.web.ArticleWebViewUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    private final List<ArticleListBean.Data> items = new ArrayList<>();
    private final UserRepository userRepository = RepositoryProvider.getUserRepository();
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    private final DialogController dialogController;
    private final boolean forceTopFlag;


    public ArticleAdapter(@Nullable DialogController dialogController, ArticleListBean bean) {
        this(dialogController, bean, false);
    }

    public ArticleAdapter(@Nullable DialogController dialogController, ArticleListBean bean, boolean forceTopFlag) {
        this.dialogController = dialogController;
        this.forceTopFlag = forceTopFlag;
        setData(bean);
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
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArticleBinding binding = ItemArticleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ArticleViewHolder(binding, userRepository, disposables, dialogController);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
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

    private boolean isTopArticle(@NonNull ArticleListBean.Data item) {
        return forceTopFlag || item.getType() == 1;
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final ItemArticleBinding binding;
        private final UserRepository userRepository;
        private final CompositeDisposable disposables;
        @Nullable
        private final DialogController dialogController;
        private boolean collectRequestRunning = false;

        public ArticleViewHolder(@NonNull ItemArticleBinding binding,
                                 @NonNull UserRepository userRepository,
                                 @NonNull CompositeDisposable disposables,
                                 @Nullable DialogController dialogController) {
            super(binding.getRoot());
            this.binding = binding;
            this.userRepository = userRepository;
            this.disposables = disposables;
            this.dialogController = dialogController;
        }

        public void bind(ArticleListBean.Data data, boolean isTopArticle) {
            String author;
            if (StringUtils.isEmpty(data.getAuthor())) {
                author = data.getShareUser();
            } else {
                author = data.getAuthor();
            }
            binding.tvAuthor.setText(author);
            binding.tvTitle.setText(data.getTitle());
            binding.tvTime.setText(" · " + data.getNiceShareDate());
            binding.tvClass.setText(data.getSuperChapterName());
            renderTopTag(isTopArticle);
            binding.ivFavorite.setEnabled(true);
            collectRequestRunning = false;
            renderFavorite(data.isCollect());

            binding.getRoot().setOnClickListener(v -> ArticleWebViewUtil.start(binding.getRoot().getContext(), data));

            binding.ivFavorite.setOnClickListener(v -> {
                handleFavoriteToggle(data);
            });
        }

        private void handleFavoriteToggle(@NonNull ArticleListBean.Data data) {
            if (collectRequestRunning) {
                return;
            }
            int articleId = data.getId();
            if (articleId <= 0) {
                showShortToast(binding.getRoot().getContext().getString(R.string.article_collect_failed));
                return;
            }
            if (!SessionManager.getInstance().isLoggedIn()) {
                showShortToast(binding.getRoot().getContext().getString(R.string.article_collect_need_login));
                binding.getRoot().getContext().startActivity(new Intent(binding.getRoot().getContext(), LoginActivity.class));
                return;
            }
            boolean targetCollect = !data.isCollect();
            binding.ivFavorite.setEnabled(false);
            collectRequestRunning = true;
            Disposable disposable;
            if (targetCollect) {
                disposable = userRepository.collectArticle(articleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> handleCollectResult(data, true, result), this::handleCollectError);
            } else {
                disposable = userRepository.unCollectArticle(articleId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> handleCollectResult(data, false, result), this::handleCollectError);
            }
            disposables.add(disposable);
        }

        private void handleCollectResult(@NonNull ArticleListBean.Data data,
                                         boolean targetCollect,
                                         DomainResult<String> result) {
            collectRequestRunning = false;
            binding.ivFavorite.setEnabled(true);
            if (result != null && result.isSuccess()) {
                data.setCollect(targetCollect);
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
            ToastViewUtils.showShortToast(dialogController, message);
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
