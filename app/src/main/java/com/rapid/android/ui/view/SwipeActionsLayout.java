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

/**
 * SwipeActionsLayout 是一个支持左右滑动操作菜单的布局容器。
 * 它包含一个主内容视图和可选的左侧/右侧操作菜单视图。
 */
public class SwipeActionsLayout extends ViewGroup {

    // 状态常量定义
    private static final int STATE_CLOSED = 0;
    private static final int STATE_LEFT_OPEN = 1;
    private static final int STATE_RIGHT_OPEN = 2;

    // 滑动方向常量定义
    private static final int DIRECTION_NONE = 0; // 未确定方向
    private static final int DIRECTION_RIGHT = 1; // 向右滑动，开启左菜单 (left > 0)
    private static final int DIRECTION_LEFT = -1; // 向左滑动，开启右菜单 (left < 0)

    // 视图组件
    private View mainContent;
    private View leftMenu;
    private View rightMenu;

    // 内部状态
    private int currentState = STATE_CLOSED;
    private ViewDragHelper dragHelper;
    private OnSwipeActionsListener listener;

    // 滑动配置
    private float swipeThreshold = 0.5f; // 滑动阈值（用于判断是否打开/关闭）
    private int mInitialSwipeDirection = DIRECTION_NONE; // 本次手势的初始滑动方向锁定

    // 外部监听器转发
    private OnClickListener externalClickListener;
    private OnLongClickListener externalLongClickListener;

    /**
     * 构造方法
     */
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

    /**
     * 初始化 ViewDragHelper。
     */
    private void init() {
        // 创建 ViewDragHelper，设置灵敏度为 1.0f
        dragHelper = ViewDragHelper.create(this, 1.0f, new DragCallback());
    }

    /**
     * 外部设置的点击监听器，转发给主内容视图。
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        this.externalClickListener = l;
        if (mainContent != null) {
            mainContent.setOnClickListener(l);
        }
    }

    /**
     * 外部设置的长按监听器，转发给主内容视图。
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        this.externalLongClickListener = l;
        if (mainContent != null) {
            mainContent.setOnLongClickListener(l);
        }
    }

    /**
     * 附加到窗口时处理 android:onClick 属性。
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mainContent != null && getContext() instanceof View.OnClickListener) {
            mainContent.setOnClickListener((OnClickListener) getContext());
        }
    }

    /**
     * 视图填充完成时，确定各个子视图的角色。
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // 假设子视图顺序为：0: mainContent, 1: leftMenu, 2: rightMenu
        List<View> children = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            children.add(getChildAt(i));
        }

        if (!children.isEmpty()) mainContent = children.get(0);
        if (children.size() > 1) leftMenu = children.get(1);
        if (children.size() > 2) rightMenu = children.get(2);

        // 重新应用外部设置的监听器
        if (externalClickListener != null && mainContent != null) {
            mainContent.setOnClickListener(externalClickListener);
        }
        if (externalLongClickListener != null && mainContent != null) {
            mainContent.setOnLongClickListener(externalLongClickListener);
        }

        // 确保主内容可点击和可聚焦，以便接收事件
        if (mainContent != null) {
            mainContent.setClickable(true);
            mainContent.setFocusable(true);
        }
    }

    /**
     * 测量子视图尺寸。
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);

        // 1. 测量主内容，它决定整体高度
        if (mainContent != null) {
            measureChild(mainContent, widthMeasureSpec, heightMeasureSpec);
        }

        int contentHeight = mainContent != null ?
                mainContent.getMeasuredHeight() : MeasureSpec.getSize(heightMeasureSpec);

        // 2. 测量左右菜单（高度与主内容相同，宽度最多为父容器的一半）
        int menuHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
        int menuWidthSpec = MeasureSpec.makeMeasureSpec(width / 2, MeasureSpec.AT_MOST);

        if (leftMenu != null) {
            measureChild(leftMenu, menuWidthSpec, menuHeightSpec);
        }

        if (rightMenu != null) {
            measureChild(rightMenu, menuWidthSpec, menuHeightSpec);
        }

        // 3. 设置最终尺寸
        setMeasuredDimension(width, contentHeight);
    }

    /**
     * 布局子视图位置。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        // 1. 布局左侧菜单（隐藏在左边）
        if (leftMenu != null) {
            int leftWidth = leftMenu.getMeasuredWidth();
            leftMenu.layout(-leftWidth, 0, 0, height);
        }

        // 2. 布局主内容（覆盖整个区域，初始位置为 0）
        if (mainContent != null) {
            mainContent.layout(0, 0, width, height);
        }

        // 3. 布局右侧菜单（隐藏在右边）
        if (rightMenu != null) {
            int rightWidth = rightMenu.getMeasuredWidth();
            rightMenu.layout(width, 0, width + rightWidth, height);
        }
    }

    /**
     * 拦截触摸事件，交给 ViewDragHelper 判断是否开始拖拽。
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * 处理触摸事件，交给 ViewDragHelper 处理拖拽。
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 在拖拽结束后，处理 ViewDragHelper 的自动结算动画。
     */
    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            invalidate(); // 继续重绘以驱动动画
        }
    }

    /**
     * 更新当前状态并通知监听器。
     *
     * @param left 主内容视图的当前左边距
     */
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

    /**
     * 通知外部监听器状态发生变化。
     */
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

    // --- 公共 API ---

    /**
     * 通过动画打开左侧菜单。
     */
    public void openLeftMenu() {
        if (leftMenu != null) {
            int left = leftMenu.getWidth();
            if (dragHelper.smoothSlideViewTo(mainContent, left, mainContent.getTop())) {
                this.postInvalidateOnAnimation();
            }
            updateState(left);
        }
    }

    /**
     * 通过动画打开右侧菜单。
     */
    public void openRightMenu() {
        if (rightMenu != null) {
            int left = -rightMenu.getWidth();
            if (dragHelper.smoothSlideViewTo(mainContent, left, mainContent.getTop())) {
                this.postInvalidateOnAnimation();
            }
            updateState(left);
        }
    }

    /**
     * 通过动画关闭所有菜单。
     */
    public void closeMenu() {
        if (dragHelper.smoothSlideViewTo(mainContent, 0, mainContent.getTop())) {
            this.postInvalidateOnAnimation();
        }
        updateState(0);
    }

    /**
     * 检查当前是否有菜单打开。
     *
     * @return 如果菜单已打开则返回 true
     */
    public boolean isMenuOpen() {
        return currentState != STATE_CLOSED;
    }

    /**
     * 设置滑动阈值。
     *
     * @param threshold 0.1f 到 1.0f 之间的浮点数
     */
    public void setSwipeThreshold(float threshold) {
        this.swipeThreshold = Math.max(0.1f, Math.min(threshold, 1.0f));
    }

    /**
     * 设置滑动操作监听器。
     */
    public void setOnSwipeActionsListener(OnSwipeActionsListener listener) {
        this.listener = listener;
    }

    /**
     * 滑动操作监听器接口。
     */
    public interface OnSwipeActionsListener {
        void onLeftMenuOpened();

        void onRightMenuOpened();

        void onClosed();

        void onLeftMenuAction(int actionId);

        void onRightMenuAction(int actionId);
    }

    /**
     * ViewDragHelper 的回调类，处理拖拽逻辑。
     */
    private class DragCallback extends ViewDragHelper.Callback {

        /**
         * 尝试捕获主内容视图进行拖拽。
         */
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            // 只有在关闭状态下才重置方向锁定，允许新的拖拽
            if (currentState == STATE_CLOSED) {
                mInitialSwipeDirection = DIRECTION_NONE;
            }
            return child == mainContent && isEnabled();
        }

        /**
         * 定义可拖拽的水平范围。
         */
        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            int leftRange = leftMenu != null ? leftMenu.getWidth() : 0;
            int rightRange = rightMenu != null ? rightMenu.getWidth() : 0;
            return Math.max(leftRange, rightRange);
        }

        /**
         * 限制主内容视图的水平拖拽位置。
         */
        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            int leftMenuWidth = leftMenu != null ? leftMenu.getWidth() : 0;
            int rightMenuWidth = rightMenu != null ? rightMenu.getWidth() : 0;

            // 1. 如果菜单已打开，只允许往关闭方向滑动
            if (currentState == STATE_LEFT_OPEN) {
                return Math.max(0, Math.min(left, leftMenuWidth));
            }
            if (currentState == STATE_RIGHT_OPEN) {
                return Math.max(-rightMenuWidth, Math.min(left, 0));
            }

            // 2. 如果处于关闭状态 (currentState == STATE_CLOSED)：实现方向锁定

            // A. 首次滑动：确定方向
            if (mInitialSwipeDirection == DIRECTION_NONE) {
                if (left > 0) {
                    mInitialSwipeDirection = DIRECTION_RIGHT; // 首次向右滑
                } else if (left < 0) {
                    mInitialSwipeDirection = DIRECTION_LEFT;  // 首次向左滑
                }
            }

            // B. 根据确定方向限制滑动范围
            return switch (mInitialSwipeDirection) {
                case DIRECTION_RIGHT ->
                    // 锁定为向右滑动：只允许 left >= 0
                        Math.max(0, Math.min(left, leftMenuWidth));
                case DIRECTION_LEFT ->
                    // 锁定为向左滑动：只允许 left <= 0
                        Math.max(-rightMenuWidth, Math.min(left, 0));
                default ->
                    // 初始位置或方向未确定：允许全范围
                        Math.max(-rightMenuWidth, Math.min(left, leftMenuWidth));
            };
        }

        /**
         * 视图位置改变时调用，用于同步移动菜单视图。
         */
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            // 菜单视图相对于主内容视图保持静止，因此它们需要抵消主内容的移动量 dx
            if (leftMenu != null) {
                leftMenu.offsetLeftAndRight(dx);
            }
            if (rightMenu != null) {
                rightMenu.offsetLeftAndRight(dx);
            }
            invalidate();
        }

        /**
         * 视图被释放（松手）时调用，用于自动结算位置。
         */
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            int left = releasedChild.getLeft();
            int leftMenuWidth = leftMenu != null ? leftMenu.getWidth() : 0;
            int rightMenuWidth = rightMenu != null ? rightMenu.getWidth() : 0;
            int settleLeft; // 最终结算位置

            // 最小甩动速度阈值
            int minFlingVelocity = 400;

            if (currentState == STATE_LEFT_OPEN) {
                // 左侧打开时：判断是关闭还是保持打开
                if (xvel < -minFlingVelocity || left < leftMenuWidth * (1 - swipeThreshold)) {
                    settleLeft = 0; // 关闭
                } else {
                    settleLeft = leftMenuWidth; // 保持打开
                }
            } else if (currentState == STATE_RIGHT_OPEN) {
                // 右侧打开时：判断是关闭还是保持打开
                if (xvel > minFlingVelocity || left > -rightMenuWidth * (1 - swipeThreshold)) {
                    settleLeft = 0; // 关闭
                } else {
                    settleLeft = -rightMenuWidth; // 保持打开
                }
            } else {
                // 关闭状态：判断是打开左菜单、打开右菜单还是保持关闭
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
                        settleLeft = 0; // 保持关闭
                    }
                }
            }

            // 启动自动结算动画
            if (dragHelper.settleCapturedViewAt(settleLeft, releasedChild.getTop())) {
                SwipeActionsLayout.this.postInvalidateOnAnimation();
            }

            updateState(settleLeft);

            // 手势结束，清除方向锁定，允许下次拖拽重新选择方向
            mInitialSwipeDirection = DIRECTION_NONE;
        }
    }
}