package com.rapid.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.R;
import com.rapid.android.data.model.ArticleListBean;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(feeds.getDatas().get(position));
    }

    @Override
    public int getItemCount() {
        return feeds.getDatas().size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        private final TextView content;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.feed_text);
        }

        public void bind(ArticleListBean.Data feedItem) {
            content.setText(feedItem.getTitle());
        }
    }
}