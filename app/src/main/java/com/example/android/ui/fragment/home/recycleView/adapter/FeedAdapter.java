package com.example.android.ui.fragment.home.recycleView.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.ui.fragment.home.recycleView.item.FeedItem;
import com.example.android.ui.fragment.home.recycleView.viewHolder.FeedViewHolder;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {
    private final List<FeedItem> feeds;

    public FeedAdapter(List<FeedItem> feeds) {
        this.feeds = feeds;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.bind(feeds.get(position));
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }
}