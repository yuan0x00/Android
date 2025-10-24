package com.rapid.android.feature.main.discover.wenda;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.databinding.ItemWendaBinding;
import com.rapid.android.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

class WendaAdapter extends RecyclerView.Adapter<WendaAdapter.WendaViewHolder> {

    private final List<ArticleListBean.Data> items = new ArrayList<>();

    void submitList(List<ArticleListBean.Data> data) {
        List<ArticleListBean.Data> newItems = data != null ? new ArrayList<>(data) : new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new WendaDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public WendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWendaBinding binding = ItemWendaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new WendaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WendaViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WendaViewHolder extends RecyclerView.ViewHolder {

        private final ItemWendaBinding binding;

        WendaViewHolder(@NonNull ItemWendaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ArticleListBean.Data bean) {
            if (bean == null) {
                binding.wendaTitle.setText("");
                binding.wendaAuthor.setVisibility(View.GONE);
                return;
            }

            String title = bean.getTitle();
            binding.wendaTitle.setText(title != null ? title : "");

            String author = getDisplayAuthor(bean);
            if (TextUtils.isEmpty(author)) {
                binding.wendaAuthor.setVisibility(View.GONE);
            } else {
                binding.wendaAuthor.setVisibility(View.VISIBLE);
                binding.wendaAuthor.setText(author);
            }

            binding.getRoot().setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), bean));
        }

        private String getDisplayAuthor(ArticleListBean.Data bean) {
            String author = bean.getAuthor();
            if (TextUtils.isEmpty(author)) {
                author = bean.getShareUser();
            }
            return author != null ? author : "";
        }
    }

    private static class WendaDiffCallback extends DiffUtil.Callback {
        private final List<ArticleListBean.Data> oldList;
        private final List<ArticleListBean.Data> newList;

        WendaDiffCallback(List<ArticleListBean.Data> oldList, List<ArticleListBean.Data> newList) {
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
            return oldItem != null && newItem != null && oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ArticleListBean.Data oldItem = oldList.get(oldItemPosition);
            ArticleListBean.Data newItem = newList.get(newItemPosition);
            if (oldItem == null || newItem == null) {
                return oldItem == newItem;
            }
            return TextUtils.equals(oldItem.getTitle(), newItem.getTitle())
                    && TextUtils.equals(oldItem.getAuthor(), newItem.getAuthor())
                    && TextUtils.equals(oldItem.getShareUser(), newItem.getShareUser())
                    && oldItem.getId() == newItem.getId();
        }
    }
}
