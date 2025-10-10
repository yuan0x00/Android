package com.rapid.android.core.theme;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.rapid.android.core.storage.PreferenceManager;

/**
 * 主题管理器
 * 统一管理应用主题模式（浅色/深色/跟随系统）
 */
public final class ThemeManager {

    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    private static volatile PreferenceManager prefs;

    private ThemeManager() {
    }

    /**
     * 初始化主题管理器
     * 应在 Application 中调用
     */
    public static void init(@NonNull Context context) {
        if (prefs == null) {
            synchronized (ThemeManager.class) {
                if (prefs == null) {
                    prefs = new PreferenceManager(context.getApplicationContext(), PREF_NAME);
                }
            }
        }
    }

    private static PreferenceManager getPrefs() {
        if (prefs == null) {
            throw new IllegalStateException("ThemeManager not initialized. Call init(Context) first.");
        }
        return prefs;
    }

    /**
     * 应用已保存的主题
     */
    public static void applySavedTheme() {
        ThemeMode mode = getSavedThemeMode();
        applyThemeMode(mode);
    }

    /**
     * 应用指定主题模式
     */
    public static void applyThemeMode(@NonNull ThemeMode mode) {
        switch (mode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        getPrefs().putInt(KEY_THEME_MODE, mode.getValue());
    }

    /**
     * 获取已保存的主题模式
     */
    @NonNull
    public static ThemeMode getSavedThemeMode() {
        int value = getPrefs().getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.getValue());
        return ThemeMode.fromValue(value);
    }

    /**
     * 主题模式枚举
     */
    public enum ThemeMode {
        /** 跟随系统 */
        SYSTEM(0),
        /** 浅色模式 */
        LIGHT(1),
        /** 深色模式 */
        DARK(2);

        private final int value;

        ThemeMode(int value) {
            this.value = value;
        }

        @NonNull
        public static ThemeMode fromValue(int value) {
            for (ThemeMode mode : values()) {
                if (mode.value == value) {
                    return mode;
                }
            }
            return SYSTEM;
        }

        public int getValue() {
            return value;
        }
    }
}
