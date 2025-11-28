package com.rapid.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;

import java.util.ArrayList;
import java.util.List;

public class SwipeActionsLayout extends ViewGroup {

    private static final int STATE_CLOSED = 0;
    private static final int STATE_LEFT_OPEN = 1;
    private static final int STATE_RIGHT_OPEN = 2;
    private View mainContent;
    private View leftMenu;
    private View rightMenu;
    private int currentState = STATE_CLOSED;
    private ViewDragHelper dragHelper;
    private OnSwipeActionsListener listener;
    // 滑动配置
    private float swipeThreshold = 0.5f; // 滑动阈值

    public SwipeActionsLayout(Context context) {
        this(context, null);
    }

    public SwipeActionsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeActionsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        dragHelper = ViewDragHelper.create(this, 1.0f, new DragCallback());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 获取子View
        List<View> children = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            children.add(getChildAt(i));
        }

        // 根据ID或位置分配角色（这里需要根据你的布局调整）
        if (!children.isEmpty()) mainContent = children.get(0);
        if (children.size() > 1) leftMenu = children.get(1);
        if (children.size() > 2) rightMenu = children.get(2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // 先测量主内容，它决定整体高度
        if (mainContent != null) {
            measureChild(mainContent, widthMeasureSpec, heightMeasureSpec);
        }

        int contentHeight = mainContent != null ?
                mainContent.getMeasuredHeight() : MeasureSpec.getSize(heightMeasureSpec);

        // 测量左侧菜单
        if (leftMenu != null) {
            int leftMenuWidthSpec = MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.AT_MOST);
            int leftMenuHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
            measureChild(leftMenu, leftMenuWidthSpec, leftMenuHeightSpec);
        }

        // 测量右侧菜单
        if (rightMenu != null) {
            int rightMenuWidthSpec = MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.AT_MOST);
            int rightMenuHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
            measureChild(rightMenu, rightMenuWidthSpec, rightMenuHeightSpec);
        }

        // 最终高度由主内容决定
        setMeasuredDimension(width, contentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        // 布局左侧菜单（隐藏在左边）
        if (leftMenu != null) {
            int leftWidth = leftMenu.getMeasuredWidth();
            leftMenu.layout(-leftWidth, 0, 0, height);
        }

        // 布局主内容（覆盖整个区域）
        if (mainContent != null) {
            mainContent.layout(0, 0, width, height);
        }

        // 布局右侧菜单（隐藏在右边）
        if (rightMenu != null) {
            int rightWidth = rightMenu.getMeasuredWidth();
            rightMenu.layout(width, 0, width + rightWidth, height);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    private void updateState(int left) {
        int newState = STATE_CLOSED;

        if (left > 0) {
            newState = STATE_LEFT_OPEN;
        } else if (left < 0) {
            newState = STATE_RIGHT_OPEN;
        }

        if (newState != currentState) {
            currentState = newState;
            notifyStateChanged();
        }
    }

    private void notifyStateChanged() {
        if (listener == null) return;

        switch (currentState) {
            case STATE_LEFT_OPEN:
                listener.onLeftMenuOpened();
                break;
            case STATE_RIGHT_OPEN:
                listener.onRightMenuOpened();
                break;
            case STATE_CLOSED:
                listener.onClosed();
                break;
        }
    }

    // 公共方法
    public void openLeftMenu() {
        if (leftMenu != null) {
            int left = leftMenu.getWidth();
            if (dragHelper.smoothSlideViewTo(mainContent, left, mainContent.getTop())) {
                this.postInvalidateOnAnimation();
            }
            updateState(left);
        }
    }

    public void openRightMenu() {
        if (rightMenu != null) {
            int left = -rightMenu.getWidth();
            if (dragHelper.smoothSlideViewTo(mainContent, left, mainContent.getTop())) {
                this.postInvalidateOnAnimation();
            }
            updateState(left);
        }
    }

    public void closeMenu() {
        if (dragHelper.smoothSlideViewTo(mainContent, 0, mainContent.getTop())) {
            this.postInvalidateOnAnimation();
        }
        updateState(0);
    }

    public boolean isMenuOpen() {
        return currentState != STATE_CLOSED;
    }

    public void setSwipeThreshold(float threshold) {
        this.swipeThreshold = Math.max(0.1f, Math.min(threshold, 1.0f));
    }

    public void setOnSwipeActionsListener(OnSwipeActionsListener listener) {
        this.listener = listener;
    }

    public interface OnSwipeActionsListener {
        void onLeftMenuOpened();

        void onRightMenuOpened();

        void onClosed();

        void onLeftMenuAction(int actionId);

        void onRightMenuAction(int actionId);
    }

    private class DragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == mainContent && isEnabled();
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            int leftRange = leftMenu != null ? leftMenu.getWidth() : 0;
            int rightRange = rightMenu != null ? rightMenu.getWidth() : 0;
            return Math.max(leftRange, rightRange);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            int leftMenuWidth = leftMenu != null ? leftMenu.getWidth() : 0;
            int rightMenuWidth = rightMenu != null ? rightMenu.getWidth() : 0;

            return switch (currentState) {
                case STATE_LEFT_OPEN ->
                    // 左侧打开时：只能从打开位置滑动到关闭位置
                        Math.max(0, Math.min(left, leftMenuWidth));
                case STATE_RIGHT_OPEN ->
                    // 右侧打开时：只能从打开位置滑动到关闭位置
                        Math.max(-rightMenuWidth, Math.min(left, 0));
                default ->
                    // 关闭状态：正常滑动范围
                        Math.max(-rightMenuWidth, Math.min(left, leftMenuWidth));
            };
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            // 同步移动菜单
            if (leftMenu != null) {
                leftMenu.offsetLeftAndRight(dx);
            }
            if (rightMenu != null) {
                rightMenu.offsetLeftAndRight(dx);
            }
            invalidate();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            int left = releasedChild.getLeft();
            int leftMenuWidth = leftMenu != null ? leftMenu.getWidth() : 0;
            int rightMenuWidth = rightMenu != null ? rightMenu.getWidth() : 0;
            int settleLeft;

            // 最小滑动速度
            int minFlingVelocity = 400;
            if (currentState == STATE_LEFT_OPEN) {
                // 左侧打开时：向右滑动关闭，向左滑动保持打开
                if (xvel > minFlingVelocity || left < leftMenuWidth * (1 - swipeThreshold)) {
                    settleLeft = 0; // 关闭
                } else {
                    settleLeft = leftMenuWidth; // 保持打开
                }
            } else if (currentState == STATE_RIGHT_OPEN) {
                // 右侧打开时：向左滑动关闭，向右滑动保持打开
                if (xvel < -minFlingVelocity || left > -rightMenuWidth * (1 - swipeThreshold)) {
                    settleLeft = 0; // 关闭
                } else {
                    settleLeft = -rightMenuWidth; // 保持打开
                }
            } else {
                // 关闭状态：正常逻辑
                if (xvel > minFlingVelocity) {
                    settleLeft = leftMenuWidth;
                } else if (xvel < -minFlingVelocity) {
                    settleLeft = -rightMenuWidth;
                } else {
                    if (left > leftMenuWidth * swipeThreshold) {
                        settleLeft = leftMenuWidth;
                    } else if (left < -rightMenuWidth * swipeThreshold) {
                        settleLeft = -rightMenuWidth;
                    } else {
                        settleLeft = 0;
                    }
                }
            }

            if (dragHelper.settleCapturedViewAt(settleLeft, releasedChild.getTop())) {
                SwipeActionsLayout.this.postInvalidateOnAnimation();
            }

            updateState(settleLeft);
        }
    }
}