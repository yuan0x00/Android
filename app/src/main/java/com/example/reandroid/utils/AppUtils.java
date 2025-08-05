package com.example.reandroid.utils;

import android.content.Context;

public class AppUtils {
    private static Context sAppContext;

    /**
     * 初始化一次，传入任意 Context（如 Activity），提取 ApplicationContext
     */
    public static void init(Context context) {
        if (sAppContext == null) {
            sAppContext = context.getApplicationContext();
        }
    }

    /**
     * 安全获取 ApplicationContext
     */
    public static Context getAppContext() {
        if (sAppContext == null) {
            throw new IllegalStateException("AppUtils未初始化，请先调用init(context)");
        }
        return sAppContext;
    }
}
