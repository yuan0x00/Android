package com.rapid.android.data.repository.user;

import com.core.network.base.BaseResponse;
import com.rapid.android.data.local.AuthStorage;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.data.network.NetApis;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {
    private final AuthStorage authStorage = AuthStorage.getInstance();

    public Observable<BaseResponse<LoginBean>> login(String username, String password) {
        return NetApis.Login().login(username, password);
    }

    public Observable<BaseResponse<String>> logout() {
        return NetApis.Login().logout()
            .flatMap(result -> {
                if (result.isSuccess()) {
                    return authStorage.clearAuthData()
                        .andThen(Observable.just(result));
                } else {
                    return Observable.just(result);
                }
            });
    }

    public Single<Boolean> isLoggedIn() {
        return authStorage.isLoggedIn();
    }

    public Completable saveAuthData(String token, String userId, String username, String password) {
        return authStorage.saveAuthData(token, userId, username, password);
    }

    public Completable clearAuthData() {
        return authStorage.clearAuthData();
    }

    public Single<String> getAuthToken() {
        return authStorage.getAuthToken();
    }

    public Single<String> getUserId() {
        return authStorage.getUserId();
    }

    public Observable<BaseResponse<LoginBean>> reLogin() {
        return authStorage.getUsername()
            .flatMapObservable(username -> {
                if (username != null) {
                    return authStorage.getPassword()
                        .flatMapObservable(password -> {
                            if (password != null) {
                                return NetApis.Login().login(username, password);
                            } else {
                                return Observable.error(new Exception("Password not available"));
                            }
                        });
                } else {
                    return Observable.error(new Exception("Username not available"));
                }
            });
    }

    public Observable<BaseResponse<UserInfoBean>> fetchUserProfile() {
        return NetApis.User().userinfo();
    }
}
