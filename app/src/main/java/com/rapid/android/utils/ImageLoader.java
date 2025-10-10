package com.rapid.android.utils;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.R;
import com.rapid.android.core.image.ImageConfig;

/**
 * 应用层图片加载器
 * 基于 core:image 模块提供应用特定的配置
 */
public final class ImageLoader {

    private static final ImageConfig DEFAULT_CONFIG = new ImageConfig.Builder()
            .setPlaceholder(R.drawable.image_placeholder)
            .setErrorPlaceholder(R.drawable.image_placeholder)
            .setScaleType(ImageView.ScaleType.CENTER_CROP)
            .build();

    static {
        // 设置全局默认配置
        com.rapid.android.core.image.ImageLoader.setDefaultConfig(DEFAULT_CONFIG);

        // 设置无图模式提供者
        com.rapid.android.core.image.ImageLoader.setNoImageModeProvider(
                () -> AppPreferences.isNoImageModeEnabled()
        );
    }

    private ImageLoader() {
        // 工具类，禁止实例化
    }

    /**
     * 加载图片
     */
    public static void load(@NonNull ImageView imageView, @Nullable String url) {
        com.rapid.android.core.image.ImageLoader.load(imageView, url);
    }

    /**
     * 加载图片或隐藏
     */
    public static void loadOrHide(@NonNull ImageView imageView, @Nullable String url) {
        com.rapid.android.core.image.ImageLoader.loadOrHide(imageView, url);
    }

    /**
     * 加载圆形图片
     */
    public static void loadCircle(@NonNull ImageView imageView, @Nullable String url) {
        com.rapid.android.core.image.ImageLoader.loadCircle(imageView, url);
    }

    /**
     * 清除图片缓存
     */
    public static void clearCache(@NonNull ImageView imageView) {
        com.rapid.android.core.image.ImageLoader.clearCache(imageView);
    }
}

