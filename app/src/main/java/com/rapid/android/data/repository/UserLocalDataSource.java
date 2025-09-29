package com.rapid.android.data.repository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface UserLocalDataSource {
    Single<Boolean> isLoggedIn();
    Completable saveAuthData(String token, String userId, String username, String password);
    Completable clearAuthData();
    Single<String> getAuthToken();
    Single<String> getUserId();
    
    // 重新登录需要的方法
    Single<String> getUsername();
    Single<String> getPassword();
}