package com.core.ui.components.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.core.ui.R;

final class LoadingDialogView extends BaseDialogView {

    LoadingDialogView(@NonNull Context context) {
        super(context);
        setDialogCancelable(false);
        setDimAmount(0.4f);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_loading;
    }
}
