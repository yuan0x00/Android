package com.example.core.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 安全区适配工具类（Safe Area Utils）
 * 支持链式调用，可为 View 设置 padding 或 margin 以避开状态栏、导航栏、刘海等区域。
 */
public final class SafeAreaUtils {

    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    private final View mView;
    private int mApplySides = 0;
    private boolean mUsePadding = true;
    private int mInsetType = WindowInsetsCompat.Type.systemBars();

    private SafeAreaUtils(View view) {
        this.mView = view;
    }

    /**
     * 开始对指定 View 进行安全区配置
     */
    public static SafeAreaUtils on(@NonNull View view) {
        return new SafeAreaUtils(view);
    }

    /**
     * 快速为根布局应用系统栏安全区（上 + 下）
     */
    public static void applySystemBars(@NonNull View view) {
        on(view).top().bottom().apply();
    }

    /**
     * 快速为全屏布局应用左右安全区（如横屏视频）
     */
    public static void applySides(@NonNull View view) {
        on(view).left().right().apply();
    }

    /**
     * 快速为底部控件（如 TabLayout、BottomNavigationView）应用底部安全区
     */
    public static void applyBottom(@NonNull View view) {
        on(view).bottom().usePadding().apply();
    }

    /**
     * 快速为顶部控件（如 Toolbar）应用顶部安全区
     */
    public static void applyTop(@NonNull View view) {
        on(view).top().usePadding().apply();
    }

    /**
     * 应用状态栏安全区（顶部）
     */
    public SafeAreaUtils top() {
        mApplySides |= TOP;
        return this;
    }

    /**
     * 应用导航栏安全区（底部）
     */
    public SafeAreaUtils bottom() {
        mApplySides |= BOTTOM;
        return this;
    }

    /**
     * 应用左侧安全区（如刘海、圆角）
     */
    public SafeAreaUtils left() {
        mApplySides |= LEFT;
        return this;
    }

    /**
     * 应用右侧安全区
     */
    public SafeAreaUtils right() {
        mApplySides |= RIGHT;
        return this;
    }

    /**
     * 同时应用所有方向的安全区
     */
    public SafeAreaUtils all() {
        mApplySides = TOP | BOTTOM | LEFT | RIGHT;
        return this;
    }

    /**
     * 使用 padding（默认）
     */
    public SafeAreaUtils usePadding() {
        mUsePadding = true;
        return this;
    }

    /**
     * 使用 margin
     */
    public SafeAreaUtils useMargin() {
        mUsePadding = false;
        return this;
    }

    /**
     * 设置 insets 类型（默认：systemBars）
     * 可组合：WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
     */
    public SafeAreaUtils insetType(int type) {
        mInsetType = type;
        return this;
    }

    /**
     * 快捷：仅系统栏（状态栏 + 导航栏）
     */
    public SafeAreaUtils systemBars() {
        return insetType(WindowInsetsCompat.Type.systemBars());
    }

    // ---------------- 便捷静态方法 ----------------

    /**
     * 快捷：包含刘海区
     */
    public SafeAreaUtils includeCutout() {
        return insetType(mInsetType | WindowInsetsCompat.Type.displayCutout());
    }

    /**
     * 快捷：包含手势区
     */
    public SafeAreaUtils includeGestures() {
        return insetType(mInsetType | WindowInsetsCompat.Type.mandatorySystemGestures());
    }

    /**
     * 应用配置
     */
    public void apply() {
        ViewCompat.setOnApplyWindowInsetsListener(mView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(mInsetType);

            Rect padding = new Rect(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;
                padding.set(marginLp.leftMargin, marginLp.topMargin, marginLp.rightMargin, marginLp.bottomMargin);
            }

            if ((mApplySides & TOP) != 0) padding.top += insets.top;
            if ((mApplySides & BOTTOM) != 0) padding.bottom += insets.bottom;
            if ((mApplySides & LEFT) != 0) padding.left += insets.left;
            if ((mApplySides & RIGHT) != 0) padding.right += insets.right;

            if (mUsePadding) {
                v.setPadding(padding.left, padding.top, padding.right, padding.bottom);
            } else if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).setMargins(padding.left, padding.top, padding.right, padding.bottom);
                v.setLayoutParams(lp);
            }

            return windowInsets;
        });
    }

    // 安全区方向
    @IntDef(flag = true, value = {
            TOP,
            BOTTOM,
            LEFT,
            RIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SafeAreaSide {
    }
}