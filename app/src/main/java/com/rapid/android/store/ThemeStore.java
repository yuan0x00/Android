package com.rapid.android.store;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.rapid.android.core.storage.AppPreferencesStorage;

public final class ThemeStore {

    private ThemeStore() {
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
    }

    public static ThemeMode getSavedThemeMode() {
        return ThemeMode.fromValue(AppPreferencesStorage.getThemeModeValue());
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
