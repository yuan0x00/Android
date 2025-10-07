package com.rapid.android.core.ui.components.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.ui.R;

/**
 * 自定义 View 弹窗基类，负责内容布局加载、背景遮罩、尺寸与动画属性的管理。
 */
public abstract class BaseDialogView extends FrameLayout {

    private static final int INVALID = -1;
    private static final float DEFAULT_DIM_AMOUNT = 0.6f;

    private final View dimView;
    private final FrameLayout contentContainer;

    protected View contentView;

    private int widthDp = INVALID;
    private int heightDp = INVALID;
    private int widthPx = LayoutParams.WRAP_CONTENT;
    private int heightPx = LayoutParams.WRAP_CONTENT;
    private int gravity = Gravity.CENTER;

    private boolean dialogCancelable = true;
    private boolean dimEnabled = true;
    private float dimAmount = DEFAULT_DIM_AMOUNT;

    private int enterAnimationRes = R.anim.core_ui_fade_in;
    private int exitAnimationRes = R.anim.core_ui_fade_out;
    private boolean consumeOutsideTouch = true;

    @Nullable
    private OnDismissListener onDismissListener;
    @Nullable
    private OnDismissRequestListener onDismissRequestListener;

    public BaseDialogView(@NonNull Context context) {
        this(context, null);
    }

    public BaseDialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseDialogView(@NonNull Context context,
                          @Nullable AttributeSet attrs,
                          int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setClipToPadding(false);
        setClipChildren(false);
        applyOutsideTouchBehavior();

        dimView = new View(context);
        LayoutParams dimParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        dimView.setLayoutParams(dimParams);
        addView(dimView);

        contentContainer = new FrameLayout(context);
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(widthPx, heightPx);
        contentParams.gravity = gravity;
        contentContainer.setLayoutParams(contentParams);
        contentContainer.setClipToPadding(false);
        contentContainer.setClipChildren(false);
        addView(contentContainer);

        dimView.setOnClickListener(v -> {
            if (dialogCancelable) {
                requestDismiss();
            }
        });

        applyDimAttributes();
        inflateContent(LayoutInflater.from(context));
    }

    private void inflateContent(@NonNull LayoutInflater inflater) {
        int layoutId = getLayoutId();
        if (layoutId == 0) {
            throw new IllegalArgumentException("getLayoutId() must return a valid layout resource ID.");
        }
        contentView = inflater.inflate(layoutId, contentContainer, false);
        contentContainer.addView(contentView);
        onDialogContentCreated(contentView);
        initListener();
        applyContentLayoutParams();
    }

    private void applyContentLayoutParams() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
        params.width = resolveDimension(widthPx, widthDp);
        params.height = resolveDimension(heightPx, heightDp);
        params.gravity = gravity;
        contentContainer.setLayoutParams(params);
    }

    private int resolveDimension(int pixelValue, int dpValue) {
        if (dpValue > 0) {
            return dpToPx(dpValue);
        }
        return pixelValue;
    }

    private int dpToPx(int dpValue) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    protected void onDialogContentCreated(@NonNull View view) {
        // 子类可重写进行视图初始化
    }

    protected void initListener() {
        // 子类可重写
    }

    @LayoutRes
    protected abstract int getLayoutId();

    public void setWidthDp(int dp) {
        widthDp = dp;
        widthPx = LayoutParams.WRAP_CONTENT;
        applyContentLayoutParams();
    }

    public void setHeightDp(int dp) {
        heightDp = dp;
        heightPx = LayoutParams.WRAP_CONTENT;
        applyContentLayoutParams();
    }

    public void setSizeDp(int width, int height) {
        widthDp = width;
        heightDp = height;
        widthPx = LayoutParams.WRAP_CONTENT;
        heightPx = LayoutParams.WRAP_CONTENT;
        applyContentLayoutParams();
    }

    public void setWidthPx(int px) {
        widthPx = px;
        widthDp = INVALID;
        applyContentLayoutParams();
    }

    public void setHeightPx(int px) {
        heightPx = px;
        heightDp = INVALID;
        applyContentLayoutParams();
    }

    public void setGravity(int dialogGravity) {
        gravity = dialogGravity;
        applyContentLayoutParams();
    }

    public void setAnimStyle(int styleRes) {
        if (styleRes == 0) {
            enterAnimationRes = R.anim.core_ui_fade_in;
            exitAnimationRes = R.anim.core_ui_fade_out;
            return;
        }
        Context context = getContext();
        int[] attrs = new int[]{android.R.attr.windowEnterAnimation, android.R.attr.windowExitAnimation};
        TypedArray array = context.obtainStyledAttributes(styleRes, attrs);
        enterAnimationRes = array.getResourceId(0, R.anim.core_ui_fade_in);
        exitAnimationRes = array.getResourceId(1, R.anim.core_ui_fade_out);
        array.recycle();
    }

    public void setDialogCancelable(boolean cancelable) {
        dialogCancelable = cancelable;
    }

    public void setDimEnabled(boolean enable) {
        dimEnabled = enable;
        applyDimAttributes();
    }

    public void setDimAmount(float amount) {
        dimAmount = amount;
        dimEnabled = true;
        applyDimAttributes();
    }

    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        onDismissListener = listener;
    }

    public void setConsumeOutsideTouch(boolean consume) {
        consumeOutsideTouch = consume;
        applyOutsideTouchBehavior();
    }

    public void setOnDismissRequestListener(@Nullable OnDismissRequestListener listener) {
        onDismissRequestListener = listener;
    }

    public void playEnterAnimation() {
        if (enterAnimationRes == 0) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(getContext(), enterAnimationRes);
        startAnimation(animation);
    }

    public void playExitAnimation(@NonNull Runnable onAnimationEnd) {
        if (exitAnimationRes == 0) {
            onAnimationEnd.run();
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(getContext(), exitAnimationRes);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // no-op
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onAnimationEnd.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // no-op
            }
        });
        startAnimation(animation);
    }

    void notifyDismissed() {
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    private void requestDismiss() {
        if (onDismissRequestListener != null) {
            onDismissRequestListener.onDismissRequested(this);
        }
    }

    private void applyDimAttributes() {
        if (dimEnabled) {
            dimView.setVisibility(VISIBLE);
            int alpha = (int) (Math.max(0f, Math.min(dimAmount, 1f)) * 255);
            dimView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        } else {
            dimView.setVisibility(GONE);
        }
    }

    private void applyOutsideTouchBehavior() {
        setClickable(consumeOutsideTouch);
        setFocusable(consumeOutsideTouch);
        setFocusableInTouchMode(consumeOutsideTouch);
        if (!consumeOutsideTouch) {
            setOnTouchListener((v, event) -> false);
        } else {
            setOnTouchListener(null);
        }
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    interface OnDismissRequestListener {
        void onDismissRequested(@NonNull BaseDialogView view);
    }
}
