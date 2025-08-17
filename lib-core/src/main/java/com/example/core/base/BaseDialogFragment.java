package com.example.core.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.core.R;
import com.example.core.widget.DimDialogFragment;

/**
 * BaseDialogFragment - 支持链式调用，并可带遮罩显示
 */
public abstract class BaseDialogFragment extends DialogFragment {

    protected View mContentView;

    private int mWidthDp = -1;
    private int mHeightDp = -1;
    private int mWidthPx = WindowManager.LayoutParams.WRAP_CONTENT;
    private int mHeightPx = WindowManager.LayoutParams.WRAP_CONTENT;
    private int mGravity = Gravity.CENTER;
    private int mAnimStyle = 0;
    private boolean mCancelable = true;

    // 是否由 showWithDim 管理（用于 dismiss 时联动）
    private boolean mManagedByDim = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(mCancelable);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getLayoutId();
        if (layoutId == 0) {
            throw new IllegalArgumentException("getLayoutId() must return a valid layout resource ID.");
        }
        mContentView = inflater.inflate(layoutId, container, false);
        return mContentView;
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

    // ============= 内部方法 =============

    private void applyWindowAttributes() {
        if (getDialog() == null || getDialog().getWindow() == null) return;

        Window window = getDialog().getWindow();
        window.setGravity(mGravity);

        if (mAnimStyle != 0) {
            window.setWindowAnimations(mAnimStyle);
        }

        WindowManager.LayoutParams params = window.getAttributes();

        final float density = requireContext().getResources().getDisplayMetrics().density;

        if (mWidthDp >= 0) {
            params.width = (int) (mWidthDp * density + 0.5f);
        } else {
            params.width = mWidthPx;
        }

        if (mHeightDp >= 0) {
            params.height = (int) (mHeightDp * density + 0.5f);
        } else {
            params.height = mHeightPx;
        }

        window.setAttributes(params);
        getDialog().setCanceledOnTouchOutside(mCancelable);
    }

    // ============= 显示与关闭 =============

    /**
     * 显示 Dialog 并自动添加遮罩层
     * 遮罩点击可关闭当前 Dialog
     */
    public void show(@NonNull FragmentManager fm) {
        if (fm.isDestroyed() || fm.isStateSaved()) return;

        // 先显示遮罩
        DimDialogFragment dimDialog = new DimDialogFragment();
        dimDialog.show(fm, DimDialogFragment.TAG);

        // 标记由 dim 管理
        mManagedByDim = true;

        // 显示内容
        showWithDim(fm);
    }

    public void showWithDim(@NonNull FragmentManager fm) {
        if (fm.isDestroyed() || fm.isStateSaved()) return;
        if (!isAdded()) {
            try {
                show(fm, getClass().getSimpleName());
            } catch (IllegalStateException e) {
                // ignore
            }
        }
    }

    public void dismissSafely() {
        if (isAdded() && getDialog() != null) {
            onDismiss(getDialog());
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // 如果是由 showWithDim 显示的，尝试关闭遮罩
        if (mManagedByDim) {
            try {
                DimDialogFragment dimDialog = (DimDialogFragment) getFragmentManager()
                        .findFragmentByTag(DimDialogFragment.TAG);
                if (dimDialog != null && !dimDialog.isRemoving()) {
                    dimDialog.dismissAllowingStateLoss();
                }
            } catch (Exception e) {
                // ignore
            }
        }
    }
}