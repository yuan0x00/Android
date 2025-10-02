package com.rapid.android.ui.common;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public final class ContentStateController {

    private final SwipeRefreshLayout swipeRefreshLayout;
    private final View progressView;
    private final View emptyView;

    public ContentStateController(@Nullable SwipeRefreshLayout swipeRefreshLayout,
                                  @Nullable View progressView,
                                  @Nullable View emptyView) {
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.progressView = progressView;
        this.emptyView = emptyView;
    }

    public void setLoading(boolean loading) {
        if (!loading) {
            stopRefreshing();
            if (progressView != null) {
                progressView.setVisibility(View.GONE);
            }
            return;
        }

        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            if (progressView != null) {
                progressView.setVisibility(View.GONE);
            }
        } else if (progressView != null) {
            progressView.setVisibility(View.VISIBLE);
        }

        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }

    public void setEmpty(boolean empty) {
        stopRefreshing();
        if (emptyView != null) {
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
        if (!empty && progressView != null) {
            progressView.setVisibility(View.GONE);
        }
    }

    public void stopRefreshing() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
