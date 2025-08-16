package com.example.reandroid.ui.dialog;

import android.app.Dialog;
import android.view.Gravity;
import android.view.MotionEvent;

import com.example.core.base.BaseDialogFragment;
import com.example.reandroid.R;

public class TipDialogFragment extends BaseDialogFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_tip;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // 设置点击遮罩关闭
            dialog.findViewById(R.id.tvContent).setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new TipDialogFragment()
                            .size(300, 300)
                            .gravity(Gravity.CENTER)
                            .cancelable(true)
                            .cancelableOutside(true)
                            .show(getParentFragmentManager());
                }
                return false;
            });
        }
    }
}