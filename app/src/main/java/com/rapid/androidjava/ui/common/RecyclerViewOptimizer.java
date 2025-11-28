package com.rapid.android.ui.common;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * RecyclerView 性能优化工具类
 * 统一管理 RecyclerView 的性能优化配置
 */
public final class RecyclerViewOptimizer {

    private RecyclerViewOptimizer() {
        // 工具类，禁止实例化
    }

    /**
     * 应用标准性能优化配置
     *
     * @param recyclerView 需要优化的 RecyclerView
     */
    public static void applyOptimizations(RecyclerView recyclerView) {
        applyOptimizations(recyclerView, true);
    }

    /**
     * 应用性能优化配置
     *
     * @param recyclerView            需要优化的 RecyclerView
     * @param disableChangeAnimations 是否禁用 item change 动画
     */
    public static void applyOptimizations(RecyclerView recyclerView, boolean disableChangeAnimations) {
        if (recyclerView == null) {
            return;
        }

        // 设置固定大小，避免每次数据变化都重新测量
        recyclerView.setHasFixedSize(true);

        // 增加缓存大小，减少 ViewHolder 创建次数
        recyclerView.setItemViewCacheSize(20);

        // 禁用 change 动画以提升性能（可选）
        if (disableChangeAnimations) {
            RecyclerView.ItemAnimator itemAnimator = recyclerView.getItemAnimator();
            if (itemAnimator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) itemAnimator).setSupportsChangeAnimations(false);
            }
        }

        // 启用绘制缓存（对于复杂布局效果明显）
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(android.view.View.DRAWING_CACHE_QUALITY_HIGH);
    }

    /**
     * 为需要嵌套滚动的 RecyclerView 应用优化
     *
     * @param recyclerView 需要优化的 RecyclerView
     */
    public static void applyNestedScrollOptimizations(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }

        // 基础优化
        applyOptimizations(recyclerView);

        // 禁用嵌套滚动，提升性能
        recyclerView.setNestedScrollingEnabled(false);
    }
}
