package com.rapid.android.core.common.device;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.common.BuildConfig;
import com.rapid.android.core.log.LogKit;

/**
 * 屏幕适配工具类（今日头条方案的 Activity 级改造版本）
 * 以宽度为基准进行等比缩放，适配不同屏幕，避免直接修改全局 DisplayMetrics。
 */
public class ScreenAdaptUtils {

    // 设计稿基准宽度（单位 dp，如 360、375、411）
    private static final float DESIGN_WIDTH_DP = 360f;
    private static final String TAG = "ScreenAdapt";

    // 是否适配平板（默认 false，平板使用原始尺寸）
    private static boolean sAdaptTablet = false;

    // 是否包含状态栏高度在适配计算中（默认 false）
    private static boolean sIncludeStatusBar = false;

    private static float sOriginalDensity;
    private static float sOriginalScaledDensity;
    private static boolean sRegistered = false;

    /**
     * 初始化屏幕适配（在 Application.onCreate 中调用）
     */
    public static synchronized void init(@NonNull Application application) {
        if (sRegistered) {
            return;
        }

        final DisplayMetrics appMetrics = application.getResources().getDisplayMetrics();
        sOriginalDensity = appMetrics.density;
        sOriginalScaledDensity = appMetrics.scaledDensity;

        application.registerComponentCallbacks(new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(@NonNull Configuration newConfig) {
                if (newConfig.fontScale > 0) {
                    sOriginalScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                }
            }

            @Override
            public void onLowMemory() {
                // ignore
            }
        });

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                applyCustomDensity(application, activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                applyCustomDensity(application, activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                applyCustomDensity(application, activity);
            }

            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });

        applyCustomDensity(application, null);
        sRegistered = true;
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

    private static void applyCustomDensity(@NonNull Application application, @Nullable Activity activity) {
        if (!shouldAdapt(application, activity)) {
            restoreDensity(application, activity);
            return;
        }

        DisplayMetrics appMetrics = application.getResources().getDisplayMetrics();
        int targetPixels = appMetrics.widthPixels;
        if (sIncludeStatusBar) {
            targetPixels = Math.max(targetPixels - getStatusBarHeight(activity != null ? activity : application), 1);
        }

        float targetDensity = targetPixels / DESIGN_WIDTH_DP;
        float targetScaledDensity = targetDensity * (sOriginalScaledDensity / sOriginalDensity);
        int targetDensityDpi = (int) (targetDensity * 160);

        updateMetrics(appMetrics, targetDensity, targetScaledDensity, targetDensityDpi);
        if (activity != null) {
            updateMetrics(activity.getResources().getDisplayMetrics(), targetDensity, targetScaledDensity, targetDensityDpi);
        }

        if (BuildConfig.DEBUG) {
            LogKit.d(TAG, "apply density=%.2f dpi=%d targetPx=%d activity=%s",
                    targetDensity, targetDensityDpi, targetPixels,
                    activity != null ? activity.getClass().getSimpleName() : "application");
        }
    }

    private static void restoreDensity(@NonNull Application application, @Nullable Activity activity) {
        DisplayMetrics appMetrics = application.getResources().getDisplayMetrics();
        int originalDpi = (int) (sOriginalDensity * 160);
        updateMetrics(appMetrics, sOriginalDensity, sOriginalScaledDensity, originalDpi);
        if (activity != null) {
            updateMetrics(activity.getResources().getDisplayMetrics(), sOriginalDensity, sOriginalScaledDensity, originalDpi);
        }
    }

    private static void updateMetrics(@NonNull DisplayMetrics metrics,
                                      float density,
                                      float scaledDensity,
                                      int densityDpi) {
        metrics.density = density;
        metrics.scaledDensity = scaledDensity;
        metrics.densityDpi = densityDpi;
    }

    private static boolean shouldAdapt(@NonNull Application application, @Nullable Activity activity) {
        if (!sAdaptTablet && isTablet(application)) {
            return false;
        }
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode()) {
            return false;
        }
        return true;
    }

    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
