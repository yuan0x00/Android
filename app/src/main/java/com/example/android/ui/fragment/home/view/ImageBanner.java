package com.example.android.ui.fragment.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.android.R;
import com.example.android.base.BaseBanner;
import com.example.android.interfaces.IBannerItem;

public class ImageBanner extends BaseBanner<IBannerItem> {

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
    protected void bindItem(View itemView, IBannerItem item, int position) {
        ImageView iv = itemView.findViewById(R.id.iv_image);
        // 使用 Glide 加载图片
        Glide.with(itemView)
                .load(item.getImageUrl())
                .centerCrop()
                .into(iv);
    }
}
