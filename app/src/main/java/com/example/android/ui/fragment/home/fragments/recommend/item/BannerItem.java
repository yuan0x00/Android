package com.example.android.ui.fragment.home.fragments.recommend.item;

import com.example.android.interfaces.IBannerItem;

public class BannerItem implements IBannerItem {
    public String url;

    public BannerItem(String url) {
        this.url = url;
    }

    @Override
    public String getImageUrl() {
        return url;
    }

}
