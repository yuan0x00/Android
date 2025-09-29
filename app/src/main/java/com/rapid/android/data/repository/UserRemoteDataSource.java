package com.rapid.android.data.repository;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.UserInfoBean;

import io.reactivex.rxjava3.core.Single;

public interface UserRemoteDataSource {
    Single<BaseResponse<LoginBean>> login(String username, String password);
    Single<BaseResponse<String>> logout();
    Single<BaseResponse<UserInfoBean>> fetchUserProfile();
}
