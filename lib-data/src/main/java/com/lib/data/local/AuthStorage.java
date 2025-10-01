package com.lib.data.local;

import com.core.datastore.IDataStore;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public final class AuthStorage {
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static volatile AuthStorage instance;
    private static IDataStore dataStore;

    private AuthStorage() {
        // 私有构造函数防止直接实例化
    }

    public static void init(IDataStore store) {
        dataStore = store;
    }

    public static AuthStorage getInstance() {
        if (dataStore == null) {
            throw new IllegalStateException("AuthStorage not initialized. Call init() first.");
        }
        
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
            String token = dataStore.getString(KEY_TOKEN, null);
            String userId = dataStore.getString(KEY_USER_ID, null);
            return token != null && userId != null;
        });
    }

    public Completable saveAuthData(String token, String userId, String username, String password) {
        return Completable.fromAction(() -> {
            dataStore.putString(KEY_TOKEN, token);
            dataStore.putString(KEY_USER_ID, userId);
            dataStore.putString(KEY_USERNAME, username);
            dataStore.putString(KEY_PASSWORD, password);
        });
    }

    public Completable clearAuthData() {
        return Completable.fromAction(() -> {
            dataStore.remove(KEY_TOKEN);
            dataStore.remove(KEY_USER_ID);
            dataStore.remove(KEY_USERNAME);
            dataStore.remove(KEY_PASSWORD);
        });
    }

    public Single<String> getAuthToken() {
        return Single.fromCallable(() -> dataStore.getString(KEY_TOKEN, null));
    }

    public Single<String> getUserId() {
        return Single.fromCallable(() -> dataStore.getString(KEY_USER_ID, null));
    }

    public Single<String> getUsername() {
        return Single.fromCallable(() -> dataStore.getString(KEY_USERNAME, null));
    }

    public Single<String> getPassword() {
        return Single.fromCallable(() -> dataStore.getString(KEY_PASSWORD, null));
    }
}
