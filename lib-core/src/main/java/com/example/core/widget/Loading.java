package com.example.core.widget;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.example.core.R;
import com.example.core.base.BaseDialogFragment;
import com.example.core.utils.Utils;

public class Loading extends BaseDialogFragment {

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
        int width = (int) (Utils.getApp().getApplicationContext().getResources().getDisplayMetrics().widthPixels * ratio);
        widthPx(width).heightPx(width);
    }

}