package com.rapid.android.core.ui.components.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.ui.R;

public class ToastDialogView extends BaseDialogView {

    private static final long ENTER_DURATION = 300L;
    private static final long EXIT_DURATION = 250L;
    private static final long POSITION_ADJUST_DURATION = 200L;
    private static final float TOAST_HEIGHT_DP = 68f; // Toast高度，根据实际布局调整
    private static final float DEFAULT_POSITION_DP = 0f; // 默认位置偏移（距离底部）

    private View toastRoot;
    private TextView messageView;
    private float toastHeightPx;

    public ToastDialogView(@NonNull Context context) {
        super(context);
        init();
    }

    public ToastDialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ToastDialogView(@NonNull Context context,
                           @Nullable AttributeSet attrs,
                           int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setDimEnabled(false);
        setDialogCancelable(false);
        setAnimStyle(0);
        setConsumeOutsideTouch(false);
        toastHeightPx = dpToPx(TOAST_HEIGHT_DP);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.core_ui_toast_view;
    }

    @Override
    protected void onDialogContentCreated(@NonNull View view) {
        toastRoot = view;
        messageView = view.findViewById(R.id.core_ui_toast_message);

        // 新的Toast初始位置设为默认位置
        resetToDefaultPosition();
    }

    public void setMessage(@NonNull CharSequence message) {
        messageView.setText(message);
    }

    /**
     * 应用位置：新的在默认位置，旧的上移一个位置
     */
    public void applyPosition(int index, int totalCount) {
        if (toastRoot == null) {
            return;
        }

        float targetPosition;
        if (index == totalCount - 1) {
            // 最新的Toast在默认位置
            targetPosition = dpToPx(DEFAULT_POSITION_DP);
        } else {
            // 旧的Toast上移，越旧的Toast位置越高
            int offsetIndex = totalCount - 1 - index;
            targetPosition = dpToPx(DEFAULT_POSITION_DP) - offsetIndex * toastHeightPx;
        }

        // 使用动画平滑移动到目标位置
        toastRoot.animate()
                .translationY(targetPosition)
                .setDuration(POSITION_ADJUST_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * 重置到默认位置（屏幕底部）
     */
    private void resetToDefaultPosition() {
        if (toastRoot != null) {
            toastRoot.setTranslationY(dpToPx(DEFAULT_POSITION_DP));
        }
    }

    @Override
    public void playEnterAnimation() {
        if (toastRoot == null) {
            super.playEnterAnimation();
            return;
        }

        // 初始状态：透明且稍微缩小
        toastRoot.setAlpha(0f);
        toastRoot.setScaleX(0.8f);
        toastRoot.setScaleY(0.8f);
        toastRoot.setTranslationY(toastRoot.getTranslationY() + dpToPx(20f)); // 从下方一点点位置开始

        // 入场动画：淡入、放大、上移一点点
        toastRoot.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(toastRoot.getTranslationY() - dpToPx(20f))
                .setDuration(ENTER_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    public void playExitAnimation(@NonNull Runnable onAnimationEnd) {
        if (toastRoot == null) {
            super.playExitAnimation(onAnimationEnd);
            return;
        }

        // 退场动画：淡出、缩小、上移一点点
        toastRoot.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationY(toastRoot.getTranslationY() - dpToPx(10f))
                .setDuration(EXIT_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(onAnimationEnd)
                .start();
    }

    /**
     * 快速退出动画（当被新Toast挤上去时使用）
     */
    public void playQuickExitAnimation(@NonNull Runnable onAnimationEnd) {
        if (toastRoot == null) {
            onAnimationEnd.run();
            return;
        }

        // 快速退场：只淡出，不移位
        toastRoot.animate()
                .alpha(0f)
                .setDuration(EXIT_DURATION / 2)
                .withEndAction(onAnimationEnd)
                .start();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}