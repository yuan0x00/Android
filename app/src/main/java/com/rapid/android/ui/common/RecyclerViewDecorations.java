package com.rapid.android.ui.common;

import android.content.res.Resources;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.ui.common.decoration.TopSpacingItemDecoration;

/**
 * RecyclerView 装饰器工具类。
 */
public final class RecyclerViewDecorations {

    private RecyclerViewDecorations() {
    }

    public static void addTopSpacing(@NonNull RecyclerView recyclerView, @DimenRes int spacingRes) {
        if (recyclerView.getContext() == null) {
            return;
        }
        Resources resources = recyclerView.getResources();
        int spacingPx = resources.getDimensionPixelSize(spacingRes);

        for (int i = 0, count = recyclerView.getItemDecorationCount(); i < count; i++) {
            RecyclerView.ItemDecoration decoration = recyclerView.getItemDecorationAt(i);
            if (decoration instanceof TopSpacingItemDecoration) {
                return;
            }
        }

        recyclerView.addItemDecoration(new TopSpacingItemDecoration(spacingPx));
    }
}
