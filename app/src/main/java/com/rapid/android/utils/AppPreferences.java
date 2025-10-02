package com.rapid.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.core.common.app.BaseApplication;

public final class AppPreferences {

    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_THEME_MODE = "pref_theme_mode";

    private AppPreferences() {
    }

    private static SharedPreferences getPrefs() {
        Context context = BaseApplication.getAppContext();
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getThemeModeValue() {
        return getPrefs().getInt(KEY_THEME_MODE, 0);
    }

    public static void setThemeModeValue(int mode) {
        getPrefs().edit().putInt(KEY_THEME_MODE, mode).apply();
    }
}
