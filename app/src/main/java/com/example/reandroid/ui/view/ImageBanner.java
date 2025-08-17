package com.example.reandroid.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.core.base.BaseBanner;
import com.example.reandroid.R;
import com.example.reandroid.ui.fragment.home.recycleView.item.BannerItem;

public class ImageBanner extends BaseBanner<BannerItem> {

    public ImageBanner(Context context) {
        super(context);
    }

    public ImageBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.banner_image;
    }

    @Override
    protected void bindItem(View itemView, BannerItem bannerItem, int position) {
        ImageView iv = itemView.findViewById(R.id.iv_image);
        // 使用 Glide 加载图片
        Glide.with(itemView)
                .load(bannerItem.url)
                .centerCrop()
                .into(iv);
    }
}
