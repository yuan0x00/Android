package com.rapid.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.utils.lang.StringUtils;
import com.rapid.android.data.model.ArticleListBean;
import com.rapid.android.databinding.ItemFeedBinding;
import com.webview.WebViewActivity;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private ArticleListBean feeds;


    public FeedAdapter(ArticleListBean feeds) {
        this.feeds = feeds;
    }

    public void setData(ArticleListBean newData) {
        this.feeds = newData;
        notifyDataSetChanged(); // 或使用 DiffUtil 更高效
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedBinding binding = ItemFeedBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FeedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(feeds.getDatas().get(position));
    }

    @Override
    public int getItemCount() {
        return feeds != null ? feeds.getDatas().size() : 0;
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ItemFeedBinding binding;

        public FeedViewHolder(@NonNull ItemFeedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ArticleListBean.Data feedItem) {
            binding.getRoot().setOnClickListener(v -> WebViewActivity.start(binding.getRoot().getContext(), feedItem.getLink(), feedItem.getTitle()));
            binding.tvTitle.setText(feedItem.getTitle());
            String author;
            if (StringUtils.isEmpty(feedItem.getAuthor())) {
                author = "分享人：" + feedItem.getShareUser();
            } else {
                author = "作者：" + feedItem.getAuthor();
            }
            binding.tvAuthor.setText(author);
            binding.tvTime.setText(String.valueOf(feedItem.getNiceShareDate()));
            String chapter = "分类：" + feedItem.getSuperChapterName() + "/" + feedItem.getChapterName();
            binding.tvClass.setText(chapter);
        }
    }
}