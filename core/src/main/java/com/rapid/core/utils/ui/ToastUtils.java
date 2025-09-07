package com.rapid.core.utils.ui;

import android.content.Context;
import android.widget.Toast;

import com.rapid.core.CoreApp;

public class ToastUtils {

    private static Toast sToast;

    public static void showShortToast(String msg) {
        show(msg, Toast.LENGTH_SHORT);
    }

    public static void showLongToast(String msg) {
        show(msg, Toast.LENGTH_LONG);
    }

    private static void show(String msg, int duration) {
        // 使用全局 Context，避免内存泄漏
        Context context = CoreApp.getAppContext();

        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(context, msg, duration);
        sToast.show();
    }
}
