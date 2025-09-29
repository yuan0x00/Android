package com.rapid.android.presentation.ui.view;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 通用轮播基类，支持无限循环、自动轮播、触摸暂停、圆点指示器
 * 子类需实现 bindItem 和 getItemLayoutRes
 */
public abstract class BaseBanner<T> extends FrameLayout {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    protected ViewPager2 mViewPager;
    protected List<T> mDataList = new ArrayList<>();
    private Runnable mAutoScrollRunnable;

    // 轮播间隔
    private long mInterval = 3000;

    // 是否正在用户滑动
    private boolean mIsUserScrolling = false;

    // 指示器相关
    private LinearLayout mIndicatorContainer;
    private int mIndicatorSelectedColor = 0xFFCCCCCC;  // 深灰
    private int mIndicatorNormalColor = 0xFF333333;    // 浅灰
    private int mIndicatorRadius = 4;                 // dp
    private int mIndicatorMargin = 6;                 // dp

    public BaseBanner(Context context) {
        this(context, null);
    }

    public BaseBanner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseBanner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 初始化 ViewPager2
        mViewPager = new ViewPager2(context);
        LayoutParams vpParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mViewPager.setLayoutParams(vpParams);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mViewPager.setOffscreenPageLimit(1);

        addView(mViewPager);

        // 设置适配器
        mViewPager.setAdapter(new BannerAdapter());

        // 触摸暂停 + 滑动后重置轮播 + 更新指示器
        setupPageChangeCallback();

        // 指示器
        setupIndicator(context);

        // 自动轮播任务
        mAutoScrollRunnable = () -> {
            if (!mIsUserScrolling && mDataList.size() > 1) {
                int next = mViewPager.getCurrentItem() + 1;
                mViewPager.setCurrentItem(next, true);
            }
            mHandler.postDelayed(mAutoScrollRunnable, mInterval);
        };
    }

    private void setupPageChangeCallback() {
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                mIsUserScrolling = (state == ViewPager2.SCROLL_STATE_DRAGGING);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 用户操作后，重置轮播计时
                startAutoScroll();
                // 更新指示器
                updateIndicator();
            }
        });
    }

    private void setupIndicator(Context context) {
        mIndicatorContainer = new LinearLayout(context);
        mIndicatorContainer.setOrientation(LinearLayout.HORIZONTAL);
        mIndicatorContainer.setGravity(Gravity.CENTER);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dp2px(16);
        mIndicatorContainer.setLayoutParams(params);

        addView(mIndicatorContainer);
    }

    // 设置数据
    public void setData(List<T> data) {
        if (data == null || data.isEmpty()) return;
        mDataList.clear();
        mDataList.addAll(data);
        Objects.requireNonNull(mViewPager.getAdapter()).notifyDataSetChanged();

        // 重置到中间位置实现“无限循环”
        if (data.size() > 1) {
            int start = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % data.size());
            mViewPager.setCurrentItem(start, false);
        }

        // 创建或更新指示器
        createIndicators();

        // 启动自动轮播
        startAutoScroll();
    }

    // 创建指示器圆点
    private void createIndicators() {
        if (mIndicatorContainer == null || mDataList.isEmpty()) return;

        mIndicatorContainer.removeAllViews();

        for (int i = 0; i < mDataList.size(); i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp2px(mIndicatorRadius * 2),
                    dp2px(mIndicatorRadius * 2)
            );
            params.setMargins(dp2px(mIndicatorMargin), 0, dp2px(mIndicatorMargin), 0);
            dot.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(i == getCurrentRealPosition() ? mIndicatorSelectedColor : mIndicatorNormalColor);
            dot.setBackground(drawable);

            mIndicatorContainer.addView(dot);
        }
    }

    // 更新指示器状态
    private void updateIndicator() {
        if (mIndicatorContainer == null || mIndicatorContainer.getChildCount() == 0) return;
        if (mDataList.size() <= 1) return;

        int currentPosition = getCurrentRealPosition();

        for (int i = 0; i < mIndicatorContainer.getChildCount(); i++) {
            View dot = mIndicatorContainer.getChildAt(i);
            GradientDrawable drawable = (GradientDrawable) dot.getBackground();
            drawable.setColor(i == currentPosition ? mIndicatorSelectedColor : mIndicatorNormalColor);
        }
    }

    // 获取当前真实位置 (0 ~ size-1)
    private int getCurrentRealPosition() {
        if (mDataList.isEmpty()) return 0;
        return mViewPager.getCurrentItem() % mDataList.size();
    }

    // 开始轮播
    public void startAutoScroll() {
        mHandler.removeCallbacks(mAutoScrollRunnable);
        mHandler.postDelayed(mAutoScrollRunnable, mInterval);
    }

    // 停止轮播（建议在 Fragment 的 onPause 中调用）
    public void stopAutoScroll() {
        mHandler.removeCallbacks(mAutoScrollRunnable);
    }

    // 设置轮播间隔
    public void setInterval(long interval) {
        this.mInterval = interval;
    }

    // 是否允许用户滑动
    public void setUserInputEnabled(boolean enabled) {
        mViewPager.setUserInputEnabled(enabled);
    }

    // 设置指示器颜色
    public void setIndicatorColors(int selectedColor, int normalColor) {
        this.mIndicatorSelectedColor = selectedColor;
        this.mIndicatorNormalColor = normalColor;
        createIndicators();
    }

    // 设置指示器圆点半径（dp）
    public void setIndicatorRadius(int radiusDp) {
        this.mIndicatorRadius = radiusDp;
        createIndicators();
    }

    // 设置指示器间距（dp）
    public void setIndicatorMargin(int marginDp) {
        this.mIndicatorMargin = marginDp;
        createIndicators();
    }

    // 子类必须实现：绑定数据到 itemView
    protected abstract void bindItem(View itemView, T data, int position);

    // 子类必须实现：返回 item 布局资源 ID
    protected abstract int getItemLayoutRes();

    // dp 转 px
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    // -------------------------
    // 内存安全：在销毁时停止轮播
    // -------------------------
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScroll();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDataList.size() > 1) {
            startAutoScroll();
        }
    }

    // 内部 ViewHolder
    static class BannerViewHolder extends RecyclerView.ViewHolder {
        public BannerViewHolder(View itemView) {
            super(itemView);
        }
    }

    // 内部适配器
    private class BannerAdapter extends RecyclerView.Adapter<BannerViewHolder> {
        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(getItemLayoutRes(), parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            int realPosition = position % mDataList.size();
            T data = mDataList.get(realPosition);
            bindItem(holder.itemView, data, realPosition);
        }

        @Override
        public int getItemCount() {
            return mDataList.size() > 1 ? Integer.MAX_VALUE : mDataList.size();
        }
    }
}