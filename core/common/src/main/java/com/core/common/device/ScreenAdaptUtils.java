package com.core.common.device;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.core.common.BuildConfig;
import com.core.common.log.Logger;

/**
 * 屏幕适配工具类（今日头条方案）
 * 以宽度为基准进行等比缩放，适配不同屏幕
 */
public class ScreenAdaptUtils {

    // 设计稿基准宽度（单位 dp，如 360、375、411）
    private static final float DESIGN_WIDTH_DP = 360f;

    // 是否适配平板（默认 false，平板使用原始尺寸）
    private static boolean sAdaptTablet = false;

    // 是否包含状态栏高度在适配计算中（默认 false）
    private static boolean sIncludeStatusBar = false;

    /**
     * 初始化屏幕适配（在 Application.onCreate 中调用）
     */
    public static void init(Application application) {
        adaptScreen(application);
    }

    /**
     * 设置是否适配平板（默认不适配）
     */
    public static void setAdaptTablet(boolean adaptTablet) {
        sAdaptTablet = adaptTablet;
    }

    /**
     * 设置是否包含状态栏高度参与适配计算（默认不包含）
     */
    public static void setIncludeStatusBar(boolean includeStatusBar) {
        sIncludeStatusBar = includeStatusBar;
    }

    /**
     * 执行屏幕适配
     */
    private static void adaptScreen(Application application) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        int screenWidthPx = displayMetrics.widthPixels;
        int screenHeightPx = displayMetrics.heightPixels;

        // 判断是否为平板（宽度 ≥ 600dp）
        if (!sAdaptTablet && isTablet(application)) {
            return; // 不适配平板
        }

        // 可选：高度减去状态栏（如设计稿不含状态栏）
        if (sIncludeStatusBar) {
            int statusBarHeight = getStatusBarHeight(application);
            screenHeightPx -= statusBarHeight;
        }

        // 计算目标 density
        float targetDensity = screenWidthPx / DESIGN_WIDTH_DP;
        float targetDensityDpi = targetDensity * 160;
        float targetScaledDensity = targetDensity * (displayMetrics.scaledDensity / displayMetrics.density);

        // 修改系统 DisplayMetrics
        DisplayMetrics dm = application.getResources().getDisplayMetrics();
        dm.density = targetDensity;
        dm.densityDpi = (int) targetDensityDpi;
        dm.scaledDensity = targetScaledDensity;

        // 同时修改 configuration（兼容部分 ROM）
        if (BuildConfig.DEBUG) {
            Logger.d("ScreenAdapt", "适配完成: density=" + dm.density + ", width=" + dm.widthPixels + "px");
        }
    }

    /**
     * 判断是否为平板设备
     */
    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK)
                >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 获取状态栏高度
     */
    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
