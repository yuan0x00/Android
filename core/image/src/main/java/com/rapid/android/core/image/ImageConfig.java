package com.rapid.android.core.image;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * 图片加载配置类
 */
public class ImageConfig {

    private final int placeholder;
    private final int errorPlaceholder;
    private final ImageView.ScaleType scaleType;
    private final DiskCacheStrategy diskCacheStrategy;
    private final boolean skipMemoryCache;

    private ImageConfig(Builder builder) {
        this.placeholder = builder.placeholder;
        this.errorPlaceholder = builder.errorPlaceholder;
        this.scaleType = builder.scaleType;
        this.diskCacheStrategy = builder.diskCacheStrategy;
        this.skipMemoryCache = builder.skipMemoryCache;
    }

    @DrawableRes
    public int getPlaceholder() {
        return placeholder;
    }

    @DrawableRes
    public int getErrorPlaceholder() {
        return errorPlaceholder;
    }

    @NonNull
    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    @NonNull
    public DiskCacheStrategy getDiskCacheStrategy() {
        return diskCacheStrategy;
    }

    public boolean isSkipMemoryCache() {
        return skipMemoryCache;
    }

    /**
     * Builder 模式构建配置
     */
    public static class Builder {
        private int placeholder = android.R.drawable.ic_menu_report_image;
        private int errorPlaceholder = android.R.drawable.ic_menu_report_image;
        private ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;
        private DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.AUTOMATIC;
        private boolean skipMemoryCache = false;

        public Builder setPlaceholder(@DrawableRes int placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder setErrorPlaceholder(@DrawableRes int errorPlaceholder) {
            this.errorPlaceholder = errorPlaceholder;
            return this;
        }

        public Builder setScaleType(@NonNull ImageView.ScaleType scaleType) {
            this.scaleType = scaleType;
            return this;
        }

        public Builder setDiskCacheStrategy(@NonNull DiskCacheStrategy strategy) {
            this.diskCacheStrategy = strategy;
            return this;
        }

        public Builder setSkipMemoryCache(boolean skip) {
            this.skipMemoryCache = skip;
            return this;
        }

        public ImageConfig build() {
            return new ImageConfig(this);
        }
    }
}
