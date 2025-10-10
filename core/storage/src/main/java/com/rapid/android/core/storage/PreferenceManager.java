package com.rapid.android.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Set;

/**
 * 统一的偏好设置管理器
 * 基于 SharedPreferences 封装，提供类型安全的 API
 */
public class PreferenceManager {

    private final SharedPreferences preferences;
    private final Gson gson;

    public PreferenceManager(@NonNull Context context, @NonNull String name) {
        this.preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // ==================== String ====================

    public void putString(@NonNull String key, @Nullable String value) {
        preferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String getString(@NonNull String key) {
        return getString(key, null);
    }

    @Nullable
    public String getString(@NonNull String key, @Nullable String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    // ==================== Int ====================

    public void putInt(@NonNull String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public int getInt(@NonNull String key) {
        return getInt(key, 0);
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    // ==================== Long ====================

    public void putLong(@NonNull String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public long getLong(@NonNull String key) {
        return getLong(key, 0L);
    }

    public long getLong(@NonNull String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    // ==================== Float ====================

    public void putFloat(@NonNull String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }

    public float getFloat(@NonNull String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(@NonNull String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    // ==================== Boolean ====================

    public void putBoolean(@NonNull String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(@NonNull String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    // ==================== StringSet ====================

    public void putStringSet(@NonNull String key, @Nullable Set<String> value) {
        preferences.edit().putStringSet(key, value).apply();
    }

    @Nullable
    public Set<String> getStringSet(@NonNull String key) {
        return getStringSet(key, null);
    }

    @Nullable
    public Set<String> getStringSet(@NonNull String key, @Nullable Set<String> defaultValue) {
        return preferences.getStringSet(key, defaultValue);
    }

    // ==================== Object (JSON) ====================

    public <T> void putObject(@NonNull String key, @Nullable T value) {
        if (value == null) {
            remove(key);
        } else {
            String json = gson.toJson(value);
            putString(key, json);
        }
    }

    @Nullable
    public <T> T getObject(@NonNull String key, @NonNull Class<T> type) {
        String json = getString(key);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, type);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Utility ====================

    public boolean contains(@NonNull String key) {
        return preferences.contains(key);
    }

    public void remove(@NonNull String key) {
        preferences.edit().remove(key).apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

    /**
     * 批量操作（原子性）
     */
    public Editor edit() {
        return new Editor(preferences.edit());
    }

    /**
     * 编辑器，支持批量操作
     */
    public static class Editor {
        private final SharedPreferences.Editor editor;

        private Editor(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        public Editor putString(@NonNull String key, @Nullable String value) {
            editor.putString(key, value);
            return this;
        }

        public Editor putInt(@NonNull String key, int value) {
            editor.putInt(key, value);
            return this;
        }

        public Editor putLong(@NonNull String key, long value) {
            editor.putLong(key, value);
            return this;
        }

        public Editor putFloat(@NonNull String key, float value) {
            editor.putFloat(key, value);
            return this;
        }

        public Editor putBoolean(@NonNull String key, boolean value) {
            editor.putBoolean(key, value);
            return this;
        }

        public Editor remove(@NonNull String key) {
            editor.remove(key);
            return this;
        }

        public Editor clear() {
            editor.clear();
            return this;
        }

        public void apply() {
            editor.apply();
        }

        public boolean commit() {
            return editor.commit();
        }
    }
}
