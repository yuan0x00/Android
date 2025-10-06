package com.rapid.android.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * 控制长列表的“回到顶部”浮动按钮。
 */
public final class BackToTopController {

    private final FloatingActionButton backToTopButton;
    private final RecyclerView recyclerView;

    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
            updateVisibility();
        }
    };

    private BackToTopController(FloatingActionButton button, RecyclerView recyclerView) {
        this.backToTopButton = button;
        this.recyclerView = recyclerView;
        this.backToTopButton.setOnClickListener(v -> this.recyclerView.smoothScrollToPosition(0));
        this.recyclerView.addOnScrollListener(scrollListener);
        updateVisibility();
    }

    public static BackToTopController attach(@Nullable FloatingActionButton button,
                                             @Nullable RecyclerView recyclerView) {
        if (button == null || recyclerView == null) {
            return null;
        }
        button.hide();
        return new BackToTopController(button, recyclerView);
    }

    public void detach() {
        recyclerView.removeOnScrollListener(scrollListener);
        backToTopButton.setOnClickListener(null);
        backToTopButton.hide();
    }

    private void updateVisibility() {
        if (recyclerView.canScrollVertically(-1)) {
            backToTopButton.show();
        } else {
            backToTopButton.hide();
        }
    }
}
