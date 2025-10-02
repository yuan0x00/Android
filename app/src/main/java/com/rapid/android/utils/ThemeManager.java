package com.rapid.android.utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private ThemeManager() {
    }

    public static void applySavedTheme() {
        ThemeMode mode = getSavedThemeMode();
        applyThemeMode(mode);
    }

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
        AppPreferences.setThemeModeValue(mode.getValue());
    }

    public static ThemeMode getSavedThemeMode() {
        return ThemeMode.fromValue(AppPreferences.getThemeModeValue());
    }

    public enum ThemeMode {
        SYSTEM(0),
        LIGHT(1),
        DARK(2);

        private final int value;

        ThemeMode(int value) {
            this.value = value;
        }

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
