package com.rapid.android.ui.common.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 仅通过顶部间距实现列表项间距，首项不添加额外间隔。
 */
public class SpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spacingPx;

    public SpacingItemDecoration(int spacingPx) {
        this.spacingPx = Math.max(spacingPx, 0);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        outRect.top = position == 0 ? spacingPx : 0;
        outRect.left = spacingPx;
        outRect.right = spacingPx;
        outRect.bottom = spacingPx;
    }
}
