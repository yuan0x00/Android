package com.example.core.utils.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.*;
import androidx.core.content.ContextCompat;

import com.example.core.CoreApp;

/**
 * 资源工具类（安全、便捷获取各类资源）
 * 所有方法均使用 Application Context，避免内存泄漏
 */
public final class ResUtils {

    private ResUtils() {
        throw new UnsupportedOperationException("ResUtils cannot be instantiated");
    }

    /**
     * 获取 Application Context（推荐所有资源获取使用此 Context）
     */
    @NonNull
    private static Context getAppContext() {
        return CoreApp.getAppContext();
    }

    /**
     * 获取 Resources 对象
     */
    @NonNull
    private static Resources getResources() {
        return getAppContext().getResources();
    }

    // —————— 字符串 ——————

    /**
     * 获取字符串（支持格式化参数）
     */
    @NonNull
    public static String getString(@StringRes int resId, @Nullable Object... formatArgs) {
        try {
            if (formatArgs != null && formatArgs.length > 0) {
                return getAppContext().getString(resId, formatArgs);
            } else {
                return getAppContext().getString(resId);
            }
        } catch (Exception e) {
            return "res_string_" + resId;
        }
    }

    // —————— 颜色 ——————

    /**
     * 获取颜色值（ARGB int）
     */
    @ColorInt
    public static int getColor(@ColorRes int resId) {
        try {
            return ContextCompat.getColor(getAppContext(), resId);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取 ColorStateList（用于 TextView、Button 等支持状态的颜色）
     */
    @Nullable
    public static ColorStateList getColorStateList(@ColorRes int resId) {
        try {
            return ContextCompat.getColorStateList(getAppContext(), resId);
        } catch (Exception e) {
            return null;
        }
    }

    // —————— 尺寸 ——————

    /**
     * 获取尺寸（返回 px）
     */
    @Dimension
    public static int getDimensionPixelSize(@Dimension int resId) {
        try {
            return getResources().getDimensionPixelSize(resId);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取尺寸（返回 float px）
     */
    public static float getDimension(@Dimension int resId) {
        try {
            return getResources().getDimension(resId);
        } catch (Exception e) {
            return 0f;
        }
    }

    /**
     * 获取 dp 值转 px
     */
    public static int dp2px(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    /**
     * 获取 sp 值转 px
     */
    public static int sp2px(float sp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                getResources().getDisplayMetrics()
        );
    }

    // —————— Drawable ——————

    /**
     * 获取 Drawable
     */
    @Nullable
    public static Drawable getDrawable(@DrawableRes int resId) {
        try {
            return ContextCompat.getDrawable(getAppContext(), resId);
        } catch (Exception e) {
            return null;
        }
    }

    // —————— 数组 ——————

    /**
     * 获取字符串数组
     */
    @NonNull
    public static String[] getStringArray(@ArrayRes int resId) {
        try {
            return getAppContext().getResources().getStringArray(resId);
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * 获取整型数组
     */
    @NonNull
    public static int[] getIntArray(@ArrayRes int resId) {
        try {
            return getAppContext().getResources().getIntArray(resId);
        } catch (Exception e) {
            return new int[0];
        }
    }

    // —————— 布尔值 ——————

    /**
     * 获取布尔值
     */
    public static boolean getBoolean(int resId) {
        try {
            return getAppContext().getResources().getBoolean(resId);
        } catch (Exception e) {
            return false;
        }
    }

    // —————— 通用资源 ID 转值 ——————

    /**
     * 根据资源名和类型获取资源 ID（如 "ic_launcher", "drawable"）
     */
    public static int getResourceId(@NonNull String resourceName, @NonNull String resourceType) {
        try {
            return getAppContext().getResources()
                    .getIdentifier(resourceName, resourceType, getAppContext().getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 根据资源名获取字符串资源 ID
     */
    public static int getStringId(@NonNull String resourceName) {
        return getResourceId(resourceName, "string");
    }

    /**
     * 根据资源名获取颜色资源 ID
     */
    public static int getColorId(@NonNull String resourceName) {
        return getResourceId(resourceName, "color");
    }

    /**
     * 根据资源名获取 Drawable 资源 ID
     */
    public static int getDrawableId(@NonNull String resourceName) {
        return getResourceId(resourceName, "drawable");
    }

    /**
     * 根据资源名获取尺寸资源 ID
     */
    public static int getDimenId(@NonNull String resourceName) {
        return getResourceId(resourceName, "dimen");
    }

    // —————— 主题属性 ——————

    /**
     * 获取当前主题中某个属性的资源 ID
     */
    public static int getThemeAttrId(int attrResId) {
        TypedValue outValue = new TypedValue();
        getAppContext().getTheme().resolveAttribute(attrResId, outValue, true);
        return outValue.resourceId;
    }

    /**
     * 获取当前主题中某个颜色属性值
     */
    @ColorInt
    public static int getThemeColor(int attrResId) {
        TypedValue outValue = new TypedValue();
        getAppContext().getTheme().resolveAttribute(attrResId, outValue, true);
        return outValue.data; // 颜色值直接存在 data 中
    }

    /**
     * 获取当前主题中某个 Drawable 属性
     */
    @Nullable
    public static Drawable getThemeDrawable(int attrResId) {
        TypedValue outValue = new TypedValue();
        getAppContext().getTheme().resolveAttribute(attrResId, outValue, true);
        if (outValue.resourceId != 0) {
            return getDrawable(outValue.resourceId);
        }
        return null;
    }
}