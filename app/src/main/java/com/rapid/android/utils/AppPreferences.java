package com.rapid.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.core.common.app.BaseApplication;

public final class AppPreferences {

    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_THEME_MODE = "pref_theme_mode";
    private static final String KEY_DATA_SAVER = "pref_data_saver";
    private static final String KEY_WIFI_ONLY_MEDIA = "pref_wifi_only_media";
    private static final String KEY_NOTIFICATIONS = "pref_notifications";

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

    public static boolean isDataSaverEnabled() {
        return getPrefs().getBoolean(KEY_DATA_SAVER, false);
    }

    public static void setDataSaverEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_DATA_SAVER, enabled).apply();
    }

    public static boolean isWifiOnlyMediaEnabled() {
        return getPrefs().getBoolean(KEY_WIFI_ONLY_MEDIA, false);
    }

    public static void setWifiOnlyMediaEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_WIFI_ONLY_MEDIA, enabled).apply();
    }

    public static boolean isNotificationsEnabled() {
        return getPrefs().getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsEnabled(boolean enabled) {
        getPrefs().edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
}
