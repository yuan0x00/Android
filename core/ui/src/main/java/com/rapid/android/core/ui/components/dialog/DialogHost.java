package com.rapid.android.core.ui.components.dialog;

import androidx.annotation.NonNull;

/**
 * 提供 {@link DialogController} 访问能力的宿主接口，方便在 Activity、Fragment 等组件之间共享
 * 自定义对话框和 Toast 能力。
 */
public interface DialogHost {

    @NonNull
    DialogController getDialogController();
}
