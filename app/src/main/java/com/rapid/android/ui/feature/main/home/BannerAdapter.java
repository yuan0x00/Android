package com.rapid.android.ui.feature.main.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.core.domain.model.BannerItemBean;
import com.rapid.android.R;
import com.rapid.android.ui.view.ImageBanner;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private final List<BannerItemBean> banners;

    public BannerAdapter(List<BannerItemBean> banners) {
        this.banners = new ArrayList<>(banners);
    }

    public void setData(List<BannerItemBean> newData) {
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

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ImageBanner banner;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.banner);
        }

        public void bind(List<BannerItemBean> banners) {
            banner.setData(banners);
        }
    }
}
