package com.core.ui.components.loading;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.core.ui.R;
import com.core.ui.presentation.BaseDialogFragment;

public class LoadingDialog extends BaseDialogFragment {

    @Override
    public void showSafely(@NonNull FragmentManager fm) {
        this.setSquareWidthOf(0.25F);
        this.cancelable(false);
        super.showSafely(fm);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_loading;
    }

    /**
     * 设置宽高为屏幕宽度的百分比
     */
    public void setSquareWidthOf(float ratio) {
        int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * ratio);
        widthPx(width).heightPx(width);
    }

}
