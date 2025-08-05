package com.example.reandroid.utils;

import android.widget.Toast;

public class ToastUtils {

    public static void showShortToast(String text) {
        Toast.makeText(AppUtils.getAppContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(String text) {
        Toast.makeText(AppUtils.getAppContext(), text, Toast.LENGTH_LONG).show();
    }
}
