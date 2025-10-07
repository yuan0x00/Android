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

    private static final long ENTER_DURATION = 180L;
    private static final long EXIT_DURATION = 150L;
    private static final float OFFSET_DP = 68f;
    private static final float ENTRY_OFFSET_DP = 24f;

    private View toastRoot;
    private TextView messageView;

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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.core_ui_toast_view;
    }

    @Override
    protected void onDialogContentCreated(@NonNull View view) {
        toastRoot = view;
        messageView = view.findViewById(R.id.core_ui_toast_message);
    }

    public void setMessage(@NonNull CharSequence message) {
        messageView.setText(message);
    }

    public void applyPosition(int index, int totalCount) {
        if (toastRoot == null) {
            return;
        }
        float spacing = dpToPx(OFFSET_DP);
        float centerOffset = (totalCount - 1) * 0.5f * spacing;
        float target = index * spacing - centerOffset;
        toastRoot.animate()
                .translationY(target)
                .setDuration(160L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    @Override
    public void playEnterAnimation() {
        if (toastRoot == null) {
            super.playEnterAnimation();
            return;
        }
        toastRoot.clearAnimation();
        toastRoot.setAlpha(0f);
        toastRoot.setScaleX(0.92f);
        toastRoot.setScaleY(0.92f);
        toastRoot.setTranslationY(toastRoot.getTranslationY() + dpToPx(ENTRY_OFFSET_DP));
        toastRoot.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationYBy(-dpToPx(ENTRY_OFFSET_DP))
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
        toastRoot.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(EXIT_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(onAnimationEnd)
                .start();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
