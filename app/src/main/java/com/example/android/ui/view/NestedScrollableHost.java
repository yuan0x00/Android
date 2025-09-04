package com.example.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;


/**
 * Layout to wrap a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the same direction as
 * ViewPager2. The scrollable element needs to be the immediate and only child of this host layout.
 *
 * This solution has limitations when using multiple levels of nested scrollable elements
 * (e.g. a horizontal RecyclerView in a vertical RecyclerView in a horizontal ViewPager2).
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
        View v = (View)this.getParent();
        while( v != null && !(v instanceof ViewPager2) )
            v = (View)v.getParent();
        return (ViewPager2)v;
    }

    private View child() { return (this.getChildCount() > 0 ? this.getChildAt(0) : null); }

    private void init() {
        this.touchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
    }

    private boolean canChildScroll(int orientation, Float delta) {
        int direction = (int)(Math.signum(-delta));
        View child = this.child();

        if( child == null )
            return false;

        if( orientation == 0 )
            return child.canScrollHorizontally(direction);
        if( orientation == 1 )
            return child.canScrollVertically(direction);

        return false;
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private void handleInterceptTouchEvent(MotionEvent ev) {
        ViewPager2 vp = this.parentViewPager();
        if( vp == null )
            return;

        int orientation = vp.getOrientation();

        // Early return if child can't scroll in same direction as parent
        if( !this.canChildScroll(orientation, -1.0f) && !this.canChildScroll(orientation, 1.0f) )
            return;

        if( ev.getAction() == MotionEvent.ACTION_DOWN ) {
            this.initialX = ev.getX();
            this.initialY = ev.getY();
            // 默认先让子视图处理，等待手势明确后再决定
            this.getParent().requestDisallowInterceptTouchEvent(true);
        }
        else if( ev.getAction() == MotionEvent.ACTION_MOVE ) {
            float dx = ev.getX() - this.initialX;
            float dy = ev.getY() - this.initialY;
            boolean isVpHorizontal = (orientation == ViewPager2.ORIENTATION_HORIZONTAL);

            // assuming ViewPager2 touch-slop is 2x touch-slop of child
            float scaleDx = Math.abs(dx) * (isVpHorizontal ? 0.5f : 1.0f);
            float scaleDy = Math.abs(dy) * (isVpHorizontal ? 1.0f : 0.5f);

            if( scaleDx > this.touchSlop || scaleDy > this.touchSlop ) {
                // 基于手势趋势判断：只有明确的水平滑动才交给ViewPager2
                if( isVpHorizontal ) {
                    // ViewPager2是水平的
                    if( scaleDx > scaleDy * 1.5f ) {
                        // 明确的水平滑动手势，检查子视图是否可以处理
                        if( this.canChildScroll(orientation, dx) ) {
                            // 子视图可以水平滑动，继续让子视图处理
                            this.getParent().requestDisallowInterceptTouchEvent(true);
                        } else {
                            // 子视图不能水平滑动，交给ViewPager2处理
                            this.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    } else {
                        // 非明确的水平滑动（垂直滑动或模糊方向），让子视图处理
                        this.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    // ViewPager2是垂直的
                    if( scaleDy > scaleDx * 1.5f ) {
                        // 明确的垂直滑动手势，检查子视图是否可以处理
                        if( this.canChildScroll(orientation, dy) ) {
                            // 子视图可以垂直滑动，继续让子视图处理
                            this.getParent().requestDisallowInterceptTouchEvent(true);
                        } else {
                            // 子视图不能垂直滑动，交给ViewPager2处理
                            this.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    } else {
                        // 非明确的垂直滑动（水平滑动或模糊方向），让子视图处理
                        this.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
        }
    }
}