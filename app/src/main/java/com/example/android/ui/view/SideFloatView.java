package com.example.android.ui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SideFloatView extends FrameLayout {

    private static final long AUTO_COLLAPSE_DELAY = 1000;
    private static final long ANIMATION_DURATION = 200;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsExpanded = true;
    private int mWidth;
    private int mScreenWidth;
    private final Runnable mAutoCollapseRunnable = this::collapse;

    public SideFloatView(Context context) {
        this(context, null);
    }

    public SideFloatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideFloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        setClickable(true);

        setOnClickListener(v -> toggle());

        post(this::measureAndStoreWidth);
        postDelayed(mAutoCollapseRunnable, AUTO_COLLAPSE_DELAY);
    }

    private void measureAndStoreWidth() {
        mWidth = getWidth();
        if (mWidth == 0) {
            post(this::measureAndStoreWidth);
        } else {
            setX(mScreenWidth - mWidth);
        }
    }

    private void toggle() {
        if (mIsExpanded) {
            collapse();
        } else {
            expand();
            // 每次展开后都重新开始倒计时
            mHandler.removeCallbacks(mAutoCollapseRunnable);
            mHandler.postDelayed(mAutoCollapseRunnable, AUTO_COLLAPSE_DELAY);
        }
    }

    private void expand() {
        if (mIsExpanded || mWidth == 0) return;
        animate()
                .x(mScreenWidth - mWidth)
                .setDuration(ANIMATION_DURATION)
                .start();
        mIsExpanded = true;
    }

    private void collapse() {
        if (!mIsExpanded || mWidth == 0) return;
        float halfVisibleX = mScreenWidth - mWidth / 2f;
        animate()
                .x(halfVisibleX)
                .setDuration(ANIMATION_DURATION)
                .start();
        mIsExpanded = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        if (changed && mWidth > 0) {
            setX(mScreenWidth - mWidth);
        }
    }

}