package com.rapid.android.ui.common;

import android.text.TextUtils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.utils.ToastViewUtils;

public final class UiFeedback {

    private UiFeedback() {
    }

    public static void observeError(LifecycleOwner owner, DialogController dialogController, LiveData<String> source) {
        if (source == null) {
            return;
        }
        source.observe(owner, msg -> {
            if (!TextUtils.isEmpty(msg)) {
                ToastViewUtils.showLongToast(dialogController, msg);
            }
        });
    }
}
