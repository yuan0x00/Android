package com.rapid.android.ui.common;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.ui.common.decoration.TopSpacingItemDecoration;

/**
 * RecyclerView 装饰器工具类。
 */
public final class RecyclerViewDecorations {

    private RecyclerViewDecorations() {
    }

    public static void addTopSpacing(@NonNull RecyclerView recyclerView) {
        if (recyclerView.getContext() == null) {
            return;
        }
        Resources resources = recyclerView.getResources();
        int spacingPx = resources.getDimensionPixelSize(com.rapid.android.R.dimen.app_spacing_sm);

        for (int i = 0, count = recyclerView.getItemDecorationCount(); i < count; i++) {
            RecyclerView.ItemDecoration decoration = recyclerView.getItemDecorationAt(i);
            if (decoration instanceof TopSpacingItemDecoration) {
                return;
            }
        }

        recyclerView.addItemDecoration(new TopSpacingItemDecoration(spacingPx));
    }
}
