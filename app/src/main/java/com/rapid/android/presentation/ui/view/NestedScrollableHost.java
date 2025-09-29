package com.rapid.android.presentation.ui.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

/**
 * 用于包裹 ViewPager2 布局。
 * 解决 ViewPager2 页面中嵌套与 ViewPager2 滑动方向相同的可滚动元素的问题。
 */
public class NestedScrollableHost extends FrameLayout {

    private int touchSlop = 0;
    private float initialX = 0.0f;
    private float initialY = 0.0f;

    public NestedScrollableHost(@NonNull Context context) {
        super(context);
        this.init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    private ViewPager2 parentViewPager() {
        View decorView = null;
        Context context = getContext();
        if (context instanceof Activity) {
            decorView = ((Activity) context).getWindow().getDecorView();
        }
        View v = (View) this.getParent();
        while (v != null && !(v instanceof ViewPager2) && v != decorView) {
            v = (View) v.getParent();
        }
        return (v instanceof ViewPager2) ? (ViewPager2) v : null;
    }

    private void init() {
        this.touchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
    }

    /**
     * 在指定坐标 (x, y) 处查找最深层的视图。
     */
    private View findViewAt(float x, float y, ViewGroup parent) {
        if (!isPointInsideView(x, y, parent)) {
            return null;
        }

        // 从上到下遍历子视图，优先选择最顶层的视图
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) {
                continue;
            }

            // 将坐标转换为子视图的相对坐标
            float translatedX = x - child.getX();
            float translatedY = y - child.getY();

            if (isPointInsideView(translatedX, translatedY, child)) {
                if (child instanceof ViewGroup) {
                    // 递归查找更深层的视图
                    View deeperView = findViewAt(translatedX, translatedY, (ViewGroup) child);
                    if (deeperView != null) {
                        return deeperView;
                    }
                }
                return child; // 如果没有更深层的视图，返回当前子视图
            }
        }
        return parent; // 如果没有子视图包含该点，返回父视图
    }

    /**
     * 检查坐标 (x, y) 是否在视图的范围内。
     */
    private boolean isPointInsideView(float x, float y, View view) {
        return x >= 0 && x < view.getWidth() && y >= 0 && y < view.getHeight();
    }

    /**
     * 检查触摸点下的视图及其父视图（直到本布局）是否可以沿指定方向滚动。
     */
    private boolean canAnyViewScroll(int orientation, float delta, float x, float y) {
        View targetView = findViewAt(x, y, this);
        if (targetView == null) {
            return false;
        }

        // 从触摸点视图向上遍历到本布局
        View current = targetView;
        int direction = (int) (Math.signum(-delta));

        while (current != this && current != null) {
            if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (current.canScrollHorizontally(direction)) {
                    return true;
                }
            } else if (orientation == ViewPager2.ORIENTATION_VERTICAL) {
                if (current.canScrollVertically(direction)) {
                    return true;
                }
            }
            current = (View) current.getParent();
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleInterceptTouchEvent(MotionEvent ev) {
        ViewPager2 vp = parentViewPager();
        if (vp == null) {
            return;
        }

        int orientation = vp.getOrientation();

        // 如果没有视图可以沿父 ViewPager2 的方向滚动，提前返回
        if (!canAnyViewScroll(orientation, -1.0f, ev.getX(), ev.getY()) &&
                !canAnyViewScroll(orientation, 1.0f, ev.getX(), ev.getY())) {
            return;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            this.initialX = ev.getX();
            this.initialY = ev.getY();
            // 默认让子视图处理触摸，直到手势方向明确
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = ev.getX() - this.initialX;
            float dy = ev.getY() - this.initialY;
            boolean isVpHorizontal = (orientation == ViewPager2.ORIENTATION_HORIZONTAL);

            // 缩放位移以匹配 ViewPager2 的触摸阈值（假设为子视图的 2 倍）
            float scaleDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1.0f);
            float scaleDy = Math.abs(dy) * (isVpHorizontal ? 1.0f : 0.5f);

            if (scaleDx > this.touchSlop || scaleDy > this.touchSlop) {
                if (isVpHorizontal) {
                    // ViewPager2 为水平方向
                    if (scaleDx > scaleDy * 1.5f) {
                        // 明确的水平手势
                        // 有视图可以水平滚动，交给子视图处理
                        // 无视图可以水平滚动，交给 ViewPager2 处理
                        getParent().requestDisallowInterceptTouchEvent(canAnyViewScroll(orientation, dx, ev.getX(), ev.getY()));
                    } else {
                        // 非明确水平手势（垂直或模糊方向），交给子视图处理
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    // ViewPager2 为垂直方向
                    if (scaleDy > scaleDx * 1.5f) {
                        // 明确的垂直手势
                        // 有视图可以垂直滚动，交给子视图处理
                        // 无视图可以垂直滚动，交给 ViewPager2 处理
                        getParent().requestDisallowInterceptTouchEvent(canAnyViewScroll(orientation, dy, ev.getX(), ev.getY()));
                    } else {
                        // 非明确垂直手势（水平或模糊方向），交给子视图处理
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
        }
    }
}