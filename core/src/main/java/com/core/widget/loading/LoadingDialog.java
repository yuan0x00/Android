package com.core.widget.loading;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.core.CoreApp;
import com.core.R;
import com.core.base.ui.BaseDialogFragment;

public class LoadingDialog extends BaseDialogFragment {

    @Override
    public void show(@NonNull FragmentManager fm) {
        this.setSquareWidthOf(0.25F);
        this.cancelable(false);
        super.show(fm);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_loading;
    }

    /**
     * 设置宽高为屏幕宽度的百分比
     */
    public void setSquareWidthOf(float ratio) {
        int width = (int) (CoreApp.getAppContext().getResources().getDisplayMetrics().widthPixels * ratio);
        widthPx(width).heightPx(width);
    }

}