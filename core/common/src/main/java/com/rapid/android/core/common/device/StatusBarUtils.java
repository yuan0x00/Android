package com.rapid.android.core.common.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏工具类（沉浸式、图标深色/浅色、高度获取、刘海屏适配）
 * 支持 API 21+
 */
public final class StatusBarUtils {

    private StatusBarUtils() {
        throw new UnsupportedOperationException("StatusBarUtils cannot be instantiated");
    }

    // —————— 设置状态栏背景色 ——————

    /**
     * 设置状态栏颜色（沉浸式）
     * @param activity Activity
     * @param color 颜色值（如 Color.BLUE, 0xFF000000）
     */
    public static void setStatusBarColor(@NonNull Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    // —————— 设置状态栏图标深色/浅色 ——————

    /**
     * 设置状态栏图标为深色（适用于浅色背景）
     * @param activity Activity
     * @param dark true=深色图标（黑字），false=浅色图标（白字）
     */
    public static void setStatusBarIconDark(@NonNull Activity activity, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = activity.getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (dark) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(flags);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0~6.0 无官方深色图标支持，可尝试 MIUI / Flyme 适配（见下方）
            if (dark) {
                setMIUIStatusBarDarkIcon(activity, true);
                setFlymeStatusBarDarkIcon(activity, true);
            } else {
                setMIUIStatusBarDarkIcon(activity, false);
                setFlymeStatusBarDarkIcon(activity, false);
            }
        }
    }

    // —————— MIUI 专属深色状态栏图标（小米手机） ——————

    private static void setMIUIStatusBarDarkIcon(@NonNull Activity activity, boolean dark) {
        try {
            Class<?> decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
            Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
            field.setAccessible(true);
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            field.setInt(decorView, Color.TRANSPARENT);

            Class<? extends Window> clazz = activity.getWindow().getClass();
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field fieldFlag = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = fieldFlag.getInt(layoutParams);

            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), dark ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception ignored) {
            // 忽略异常，非小米设备或版本不支持
        }
    }

    // —————— Flyme 专属深色状态栏图标（魅族手机） ——————

    private static void setFlymeStatusBarDarkIcon(@NonNull Activity activity, boolean dark) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
        } catch (Exception ignored) {
            // 忽略异常，非魅族设备或版本不支持
        }
    }

    // —————— 获取状态栏高度 ——————

    /**
     * 获取状态栏高度（单位：px）
     */
    public static int getStatusBarHeight(@NonNull Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return dp2px(context, 24); // 默认 24dp
    }

    private static int dp2px(@NonNull Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    // —————— 设置全屏沉浸（隐藏状态栏） ——————

    /**
     * 隐藏状态栏（全屏模式）
     */
    public static void hideStatusBar(@NonNull Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(flags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9+ 隐藏刘海区域
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            activity.getWindow().setAttributes(lp);
        }
    }

    /**
     * 显示状态栏
     */
    public static void showStatusBar(@NonNull Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(flags);
    }

    // —————— 适配刘海屏/挖孔屏 ——————

    /**
     * 设置允许内容延伸到刘海区域（短边）
     * 适用于视频播放、游戏等全屏场景
     */
    @SuppressLint("NewApi")
    public static void allowDisplayCutout(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            activity.getWindow().setAttributes(lp);
        }
    }

    /**
     * 禁止内容延伸到刘海区域（默认行为）
     */
    @SuppressLint("NewApi")
    public static void forbidDisplayCutout(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            activity.getWindow().setAttributes(lp);
        }
    }

    // —————— 判断是否为刘海屏 ——————

    /**
     * 判断设备是否为刘海屏（Android 9+ 官方 API）
     */
    @SuppressLint("NewApi")
    public static boolean hasDisplayCutout(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            View decorView = activity.getWindow().getDecorView();
            WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(decorView);
            if (windowInsets != null) {
                return windowInsets.getDisplayCutout() != null;
            }
        }
        return false;
    }

    // —————— 工具方法：设置透明状态栏（常用于 CoordinatorLayout / DrawerLayout） ——————

    /**
     * 设置透明状态栏（内容延伸到状态栏下方）
     */
    public static void setTransparentStatusBar(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}