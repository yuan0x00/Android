package com.rapid.android.data.repository;

import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserRepositoryImpl implements UserRepository {
    private final UserRemoteDataSource remoteDataSource;
    private final UserLocalDataSource localDataSource;

    public UserRepositoryImpl(UserRemoteDataSource remoteDataSource, UserLocalDataSource localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public Single<BaseResponse<LoginBean>> login(String username, String password) {
        return remoteDataSource.login(username, password);
    }

    @Override
    public Single<BaseResponse<String>> logout() {
        return remoteDataSource.logout()
            .flatMap(result -> {
                if (result.isSuccess()) {
                    return localDataSource.clearAuthData()
                        .andThen(Single.just(result));
                } else {
                    return Single.just(result);
                }
            });
    }

    @Override
    public Single<Boolean> isLoggedIn() {
        return localDataSource.isLoggedIn();
    }

    @Override
    public Completable saveAuthData(String token, String userId, String username, String password) {
        return localDataSource.saveAuthData(token, userId, username, password);
    }

    @Override
    public Completable clearAuthData() {
        return localDataSource.clearAuthData();
    }

    @Override
    public Single<String> getAuthToken() {
        return localDataSource.getAuthToken();
    }

    @Override
    public Single<String> getUserId() {
        return localDataSource.getUserId();
    }

    @Override
    public Single<BaseResponse<LoginBean>> reLogin() {
        // 获取本地存储的用户名和密码进行重新登录
        return localDataSource.getUsername()
            .flatMap(username -> {
                if (username != null) {
                    return localDataSource.getPassword()
                        .flatMap(password -> {
                            if (password != null) {
                                // 使用存储的用户名和密码重新登录
                                return remoteDataSource.login(username, password);
                            } else {
                                return Single.error(new Exception("Password not available"));
                            }
                        });
                } else {
                    return Single.error(new Exception("Username not available"));
                }
            });
    }

    @Override
    public Single<BaseResponse<UserInfoBean>> fetchUserProfile() {
        return remoteDataSource.fetchUserProfile();
    }
}
