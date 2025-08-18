package com.example.android.ui.fragment.home.recycleView.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.ui.fragment.home.recycleView.item.FeedItem;

public class FeedViewHolder extends RecyclerView.ViewHolder {
    private final TextView content;

    public FeedViewHolder(@NonNull View itemView) {
        super(itemView);
        content = itemView.findViewById(R.id.feed_text);
    }

    public void bind(FeedItem feedItem) {
        content.setText(feedItem.text);
    }
}