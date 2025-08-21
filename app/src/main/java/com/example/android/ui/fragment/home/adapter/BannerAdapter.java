package com.example.android.ui.fragment.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.interfaces.IBannerItem;
import com.example.android.ui.fragment.home.viewHolder.BannerViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerViewHolder> {
    private final List<IBannerItem> banners;

    public BannerAdapter(List<? extends IBannerItem> banners) {
        this.banners = new ArrayList<>(banners);
    }

    public void setData(List<? extends IBannerItem> newData) {
        this.banners.clear();
        this.banners.addAll(newData);
        notifyDataSetChanged(); // 或使用 DiffUtil 更高效
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