package com.rapid.android.core.storage;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public final class AuthStorage {
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static volatile AuthStorage instance;

    private AuthStorage() {
        // 私有构造函数防止直接实例化
    }

    private static PreferenceHelper getPrefs() {
        return PreferenceHelper.getDefault();
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
        return Single.fromCallable(() -> {
            String token = getPrefs().getString(KEY_TOKEN, null);
            String userId = getPrefs().getString(KEY_USER_ID, null);
            return token != null && userId != null;
        });
    }

    public Completable saveAuthData(String token, String userId, String username, String password) {
        return Completable.fromAction(() -> {
            getPrefs().putString(KEY_TOKEN, token);
            getPrefs().putString(KEY_USER_ID, userId);
            getPrefs().putString(KEY_USERNAME, username);
            getPrefs().putString(KEY_PASSWORD, password);
        });
    }

    public Completable clearAuthData() {
        return Completable.fromAction(() -> {
            getPrefs().remove(KEY_TOKEN);
            getPrefs().remove(KEY_USER_ID);
            getPrefs().remove(KEY_USERNAME);
            getPrefs().remove(KEY_PASSWORD);
        });
    }

    public Single<String> getUserId() {
        return Single.fromCallable(() -> getPrefs().getString(KEY_USER_ID, null));
    }

    public Single<String> getUsername() {
        return Single.fromCallable(() -> getPrefs().getString(KEY_USERNAME, null));
    }

    public Single<String> getPassword() {
        return Single.fromCallable(() -> getPrefs().getString(KEY_PASSWORD, null));
    }

    public String peekToken() {
        return getPrefs().getString(KEY_TOKEN, null);
    }

    public String peekUserId() {
        return getPrefs().getString(KEY_USER_ID, null);
    }

    public String peekUsername() {
        return getPrefs().getString(KEY_USERNAME, null);
    }

    public String peekPassword() {
        return getPrefs().getString(KEY_PASSWORD, null);
    }
}
