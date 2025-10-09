package com.rapid.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.rapid.android.core.common.app.BaseApplication;

public final class AppPreferences {

    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_THEME_MODE = "pref_theme_mode";
    private static final String KEY_NOTIFICATIONS = "pref_notifications";
    private static final String KEY_HOME_TOP_ENABLED = "pref_home_top_enabled";
    private static final String KEY_NO_IMAGE_MODE = "pref_no_image_mode";

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

    public static boolean isNotificationsEnabled() {
        return getPrefs().getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public static boolean isHomeTopEnabled() {
        return getPrefs().getBoolean(KEY_HOME_TOP_ENABLED, true);
    }

    public static void setHomeTopEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_HOME_TOP_ENABLED, enabled).apply();
    }

    public static boolean isNoImageModeEnabled() {
        return getPrefs().getBoolean(KEY_NO_IMAGE_MODE, false);
    }

    public static void setNoImageModeEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_NO_IMAGE_MODE, enabled).apply();
    }
}
