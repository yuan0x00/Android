package com.rapid.android.core.data.local;

import com.rapid.android.core.datastore.IDataStore;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class DataStoreAuthStorage {
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static volatile DataStoreAuthStorage instance;
    private final IDataStore dataStore;

    private DataStoreAuthStorage(IDataStore dataStore) {
        this.dataStore = dataStore;
    }

    public static void init(IDataStore dataStore) {
        if (instance == null) {
            synchronized (DataStoreAuthStorage.class) {
                if (instance == null) {
                    instance = new DataStoreAuthStorage(dataStore);
                }
            }
        }
    }

    public static DataStoreAuthStorage getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DataStoreAuthStorage not initialized. Call init() first.");
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