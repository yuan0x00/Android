package com.example.android.ui.fragment.home.recycleView.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.ui.fragment.home.recycleView.item.BannerItem;
import com.example.android.ui.fragment.home.recycleView.viewHolder.BannerViewHolder;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerViewHolder> {
    private final List<BannerItem> banners;

    public BannerAdapter(List<BannerItem> banners) {
        this.banners = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.bind(banners);
    }

    @Override
    public int getItemCount() {
        return 1; // 只有一个 banner
    }
}