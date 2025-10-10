package com.rapid.android.core.image;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

/**
 * 通用图片加载器
 * 封装 Glide 提供统一的图片加载接口
 */
public final class ImageLoader {

    private static ImageConfig defaultConfig = new ImageConfig.Builder().build();
    private static NoImageModeProvider noImageModeProvider = () -> false;

    private ImageLoader() {
        // 工具类，禁止实例化
    }

    /**
     * 设置全局默认配置
     */
    public static void setDefaultConfig(@NonNull ImageConfig config) {
        defaultConfig = config;
    }

    /**
     * 设置无图模式提供者
     */
    public static void setNoImageModeProvider(@NonNull NoImageModeProvider provider) {
        noImageModeProvider = provider;
    }

    /**
     * 加载图片（使用默认配置）
     */
    public static void load(@NonNull ImageView imageView, @Nullable String url) {
        load(imageView, url, defaultConfig);
    }

    /**
     * 加载图片（自定义配置）
     */
    public static void load(@NonNull ImageView imageView, @Nullable String url, @NonNull ImageConfig config) {
        // 检查无图模式
        if (noImageModeProvider.isNoImageMode()) {
            showPlaceholder(imageView, config.getPlaceholder());
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleType(config.getScaleType());

        if (TextUtils.isEmpty(url)) {
            Glide.with(imageView).clear(imageView);
            imageView.setImageResource(config.getPlaceholder());
            return;
        }

        RequestOptions options = buildRequestOptions(config);
        Glide.with(imageView)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * 加载图片或隐藏（URL为空时隐藏ImageView）
     */
    public static void loadOrHide(@NonNull ImageView imageView, @Nullable String url) {
        loadOrHide(imageView, url, defaultConfig);
    }

    /**
     * 加载图片或隐藏（自定义配置）
     */
    public static void loadOrHide(@NonNull ImageView imageView, @Nullable String url, @NonNull ImageConfig config) {
        // 检查无图模式
        if (noImageModeProvider.isNoImageMode()) {
            showPlaceholder(imageView, config.getPlaceholder());
            return;
        }

        if (TextUtils.isEmpty(url)) {
            Glide.with(imageView).clear(imageView);
            imageView.setVisibility(View.GONE);
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleType(config.getScaleType());

        RequestOptions options = buildRequestOptions(config);
        Glide.with(imageView)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * 加载圆形图片
     */
    public static void loadCircle(@NonNull ImageView imageView, @Nullable String url) {
        loadCircle(imageView, url, defaultConfig);
    }

    /**
     * 加载圆形图片（自定义配置）
     */
    public static void loadCircle(@NonNull ImageView imageView, @Nullable String url, @NonNull ImageConfig config) {
        if (noImageModeProvider.isNoImageMode()) {
            showPlaceholder(imageView, config.getPlaceholder());
            return;
        }

        imageView.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(url)) {
            Glide.with(imageView).clear(imageView);
            imageView.setImageResource(config.getPlaceholder());
            return;
        }

        RequestOptions options = buildRequestOptions(config).circleCrop();
        Glide.with(imageView)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * 清除图片缓存
     */
    public static void clearCache(@NonNull ImageView imageView) {
        Glide.with(imageView).clear(imageView);
    }

    /**
     * 构建 RequestOptions
     */
    private static RequestOptions buildRequestOptions(@NonNull ImageConfig config) {
        RequestOptions options = new RequestOptions()
                .placeholder(config.getPlaceholder())
                .error(config.getErrorPlaceholder());

        // 设置缩放模式
        switch (config.getScaleType()) {
            case CENTER_CROP:
                options = options.centerCrop();
                break;
            case CENTER_INSIDE:
                options = options.centerInside();
                break;
            case FIT_CENTER:
                options = options.fitCenter();
                break;
        }

        // 设置缓存策略
        options = options.diskCacheStrategy(config.getDiskCacheStrategy());

        // 设置跳过内存缓存
        if (config.isSkipMemoryCache()) {
            options = options.skipMemoryCache(true);
        }

        return options;
    }

    /**
     * 显示占位图
     */
    private static void showPlaceholder(@NonNull ImageView imageView, @DrawableRes int placeholder) {
        Glide.with(imageView).clear(imageView);
        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(placeholder);
    }

    /**
     * 无图模式提供者接口
     */
    public interface NoImageModeProvider {
        boolean isNoImageMode();
    }
}
