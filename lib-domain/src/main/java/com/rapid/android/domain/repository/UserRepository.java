package com.rapid.android.domain.repository;

import com.rapid.android.domain.model.CoinBean;
import com.rapid.android.domain.model.LoginBean;
import com.rapid.android.domain.model.UserInfoBean;
import com.rapid.android.domain.result.DomainResult;

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
