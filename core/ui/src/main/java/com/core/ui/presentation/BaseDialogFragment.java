package com.core.ui.presentation;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.core.ui.R;

/**
 * 自定义弹窗基类：负责尺寸、动画、背景遮罩等属性的应用，真正的显示/关闭交由 DialogController 管理。
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private static final int INVALID = -1;
    private static final float DEFAULT_DIM_AMOUNT = 0.6f;

    protected View contentView;

    private int widthDp = INVALID;
    private int heightDp = INVALID;
    private int widthPx = WindowManager.LayoutParams.WRAP_CONTENT;
    private int heightPx = WindowManager.LayoutParams.WRAP_CONTENT;
    private int gravity = Gravity.CENTER;
    private int animStyle = 0;
    private boolean dialogCancelable = true;
    private boolean dimEnabled = true;
    private float dimAmount = DEFAULT_DIM_AMOUNT;

    @Nullable
    private OnDismissListener onDismissListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(dialogCancelable);
        setStyle(STYLE_NO_FRAME, R.style.BaseDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int layoutId = getLayoutId();
        if (layoutId == 0) {
            throw new IllegalArgumentException("getLayoutId() must return a valid layout resource ID.");
        }
        contentView = inflater.inflate(layoutId, container, false);
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        applyWindowAttributes();
    }

    @LayoutRes
    protected abstract int getLayoutId();

    protected void initListener() {
        // 子类可重写
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initListener();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    public void setWidthDp(int dp) {
        this.widthDp = dp;
        this.widthPx = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    public void setHeightDp(int dp) {
        this.heightDp = dp;
        this.heightPx = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    public void setSizeDp(int widthDp, int heightDp) {
        this.widthDp = widthDp;
        this.heightDp = heightDp;
        this.widthPx = WindowManager.LayoutParams.WRAP_CONTENT;
        this.heightPx = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    public void setWidthPx(int px) {
        this.widthPx = px;
        this.widthDp = INVALID;
    }

    public void setHeightPx(int px) {
        this.heightPx = px;
        this.heightDp = INVALID;
    }

    public void setGravity(int dialogGravity) {
        this.gravity = dialogGravity;
    }

    public void setAnimStyle(int styleRes) {
        this.animStyle = styleRes;
    }

    public void setDialogCancelable(boolean cancelable) {
        this.dialogCancelable = cancelable;
        setCancelable(cancelable);
    }

    public void setDimEnabled(boolean enable) {
        this.dimEnabled = enable;
    }

    public void setDimAmount(float amount) {
        this.dimAmount = amount;
        this.dimEnabled = true;
    }

    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public void showSafely(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed() || fragmentManager.isStateSaved()) {
            return;
        }
        if (!isAdded()) {
            try {
                show(fragmentManager, getClass().getSimpleName());
            } catch (IllegalStateException ignored) {
                // ignore illegal state when host lifecycle is already finishing
            }
        }
    }

    public void dismissSafely() {
        try {
            dismissAllowingStateLoss();
        } catch (Exception ignored) {
            // ignore unexpected exceptions from fragment manager state
        }
    }

    private void applyWindowAttributes() {
        if (getDialog() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }

        if (animStyle != 0) {
            window.setWindowAnimations(animStyle);
        } else {
            window.setWindowAnimations(R.style.FadeWindowAnimation);
        }

        int targetWidth = resolveSize(widthPx, widthDp);
        int targetHeight = resolveSize(heightPx, heightDp);
        window.setLayout(targetWidth, targetHeight);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (dimEnabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(dimAmount);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0f);
        }

        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = gravity;
        window.setAttributes(params);

        getDialog().setCanceledOnTouchOutside(dialogCancelable);
    }

    private int resolveSize(int pixelValue, int dpValue) {
        if (dpValue > 0 && getContext() != null) {
            float density = getContext().getResources().getDisplayMetrics().density;
            return (int) (dpValue * density + 0.5f);
        }
        return pixelValue;
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
