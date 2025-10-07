package com.rapid.android.ui.feature.main.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rapid.android.core.domain.model.ArticleListBean;
import com.rapid.android.core.domain.model.FriendLinkBean;
import com.rapid.android.databinding.ItemHomeHighlightBinding;
import com.rapid.android.ui.feature.web.ArticleWebViewActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeHighlightAdapter extends RecyclerView.Adapter<HomeHighlightAdapter.HighlightViewHolder> {

    private HighlightData data = new HighlightData(new ArrayList<>(), new ArrayList<>());

    public void setData(HighlightData data) {
        if (data == null) {
            this.data = new HighlightData(new ArrayList<>(), new ArrayList<>());
        } else {
            this.data = data;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HighlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeHighlightBinding binding = ItemHomeHighlightBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HighlightViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HighlightViewHolder holder, int position) {
        holder.bind(data);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class HighlightData {
        final List<ArticleListBean.Data> topArticles;
        final List<FriendLinkBean> friendLinks;

        public HighlightData(List<ArticleListBean.Data> topArticles,
                             List<FriendLinkBean> friendLinks) {
            this.topArticles = topArticles != null ? topArticles : new ArrayList<>();
            this.friendLinks = friendLinks != null ? friendLinks : new ArrayList<>();
        }
    }

    static class HighlightViewHolder extends RecyclerView.ViewHolder {

        private final ItemHomeHighlightBinding binding;

        HighlightViewHolder(@NonNull ItemHomeHighlightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HighlightData data) {
            bindTopArticles(data.topArticles);
            bindFriendLinks(data.friendLinks);
        }

        private void bindTopArticles(List<ArticleListBean.Data> topArticles) {
            binding.topArticlesContainer.removeAllViews();
            if (topArticles == null || topArticles.isEmpty()) {
                binding.titleTopArticles.setVisibility(View.GONE);
                binding.topArticlesContainer.setVisibility(View.GONE);
            } else {
                binding.titleTopArticles.setVisibility(View.VISIBLE);
                binding.topArticlesContainer.setVisibility(View.VISIBLE);
                LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
                int count = Math.min(3, topArticles.size());
                for (int i = 0; i < count; i++) {
                    ArticleListBean.Data item = topArticles.get(i);
                    View itemView = inflater.inflate(android.R.layout.simple_list_item_2, binding.topArticlesContainer, false);
                    android.widget.TextView title = itemView.findViewById(android.R.id.text1);
                    android.widget.TextView subtitle = itemView.findViewById(android.R.id.text2);
                    title.setText(item.getTitle());
                    if (subtitle != null) {
                        subtitle.setText(item.getAuthor() != null && !item.getAuthor().isEmpty()
                                ? item.getAuthor()
                                : item.getNiceDate());
                    }
                    itemView.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), item));
                    binding.topArticlesContainer.addView(itemView);
                }
            }
        }

        private void bindFriendLinks(List<FriendLinkBean> friendLinks) {
            binding.friendLinksGroup.removeAllViews();
            if (friendLinks == null || friendLinks.isEmpty()) {
                binding.titleFriendLinks.setVisibility(View.GONE);
                binding.friendLinksGroup.setVisibility(View.GONE);
            } else {
                binding.titleFriendLinks.setVisibility(View.VISIBLE);
                binding.friendLinksGroup.setVisibility(View.VISIBLE);
                int count = Math.min(8, friendLinks.size());
                for (int i = 0; i < count; i++) {
                    FriendLinkBean bean = friendLinks.get(i);
                    Chip chip = new Chip(binding.getRoot().getContext());
                    chip.setText(bean.getName());
                    chip.setCheckable(false);
                    chip.setEnsureMinTouchTargetSize(false);
                    chip.setOnClickListener(v -> ArticleWebViewActivity.start(v.getContext(), bean.getLink(), bean.getName()));
                    binding.friendLinksGroup.addView(chip);
                }
            }
        }
    }
}
