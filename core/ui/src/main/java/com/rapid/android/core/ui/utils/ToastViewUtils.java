package com.rapid.android.core.ui.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.rapid.android.core.ui.components.dialog.DialogController;
import com.rapid.android.core.ui.components.dialog.DialogEffect;

public final class ToastViewUtils {

    private static final long LONG_DURATION_MILLIS = 3500L;
    private static final int MAX_COUNT = 2;

    private ToastViewUtils() {
    }

    public static void showShortToast(@Nullable DialogController controller, @Nullable CharSequence message) {
        if (controller == null || TextUtils.isEmpty(message)) {
            return;
        }
        controller.show(new DialogEffect.Toast(message));
    }

    public static void showLongToast(@Nullable DialogController controller, @Nullable CharSequence message) {
        if (controller == null || TextUtils.isEmpty(message)) {
            return;
        }
        controller.show(new DialogEffect.Toast(message, LONG_DURATION_MILLIS, MAX_COUNT));
    }
}
