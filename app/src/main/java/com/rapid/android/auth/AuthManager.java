package com.rapid.android.auth;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.core.CoreApp;
import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.network.NetApis;

import java.io.IOException;
import java.security.GeneralSecurityException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static AuthManager instance;
    private final EncryptedSharedPreferences prefs;
    private final MutableLiveData<Boolean> loginState = new MutableLiveData<>();
    private final MutableLiveData<String> loginError = new MutableLiveData<>();

    private AuthManager() {
        try {
            Context appContext = CoreApp.getAppContext();
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    appContext, PREF_NAME, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            loginState.setValue(isLoggedIn());
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize AuthManager", e);
        }
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public void login(String username, String password, DisposableObserver<BaseResponse<LoginBean>> observer) {
        Observable<BaseResponse<LoginBean>> observable = NetApis.Login().login(username, password);
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<BaseResponse<LoginBean>>() {
                    @Override
                    public void onNext(BaseResponse<LoginBean> response) {
                        if (response.getData() != null) {
                            LoginBean data = response.getData();
                            saveAuthData(data.getToken(), String.valueOf(data.getId()), username, password);
                            loginState.setValue(true);
                            loginError.setValue(null);
                        } else {
                            loginState.setValue(false);
                            loginError.setValue("登录失败");
                        }
                        if (observer != null) {
                            observer.onNext(response);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loginState.setValue(false);
                        loginError.setValue(e.getMessage());
                        if (observer != null) {
                            observer.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (observer != null) {
                            observer.onComplete();
                        }
                    }
                });
    }

    public void reLogin(DisposableObserver<BaseResponse<LoginBean>> observer) {
        String username = prefs.getString(KEY_USERNAME, null);
        String password = prefs.getString(KEY_PASSWORD, null);
        if (username != null && password != null) {
            login(username, password, observer);
        } else {
            loginState.setValue(false);
            loginError.setValue("No credentials available for relogin");
            if (observer != null) {
                observer.onError(new IllegalStateException("No credentials available"));
            }
        }
    }

    public void saveAuthData(String token, String userId, String username, String password) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply();
        loginState.setValue(true);
        loginError.setValue(null);
    }

    public void clearAuthData() {
        prefs.edit().clear().apply();
        loginState.setValue(false);
        loginError.setValue(null);
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_TOKEN) && prefs.contains(KEY_USER_ID);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public LiveData<Boolean> getLoginState() {
        return loginState;
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }
}
