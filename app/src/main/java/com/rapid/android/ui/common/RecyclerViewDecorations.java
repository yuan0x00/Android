package com.rapid.android.ui.common;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rapid.android.ui.common.decoration.SpacingItemDecoration;

/**
 * RecyclerView 装饰器工具类。
 */
public final class RecyclerViewDecorations {

    private RecyclerViewDecorations() {
    }

    public static void addSpacing(@NonNull RecyclerView recyclerView) {
        if (recyclerView.getContext() == null) {
            return;
        }
        Resources resources = recyclerView.getResources();
        int spacingPx = resources.getDimensionPixelSize(com.rapid.android.R.dimen.app_spacing_sm);

        for (int i = 0, count = recyclerView.getItemDecorationCount(); i < count; i++) {
            RecyclerView.ItemDecoration decoration = recyclerView.getItemDecorationAt(i);
            if (decoration instanceof SpacingItemDecoration) {
                return;
            }
        }

        recyclerView.addItemDecoration(new SpacingItemDecoration(spacingPx));
    }
}
