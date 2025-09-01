package com.example.android.ui.fragment.home.fragments.recommend.viewHolder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.R;
import com.example.android.interfaces.IBannerItem;
import com.example.android.ui.fragment.home.fragments.recommend.view.ImageBanner;

import java.util.List;

public class BannerViewHolder extends RecyclerView.ViewHolder {
    private final ImageBanner banner;

    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);
        banner = itemView.findViewById(R.id.banner);
    }

    public void bind(List<IBannerItem> banners) {
        banner.setData(banners);
    }
}