package com.rapid.android.domain.repository;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.UserInfoBean;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface UserRepository {
    Single<BaseResponse<LoginBean>> login(String username, String password);
    Single<BaseResponse<String>> logout();
    Single<Boolean> isLoggedIn();
    Completable saveAuthData(String token, String userId, String username, String password);
    Completable clearAuthData();
    Single<String> getAuthToken();
    Single<String> getUserId();
    
    // 重新登录功能
    Single<BaseResponse<LoginBean>> reLogin();
    Single<BaseResponse<UserInfoBean>> fetchUserProfile();
}
