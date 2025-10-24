package com.rapid.android.ui.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rapid.android.feature.main.TabNavigator;
import com.rapid.android.utils.AppPreferences;

/**
 * 控制长列表的“回到顶部”浮动按钮。
 */
public final class BackToTopController {

    private final FloatingActionButton backToTopButton;
    private final RecyclerView recyclerView;
    @Nullable
    private final TabNavigator tabNavigator;
    private final int scrollThreshold;

    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
            updateBackToTopVisibility();
            handleBottomBarOnScroll(dy);
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                handleBottomBarOnIdle();
            }
        }
    };

    private BackToTopController(FloatingActionButton button,
                                RecyclerView recyclerView,
                                @Nullable TabNavigator tabNavigator,
                                int scrollThreshold) {
        this.backToTopButton = button;
        this.recyclerView = recyclerView;
        this.tabNavigator = tabNavigator;
        this.scrollThreshold = Math.max(0, scrollThreshold);
        this.backToTopButton.setOnClickListener(v -> this.recyclerView.smoothScrollToPosition(0));
        this.recyclerView.addOnScrollListener(scrollListener);
        updateBackToTopVisibility();
        handleBottomBarOnIdle();
    }

    public static BackToTopController attach(@Nullable FloatingActionButton button,
                                             @Nullable RecyclerView recyclerView) {
        return attach(button, recyclerView, null);
    }

    public static BackToTopController attach(@Nullable FloatingActionButton button,
                                             @Nullable RecyclerView recyclerView,
                                             @Nullable TabNavigator tabNavigator) {
        return attach(button, recyclerView, tabNavigator, 8);
    }

    public static BackToTopController attach(@Nullable FloatingActionButton button,
                                             @Nullable RecyclerView recyclerView,
                                             @Nullable TabNavigator tabNavigator,
                                             int scrollThreshold) {
        if (button == null || recyclerView == null) {
            return null;
        }
        button.hide();
        return new BackToTopController(button, recyclerView, tabNavigator, scrollThreshold);
    }

    public void detach() {
        recyclerView.removeOnScrollListener(scrollListener);
        backToTopButton.setOnClickListener(null);
        backToTopButton.hide();
        handleBottomBarRestore();
    }

    private void updateBackToTopVisibility() {
        if (recyclerView.canScrollVertically(-1)) {
            backToTopButton.show();
        } else {
            backToTopButton.hide();
        }
    }

    private void handleBottomBarOnScroll(int dy) {
        if (tabNavigator == null) {
            return;
        }
        if (!AppPreferences.isAutoHideBottomBarEnabled()) {
            if (!tabNavigator.isBottomBarVisible()) {
                tabNavigator.showBottomBar(false);
            }
            return;
        }

        boolean canScrollUp = recyclerView.canScrollVertically(-1);
        boolean canScrollDown = recyclerView.canScrollVertically(1);

        if (dy > scrollThreshold && canScrollDown) {
            tabNavigator.hideBottomBar(true);
        } else if (dy < -scrollThreshold || !canScrollUp) {
            tabNavigator.showBottomBar(true);
        }

        if (!canScrollUp || !canScrollDown) {
            tabNavigator.showBottomBar(true);
        }
    }

    private void handleBottomBarOnIdle() {
        if (tabNavigator == null) {
            return;
        }
        if (!AppPreferences.isAutoHideBottomBarEnabled()) {
            if (!tabNavigator.isBottomBarVisible()) {
                tabNavigator.showBottomBar(false);
            }
            return;
        }

        boolean canScrollUp = recyclerView.canScrollVertically(-1);
        boolean canScrollDown = recyclerView.canScrollVertically(1);

        if (!canScrollUp || !canScrollDown) {
            tabNavigator.showBottomBar(true);
            return;
        }

        if (!tabNavigator.isBottomBarVisible()) {
            tabNavigator.showBottomBar(true);
        }
    }

    private void handleBottomBarRestore() {
        if (tabNavigator == null) {
            return;
        }
        if (!tabNavigator.isBottomBarVisible()) {
            tabNavigator.showBottomBar(false);
        }
    }
}
