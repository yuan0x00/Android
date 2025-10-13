package com.rapid.android.utils;

import android.content.Context;

import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.storage.PreferenceManager;

public final class AppPreferences {

    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_THEME_MODE = "pref_theme_mode";
    private static final String KEY_NOTIFICATIONS = "pref_notifications";
    private static final String KEY_NOTIFICATION_PREFERENCE_REQUESTED = "pref_notification_requested";
    private static final String KEY_NOTIFICATION_PERMISSION_DENIED_TIME = "pref_notification_denied_time";
    private static final String KEY_HOME_TOP_ENABLED = "pref_home_top_enabled";
    private static final String KEY_NO_IMAGE_MODE = "pref_no_image_mode";
    private static final String KEY_AUTO_HIDE_BOTTOM_BAR = "pref_auto_hide_bottom_bar";

    private static volatile PreferenceManager preferenceManager;

    private AppPreferences() {
    }

    private static PreferenceManager getPrefs() {
        if (preferenceManager == null) {
            synchronized (AppPreferences.class) {
                if (preferenceManager == null) {
                    Context context = BaseApplication.getAppContext();
                    preferenceManager = new PreferenceManager(context, PREF_NAME);
                }
            }
        }
        return preferenceManager;
    }

    public static int getThemeModeValue() {
        return getPrefs().getInt(KEY_THEME_MODE, 0);
    }

    public static void setThemeModeValue(int mode) {
        getPrefs().putInt(KEY_THEME_MODE, mode);
    }

    public static boolean isNotificationsEnabled() {
        return getPrefs().getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsEnabled(boolean enabled) {
        getPrefs().putBoolean(KEY_NOTIFICATIONS, enabled);
    }

    /**
     * 获取用户是否请求过通知偏好（记录用户意图）
     */
    public static boolean isNotificationPreferenceRequested() {
        return getPrefs().getBoolean(KEY_NOTIFICATION_PREFERENCE_REQUESTED, false);
    }

    /**
     * 设置用户通知偏好请求状态
     */
    public static void setNotificationPreferenceRequested(boolean requested) {
        getPrefs().putBoolean(KEY_NOTIFICATION_PREFERENCE_REQUESTED, requested);
    }

    /**
     * 获取通知权限被拒绝的时间戳
     *
     * @return 时间戳，如果从未被拒绝则返回 0
     */
    public static long getNotificationPermissionDeniedTime() {
        return getPrefs().getLong(KEY_NOTIFICATION_PERMISSION_DENIED_TIME, 0);
    }

    /**
     * 记录通知权限被拒绝的时间戳
     */
    public static void setNotificationPermissionDeniedTime(long timestamp) {
        getPrefs().putLong(KEY_NOTIFICATION_PERMISSION_DENIED_TIME, timestamp);
    }

    public static boolean isHomeTopEnabled() {
        return getPrefs().getBoolean(KEY_HOME_TOP_ENABLED, false);
    }

    public static void setHomeTopEnabled(boolean enabled) {
        getPrefs().putBoolean(KEY_HOME_TOP_ENABLED, enabled);
    }

    public static boolean isNoImageModeEnabled() {
        return getPrefs().getBoolean(KEY_NO_IMAGE_MODE, false);
    }

    public static void setNoImageModeEnabled(boolean enabled) {
        getPrefs().putBoolean(KEY_NO_IMAGE_MODE, enabled);
    }

    public static boolean isAutoHideBottomBarEnabled() {
        return getPrefs().getBoolean(KEY_AUTO_HIDE_BOTTOM_BAR, false);
    }

    public static void setAutoHideBottomBarEnabled(boolean enabled) {
        getPrefs().putBoolean(KEY_AUTO_HIDE_BOTTOM_BAR, enabled);
    }
}
