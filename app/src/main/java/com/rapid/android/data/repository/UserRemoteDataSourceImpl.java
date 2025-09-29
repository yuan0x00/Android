package com.rapid.android.data.repository;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.data.network.NetApis;

import io.reactivex.rxjava3.core.Single;

public class UserRemoteDataSourceImpl implements UserRemoteDataSource {
    @Override
    public Single<BaseResponse<LoginBean>> login(String username, String password) {
        return NetApis.Login().login(username, password).firstOrError();
    }

    @Override
    public Single<BaseResponse<String>> logout() {
        return NetApis.Login().logout().firstOrError();
    }

    @Override
    public Single<BaseResponse<UserInfoBean>> fetchUserProfile() {
        return NetApis.User().userinfo().firstOrError();
    }
}
