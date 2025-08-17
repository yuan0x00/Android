package com.example.core.utils;

import android.app.Application;
import android.content.Context;

public class Utils {
    private static Application sAppContext;

    /**
     * 初始化一次，传入任意 Context，提取 Application
     */
    public static void init(Context context) {
        if (sAppContext == null) {
            sAppContext = (Application) context.getApplicationContext();
        }
    }

    /**
     * 安全获取 Application
     */
    public static Application getApp() {
        if (sAppContext == null) {
            throw new IllegalStateException("AppUtils未初始化，请先调用init(context)");
        }
        return sAppContext;
    }
}
