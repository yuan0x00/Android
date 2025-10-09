package com.rapid.android.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.rapid.android.R;

public final class ImageLoader {

    private static final RequestOptions DEFAULT_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .centerCrop();

    private ImageLoader() {
    }

    public static void load(@NonNull ImageView imageView, @Nullable String url) {
        if (AppPreferences.isNoImageModeEnabled()) {
            showPlaceholder(imageView);
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (TextUtils.isEmpty(url)) {
            Glide.with(imageView).clear(imageView);
            imageView.setImageResource(R.drawable.image_placeholder);
            return;
        }

        Glide.with(imageView)
                .load(url)
                .apply(DEFAULT_OPTIONS)
                .into(imageView);
    }

    public static void loadOrHide(@NonNull ImageView imageView, @Nullable String url) {
        if (AppPreferences.isNoImageModeEnabled()) {
            showPlaceholder(imageView);
            return;
        }

        if (TextUtils.isEmpty(url)) {
            Glide.with(imageView).clear(imageView);
            imageView.setVisibility(View.GONE);
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(imageView)
                .load(url)
                .apply(DEFAULT_OPTIONS)
                .into(imageView);
    }

    private static void showPlaceholder(@NonNull ImageView imageView) {
        Glide.with(imageView).clear(imageView);
        imageView.setVisibility(View.VISIBLE);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(R.drawable.image_placeholder);
    }
}
