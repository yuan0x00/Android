package com.rapid.android.feature.main.mine.favorite;

import android.content.Intent;
import android.text.TextUtils;
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
import com.rapid.android.core.ui.utils.ToastUtils;
import com.rapid.android.databinding.ItemArticleBinding;
import com.rapid.android.feature.login.LoginActivity;
import com.rapid.android.feature.web.ArticleWebViewUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

final class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private final List<ArticleListBean.Data> items = new ArrayList<>();
    private final UserRepository userRepository = RepositoryProvider.getUserRepository();
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    private final DialogController dialogController;
    @Nullable
    private final FavoriteActionListener actionListener;

    FavoriteAdapter(@Nullable DialogController dialogController,
                    @Nullable FavoriteActionListener actionListener) {
        this.dialogController = dialogController;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArticleBinding binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    void submitNewList(List<ArticleListBean.Data> data) {
        List<ArticleListBean.Data> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        markItemsAsCollected(newItems);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    void appendList(List<ArticleListBean.Data> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        markItemsAsCollected(more);
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(items.get(position));
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

    private void markItemsAsCollected(@NonNull List<ArticleListBean.Data> list) {
        for (ArticleListBean.Data item : list) {
            if (item != null && !item.isCollect()) {
                item.setCollect(true);
            }
        }
    }

    interface FavoriteActionListener {
        void onFavoriteRemoved(@NonNull ArticleListBean.Data data);
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ItemArticleBinding binding;
        private boolean collectRequestRunning = false;

        FavoriteViewHolder(@NonNull ItemArticleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ArticleListBean.Data data) {
            binding.tvTopTag.setVisibility(View.GONE);
            binding.tvTitle.setText(data.getTitle());
            binding.tvAuthor.setText(resolveAuthor(data));
            binding.tvTime.setText(buildTimeText(data));
            binding.tvClass.setText(resolveCategory(data));
            binding.getRoot().setOnClickListener(v -> ArticleWebViewUtil.start(v.getContext(), data));
            binding.ivFavorite.setEnabled(true);
            collectRequestRunning = false;
            renderFavorite(data.isCollect());
            binding.ivFavorite.setOnClickListener(v -> handleFavoriteToggle(data));
        }

        private void handleFavoriteToggle(@NonNull ArticleListBean.Data data) {
            if (collectRequestRunning) {
                return;
            }
            int articleId = data.getId();
            if (articleId <= 0) {
                showShortToast(itemView.getContext().getString(R.string.article_collect_failed));
                return;
            }
            if (!SessionManager.getInstance().isLoggedIn()) {
                showShortToast(itemView.getContext().getString(R.string.article_collect_need_login));
                itemView.getContext().startActivity(new Intent(itemView.getContext(), LoginActivity.class));
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
                disposable = userRepository.unCollectFavorite(articleId, resolveOriginId(data))
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
                if (targetCollect) {
                    renderFavorite(true);
                    showShortToast(itemView.getContext().getString(R.string.article_collect_success));
                } else {
                    showShortToast(itemView.getContext().getString(R.string.article_uncollect_success));
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        items.remove(position);
                        notifyItemRemoved(position);
                    }
                    if (actionListener != null) {
                        actionListener.onFavoriteRemoved(data);
                    }
                }
            } else {
                renderFavorite(data.isCollect());
                showShortToast(itemView.getContext().getString(R.string.article_collect_failed));
            }
        }

        private void handleCollectError(@NonNull Throwable throwable) {
            collectRequestRunning = false;
            binding.ivFavorite.setEnabled(true);
            showShortToast(itemView.getContext().getString(R.string.article_collect_failed));
        }

        private void renderFavorite(boolean collected) {
            binding.ivFavorite.setImageResource(collected ? R.drawable.bookmark_fill_24px : R.drawable.bookmark_24px);
            binding.ivFavorite.setContentDescription(itemView.getContext().getString(
                    collected ? R.string.article_uncollect : R.string.article_collect
            ));
        }

        private String resolveAuthor(@NonNull ArticleListBean.Data data) {
            String authorName = !TextUtils.isEmpty(data.getAuthor()) ? data.getAuthor() : data.getShareUser();
            if (TextUtils.isEmpty(authorName)) {
                authorName = itemView.getContext().getString(R.string.mine_placeholder_dash);
            }
            return authorName;
        }

        private String buildTimeText(@NonNull ArticleListBean.Data data) {
            String time = !StringUtils.isEmpty(data.getNiceShareDate()) ? data.getNiceShareDate() : data.getNiceDate();
            if (StringUtils.isEmpty(time)) {
                return "";
            }
            return " · " + time;
        }

        private String resolveCategory(@NonNull ArticleListBean.Data data) {
            if (!StringUtils.isEmpty(data.getSuperChapterName())) {
                return data.getSuperChapterName();
            }
            if (!StringUtils.isEmpty(data.getChapterName())) {
                return data.getChapterName();
            }
            return itemView.getContext().getString(R.string.mine_placeholder_dash);
        }

        private int resolveOriginId(@NonNull ArticleListBean.Data data) {
            int originId = data.getOriginId();
            return originId == 0 ? -1 : originId;
        }

        private void showShortToast(String message) {
            if (dialogController == null || TextUtils.isEmpty(message)) {
                return;
            }
            ToastUtils.showShortToast(dialogController, message);
        }
    }

    private static class FavoriteDiffCallback extends DiffUtil.Callback {
        private final List<ArticleListBean.Data> oldList;
        private final List<ArticleListBean.Data> newList;

        FavoriteDiffCallback(List<ArticleListBean.Data> oldList, List<ArticleListBean.Data> newList) {
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
            ArticleListBean.Data oldItem = oldList.get(oldItemPosition);
            ArticleListBean.Data newItem = newList.get(newItemPosition);
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ArticleListBean.Data oldItem = oldList.get(oldItemPosition);
            ArticleListBean.Data newItem = newList.get(newItemPosition);
            return TextUtils.equals(oldItem.getTitle(), newItem.getTitle())
                    && TextUtils.equals(oldItem.getDesc(), newItem.getDesc())
                    && TextUtils.equals(oldItem.getAuthor(), newItem.getAuthor())
                    && TextUtils.equals(oldItem.getShareUser(), newItem.getShareUser())
                    && TextUtils.equals(oldItem.getNiceShareDate(), newItem.getNiceShareDate())
                    && TextUtils.equals(oldItem.getNiceDate(), newItem.getNiceDate())
                    && TextUtils.equals(oldItem.getLink(), newItem.getLink());
        }
    }
}
