package com.rapid.android.data.repository;

import com.rapid.android.data.local.database.AppDatabase;
import com.rapid.android.data.local.entity.UserEntity;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class UserLocalDataSourceWithRoom implements UserLocalDataSource {
    private final AppDatabase database;

    public UserLocalDataSourceWithRoom(AppDatabase database) {
        this.database = database;
    }

    @Override
    public Single<Boolean> isLoggedIn() {
        return database.userDao()
                .getUsersCount()
                .map(count -> count > 0);
    }

    @Override
    public Completable saveAuthData(String token, String userId, String username, String password) {
        // 将用户信息存储到数据库
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername(username);
        user.setToken(token);
        // 注意：出于安全考虑，不应在数据库中存储明文密码
        return database.userDao().insertUser(user);
    }

    @Override
    public Completable clearAuthData() {
        return database.userDao().deleteAllUsers();
    }

    @Override
    public Single<String> getAuthToken() {
        // 获取当前登录用户（如果有）的token
        return database.userDao()
                .getAllUsers()
                .map(users -> {
                    if (!users.isEmpty()) {
                        return users.get(0).getToken();
                    }
                    return null;
                });
    }

    @Override
    public Single<String> getUserId() {
        // 获取当前登录用户（如果有）的ID
        return database.userDao()
                .getAllUsers()
                .map(users -> {
                    if (!users.isEmpty()) {
                        return users.get(0).getId();
                    }
                    return null;
                });
    }

    @Override
    public Single<String> getUsername() {
        // 从数据库获取用户名
        return database.userDao()
                .getAllUsers()
                .map(users -> {
                    if (!users.isEmpty()) {
                        return users.get(0).getUsername();
                    }
                    return null;
                });
    }

    @Override
    public Single<String> getPassword() {
        // 对于安全原因，密码不应存储在数据库中，返回空
        // 如果需要支持relogin功能，可考虑在数据库中存储加密的凭证
        return Single.just("");
    }
}