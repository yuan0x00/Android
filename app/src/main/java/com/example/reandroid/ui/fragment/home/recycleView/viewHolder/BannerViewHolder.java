package com.example.reandroid.ui.fragment.home.recycleView.viewHolder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reandroid.R;
import com.example.reandroid.ui.fragment.home.recycleView.item.BannerItem;
import com.example.reandroid.ui.view.ImageBanner;

import java.util.List;

public class BannerViewHolder extends RecyclerView.ViewHolder {
    private final ImageBanner banner;

    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);
        banner = itemView.findViewById(R.id.banner);
    }

    public void bind(List<BannerItem> banners) {
        banner.setData(banners);
    }
}