package com.rapid.android.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * 加密存储管理器
 * 用于存储敏感数据（如 token、密码等）
 * 基于 EncryptedSharedPreferences 实现
 */
public class SecureStorage {

    private final SharedPreferences encryptedPreferences;

    public SecureStorage(@NonNull Context context, @NonNull String fileName) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            this.encryptedPreferences = EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create secure storage", e);
        }
    }

    public void putString(@NonNull String key, @Nullable String value) {
        encryptedPreferences.edit().putString(key, value).apply();
    }

    @Nullable
    public String getString(@NonNull String key) {
        return getString(key, null);
    }

    @Nullable
    public String getString(@NonNull String key, @Nullable String defaultValue) {
        return encryptedPreferences.getString(key, defaultValue);
    }

    public void putInt(@NonNull String key, int value) {
        encryptedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(@NonNull String key, int defaultValue) {
        return encryptedPreferences.getInt(key, defaultValue);
    }

    public void putLong(@NonNull String key, long value) {
        encryptedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(@NonNull String key, long defaultValue) {
        return encryptedPreferences.getLong(key, defaultValue);
    }

    public void putBoolean(@NonNull String key, boolean value) {
        encryptedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return encryptedPreferences.getBoolean(key, defaultValue);
    }

    public boolean contains(@NonNull String key) {
        return encryptedPreferences.contains(key);
    }

    public void remove(@NonNull String key) {
        encryptedPreferences.edit().remove(key).apply();
    }

    public void clear() {
        encryptedPreferences.edit().clear().apply();
    }
}
