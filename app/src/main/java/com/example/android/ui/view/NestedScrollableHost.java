package com.example.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

public class NestedScrollableHost extends FrameLayout {

    private int touchSlop;
    private float initialX;
    private float initialY;

    public NestedScrollableHost(Context context) {
        super(context);
        init(context);
    }

    public NestedScrollableHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private ViewPager2 getParentViewPager() {
        View v = (View) getParent();
        while (v != null && !(v instanceof ViewPager2)) {
            v = (View) v.getParent();
        }
        return (v != null) ? (ViewPager2) v : null;
    }

    private View getChild() {
        return getChildCount() > 0 ? getChildAt(0) : null;
    }

    private boolean canChildScroll(int orientation, float delta, View view) {
        if (view == null) return false;

        int direction = (int) -Math.signum(delta);
        if (orientation == 0 && view.canScrollHorizontally(direction)) {
            return true;
        } else if (orientation == 1 && view.canScrollVertically(direction)) {
            return true;
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (canChildScroll(orientation, delta, child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        handleInterceptTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    private void handleInterceptTouchEvent(MotionEvent e) {
        ViewPager2 viewPager = getParentViewPager();
        if (viewPager == null) return;

        int orientation = viewPager.getOrientation();
        View child = getChild();
        if (child == null) return;

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = e.getX();
            initialY = e.getY();
            getParent().requestDisallowInterceptTouchEvent(true); // 初始允许子视图处理
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - initialX;
            float dy = e.getY() - initialY;
            boolean isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL;
            float scaledDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1f);
            float scaledDy = Math.abs(dy) * (isVpHorizontal ? 1f : 0.5f);

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // 垂直手势，允许父视图拦截
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // 水平手势，检查子视图是否可滚动
                    getParent().requestDisallowInterceptTouchEvent(canChildScroll(orientation, isVpHorizontal ? dx : dy, child));
                }
            }
        } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
            // 触摸结束时释放拦截状态
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }
}