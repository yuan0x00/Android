package com.example.core.utils;

import android.widget.Toast;

public class ToastUtils {

    public static void showShortToast(String text) {
        Toast.makeText(Utils.getApp().getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(String text) {
        Toast.makeText(Utils.getApp().getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}
