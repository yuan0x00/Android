package com.rapid.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.rapid.android.R;
import com.rapid.android.core.domain.model.BannerItemBean;

public class ImageBanner extends BaseBanner<BannerItemBean> {

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
    protected void bindItem(View itemView, BannerItemBean item, int position) {
        ImageView iv = itemView.findViewById(R.id.iv_image);
        // 使用 Glide 加载图片
        Glide.with(itemView)
                .load(item.getImagePath())
                .centerCrop()
                .into(iv);
    }
}
