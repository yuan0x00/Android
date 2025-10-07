package com.rapid.android.ui.common;

import android.text.TextUtils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rapid.android.core.common.utils.ToastUtils;

public final class UiFeedback {

    private UiFeedback() {
    }

    public static void observeError(LifecycleOwner owner, LiveData<String> source) {
        if (source == null) {
            return;
        }
        source.observe(owner, msg -> {
            if (!TextUtils.isEmpty(msg)) {
                ToastUtils.showLongToast(msg);
            }
        });
    }
}
