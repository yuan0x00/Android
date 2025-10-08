package com.rapid.android.ui.feature.main.mine.favorite;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

final class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private final Callback callback;

    private final List<ArticleListBean.Data> items = new ArrayList<>();
    FavoriteAdapter(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_article, parent, false);
        return new FavoriteViewHolder(view, callback);
    }

    void submitNewList(List<ArticleListBean.Data> data) {
        List<ArticleListBean.Data> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FavoriteDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    void appendList(List<ArticleListBean.Data> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    interface Callback {
        void onEdit(ArticleListBean.Data data);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView desc;
        private final TextView author;
        private final TextView meta;
        private final com.google.android.material.button.MaterialButton editButton;
        private final Callback callback;

        FavoriteViewHolder(@NonNull View itemView, Callback callback) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            desc = itemView.findViewById(R.id.tvDesc);
            author = itemView.findViewById(R.id.tvAuthor);
            meta = itemView.findViewById(R.id.tvMeta);
            editButton = itemView.findViewById(R.id.btnEdit);
            this.callback = callback;
        }

        void bind(ArticleListBean.Data data) {
            title.setText(data.getTitle());

            if (!TextUtils.isEmpty(data.getDesc())) {
                desc.setVisibility(View.VISIBLE);
                desc.setText(data.getDesc());
            } else {
                desc.setVisibility(View.GONE);
                desc.setText("");
            }

            String authorName = !TextUtils.isEmpty(data.getAuthor()) ? data.getAuthor() : data.getShareUser();
            if (TextUtils.isEmpty(authorName)) {
                authorName = itemView.getContext().getString(R.string.mine_placeholder_dash);
            }
            author.setText(authorName);

            StringBuilder metaBuilder = new StringBuilder();
            if (!TextUtils.isEmpty(data.getSuperChapterName())) {
                metaBuilder.append(data.getSuperChapterName());
            }
            if (!TextUtils.isEmpty(data.getNiceShareDate())) {
                if (metaBuilder.length() > 0) {
                    metaBuilder.append(" · ");
                }
                metaBuilder.append(data.getNiceShareDate());
            } else if (!TextUtils.isEmpty(data.getNiceDate())) {
                if (metaBuilder.length() > 0) {
                    metaBuilder.append(" · ");
                }
                metaBuilder.append(data.getNiceDate());
            }
            meta.setText(metaBuilder.toString());

            itemView.setOnClickListener(v -> {
                if (!TextUtils.isEmpty(data.getLink())) {
                    ArticleWebViewActivity.start(v.getContext(), data);
                }
            });

            if (callback != null) {
                editButton.setVisibility(View.VISIBLE);
                editButton.setOnClickListener(v -> callback.onEdit(data));
            } else {
                editButton.setVisibility(View.GONE);
            }
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
