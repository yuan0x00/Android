package com.core.ui.presentation;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.core.ui.R;

public abstract class BaseDialogFragment extends DialogFragment {

    protected View mContentView;

    private int mWidthDp = -1;
    private int mHeightDp = -1;
    private int mWidthPx = WindowManager.LayoutParams.WRAP_CONTENT;
    private int mHeightPx = WindowManager.LayoutParams.WRAP_CONTENT;
    private int mGravity = Gravity.CENTER;
    private int mAnimStyle = 0;
    private boolean mCancelable = true;

    private boolean mAutoDim = true;   // 是否自动添加 dim 层


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(mCancelable);
        setStyle(STYLE_NO_FRAME, R.style.BaseDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        int layoutId = getLayoutId();
        if (layoutId == 0) {
            throw new IllegalArgumentException("getLayoutId() must return a valid layout resource ID.");
        }

        // 包一层根容器，内部含 dim 背景和内容
        // 内部容器（含 dim 层 + 内容）
        FrameLayout mRootContainer = new FrameLayout(requireContext());

        if (mAutoDim) {
            View dimView = new View(requireContext());
            dimView.setBackgroundColor(Color.parseColor("#99000000"));
            FrameLayout.LayoutParams dimParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            if (mCancelable) {
                dimView.setOnClickListener(v -> dismissAllowingStateLoss());
            }
            mRootContainer.addView(dimView, dimParams);
        }

        mContentView = inflater.inflate(layoutId, mRootContainer, false);
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        contentParams.gravity = Gravity.CENTER;
        mRootContainer.addView(mContentView, contentParams);

        return mRootContainer;
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

    // ============= 链式调用 API =============

    public BaseDialogFragment width(int dp) {
        this.mWidthDp = dp;
        return this;
    }

    public BaseDialogFragment height(int dp) {
        this.mHeightDp = dp;
        return this;
    }

    public BaseDialogFragment size(int widthDp, int heightDp) {
        this.mWidthDp = widthDp;
        this.mHeightDp = heightDp;
        return this;
    }

    public BaseDialogFragment widthPx(int px) {
        this.mWidthPx = px;
        return this;
    }

    public BaseDialogFragment heightPx(int px) {
        this.mHeightPx = px;
        return this;
    }

    public BaseDialogFragment gravity(int gravity) {
        this.mGravity = gravity;
        return this;
    }

    public BaseDialogFragment animStyle(int animStyle) {
        this.mAnimStyle = animStyle;
        return this;
    }

    public BaseDialogFragment cancelable(boolean cancelable) {
        this.mCancelable = cancelable;
        setCancelable(cancelable);
        return this;
    }

    public BaseDialogFragment autoDim(boolean enable) {
        this.mAutoDim = enable;
        return this;
    }

    // ============= 内部方法 =============

    private void applyWindowAttributes() {
        if (getDialog() == null || getDialog().getWindow() == null) return;

        Window window = getDialog().getWindow();

        if (mAnimStyle != 0) {
            window.setWindowAnimations(mAnimStyle);
        }

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setWindowAnimations(R.style.FadeWindowAnimation);

        getDialog().setCanceledOnTouchOutside(mCancelable);
    }

    // ============= 显示与关闭 =============

    public void showSafely(@NonNull FragmentManager fm) {
        if (fm.isDestroyed() || fm.isStateSaved()) return;
        if (!isAdded()) {
            try {
                show(fm, getClass().getSimpleName());
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public void dismissSafely() {
        try {
            dismissAllowingStateLoss();
        } catch (Exception ignored) {
        }
    }
}
