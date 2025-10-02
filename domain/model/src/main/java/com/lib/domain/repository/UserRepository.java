package com.lib.domain.repository;

import com.lib.domain.model.CoinBean;
import com.lib.domain.model.LoginBean;
import com.lib.domain.model.UserInfoBean;
import com.lib.domain.result.DomainResult;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface UserRepository {

    Observable<DomainResult<LoginBean>> login(String username, String password);

    Observable<DomainResult<String>> logout();

    Single<Boolean> isLoggedIn();

    Completable saveAuthData(String token, String userId, String username, String password);

    Completable clearAuthData();

    Single<String> getAuthToken();

    Single<String> getUserId();

    Observable<DomainResult<LoginBean>> reLogin();

    Observable<DomainResult<UserInfoBean>> fetchUserProfile();

    Observable<DomainResult<CoinBean>> signIn();
}
