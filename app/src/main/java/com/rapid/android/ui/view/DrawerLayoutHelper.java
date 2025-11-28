package com.rapid.android.ui.view;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;

public class DrawerLayoutHelper {

    private static final String TAG = "DrawerLayoutHelper";

    /**
     * 禁用系统手势边缘更新机制（sEdgeSizeUsingSystemGestureInsets），防止 onLayout 重置 edgeSize
     * 仅在 API 29+ 上生效
     */
    private static void disableSystemGestureEdgeUpdate() {
        if (Build.VERSION.SDK_INT < 29) {
            Log.d(TAG, "API < 29, no need to disable system gesture edge update");
            return;
        }
        try {
            Field staticField = DrawerLayout.class.getDeclaredField("sEdgeSizeUsingSystemGestureInsets");
            staticField.setAccessible(true);
            staticField.setBoolean(null, false);  // 设为 false，绕过 final
            Log.d(TAG, "Disabled sEdgeSizeUsingSystemGestureInsets successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable system gesture edge update", e);
        }
    }

    /**
     * 通过反射的方式将 DrawerLayout 的侧滑范围设为指定百分比的全屏宽度
     * 改进：禁用系统重置 + 空 mPeekRunnable
     *
     * @param activity               Activity 实例，用于获取屏幕尺寸
     * @param drawerLayout           DrawerLayout 实例
     * @param displayWidthPercentage 宽度百分比（1.0f 为全屏）
     */
    public static void setDrawerLeftEdgeSize(final Activity activity, final DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) {
            return;
        }

        // 延迟到布局完成后执行（解决初始化时机 + 确保禁用生效）
        drawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // 步骤1: 禁用系统手势重置（全局生效）
                disableSystemGestureEdgeUpdate();

                // 步骤2: 修改 edgeSize
                try {
                    Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
                    leftDraggerField.setAccessible(true);
                    ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);

                    if (leftDragger != null) {
                        Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
                        edgeSizeField.setAccessible(true);
                        int edgeSize = edgeSizeField.getInt(leftDragger);

                        Point displaySize = new Point();
                        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                        int newEdgeSize = Math.max(edgeSize, (int) (displaySize.x * displayWidthPercentage));

                        edgeSizeField.setInt(leftDragger, newEdgeSize);
                        Log.d(TAG, "Set left edge size to: " + newEdgeSize);
                    }

                    // 步骤3: 空实现 mPeekRunnable，防长按 peek
                    Field leftCallbackField = drawerLayout.getClass().getDeclaredField("mLeftCallback");
                    leftCallbackField.setAccessible(true);
                    ViewDragHelper.Callback leftCallback = (ViewDragHelper.Callback) leftCallbackField.get(drawerLayout);

                    if (leftCallback != null) {
                        Field peekRunnableField = leftCallback.getClass().getDeclaredField("mPeekRunnable");
                        peekRunnableField.setAccessible(true);
                        Runnable nullRunnable = new Runnable() {
                            @Override
                            public void run() {
                                // 空实现
                            }
                        };
                        peekRunnableField.set(leftCallback, nullRunnable);
                        Log.d(TAG, "Set empty mPeekRunnable");
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Failed to set drawer edge size", e);
                }
            }
        });
    }

    public static void setDrawerLeftEdgeSizeWithContentPush(
            final Activity activity,
            final DrawerLayout drawerLayout,
            final float displayWidthPercentage) {

        setDrawerLeftEdgeSize(activity, drawerLayout, displayWidthPercentage);

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                View content = drawerLayout.getChildAt(0);
                if (content != null) {
                    float move = drawerView.getWidth() * slideOffset;
                    content.setTranslationX(move);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                View content = drawerLayout.getChildAt(0);
                if (content != null) {
                    content.setTranslationX(0);
                }
            }
        });

    }

}