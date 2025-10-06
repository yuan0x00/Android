package com.rapid.android.ui.common.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.core.ui.components.dialog.BaseDialogView;
import com.rapid.android.R;

public class TipDialogView extends BaseDialogView {

    public TipDialogView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_tip;
    }
}
