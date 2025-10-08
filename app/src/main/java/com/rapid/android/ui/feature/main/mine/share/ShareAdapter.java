package com.rapid.android.ui.feature.main.mine.share;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.core.common.text.StringUtils;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.databinding.ItemFeedBinding;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

final class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ShareViewHolder> {

    private final List<ArticleListBean.Data> items = new ArrayList<>();

    void submitNewList(List<ArticleListBean.Data> data) {
        List<ArticleListBean.Data> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ShareDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ShareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ShareViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ShareViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedBinding binding;

        ShareViewHolder(@NonNull ItemFeedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ArticleListBean.Data data) {
            binding.tvTopTag.setVisibility(View.GONE);
            binding.ivFavorite.setVisibility(View.GONE);
            binding.tvTitle.setText(data.getTitle());

            String authorName = !TextUtils.isEmpty(data.getShareUser())
                    ? data.getShareUser()
                    : data.getAuthor();
            if (TextUtils.isEmpty(authorName)) {
                authorName = itemView.getContext().getString(R.string.mine_placeholder_dash);
            }
            binding.tvAuthor.setText(authorName);

            String time = !StringUtils.isEmpty(data.getNiceShareDate()) ? data.getNiceShareDate() : data.getNiceDate();
            binding.tvTime.setText(!StringUtils.isEmpty(time) ? " · " + time : "");

            if (!StringUtils.isEmpty(data.getSuperChapterName())) {
                binding.tvClass.setText(data.getSuperChapterName());
                binding.tvClass.setVisibility(View.VISIBLE);
            } else if (!StringUtils.isEmpty(data.getChapterName())) {
                binding.tvClass.setText(data.getChapterName());
                binding.tvClass.setVisibility(View.VISIBLE);
            } else {
                binding.tvClass.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (!TextUtils.isEmpty(data.getLink())) {
                    ArticleWebViewActivity.start(v.getContext(), data);
                }
            });
        }
    }

    private static class ShareDiffCallback extends DiffUtil.Callback {
        private final List<ArticleListBean.Data> oldList;
        private final List<ArticleListBean.Data> newList;

        ShareDiffCallback(List<ArticleListBean.Data> oldList, List<ArticleListBean.Data> newList) {
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
            return TextUtils.equals(oldItem.getTitle(), newItem.getTitle())
                    && TextUtils.equals(oldItem.getDesc(), newItem.getDesc())
                    && TextUtils.equals(oldItem.getLink(), newItem.getLink())
                    && TextUtils.equals(oldItem.getNiceShareDate(), newItem.getNiceShareDate())
                    && TextUtils.equals(oldItem.getNiceDate(), newItem.getNiceDate())
                    && oldItem.isCollect() == newItem.isCollect();
        }
    }
}
