package com.rapid.android.data.local;

import android.content.Context;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.core.CoreApp;

import java.io.IOException;
import java.security.GeneralSecurityException;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public final class AuthStorage {
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static volatile AuthStorage instance;
    private final EncryptedSharedPreferences prefs;

    private AuthStorage() {
        try {
            Context appContext = CoreApp.getAppContext();
            MasterKey masterKey = new MasterKey.Builder(appContext, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            this.prefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    appContext,
                    "auth_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize AuthStorage", e);
        }
    }

    public static AuthStorage getInstance() {
        if (instance == null) {
            synchronized (AuthStorage.class) {
                if (instance == null) {
                    instance = new AuthStorage();
                }
            }
        }
        return instance;
    }

    public Single<Boolean> isLoggedIn() {
        return Single.fromCallable(() -> prefs.contains(KEY_TOKEN) && prefs.contains(KEY_USER_ID));
    }

    public Completable saveAuthData(String token, String userId, String username, String password) {
        return Completable.fromAction(() -> {
            prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply();
        });
    }

    public Completable clearAuthData() {
        return Completable.fromAction(() -> prefs.edit().clear().apply());
    }

    public Single<String> getAuthToken() {
        return Single.fromCallable(() -> prefs.getString(KEY_TOKEN, null));
    }

    public Single<String> getUserId() {
        return Single.fromCallable(() -> prefs.getString(KEY_USER_ID, null));
    }

    public Single<String> getUsername() {
        return Single.fromCallable(() -> prefs.getString(KEY_USERNAME, null));
    }

    public Single<String> getPassword() {
        return Single.fromCallable(() -> prefs.getString(KEY_PASSWORD, null));
    }
}