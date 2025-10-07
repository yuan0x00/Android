package com.rapid.android.core.domain.repository;

import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.domain.result.DomainResult;

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

    Observable<DomainResult<ArticleListBean>> favoriteArticles(int page);

    Observable<DomainResult<UserShareBean>> myShareArticles(int page, Integer pageSize);

    Observable<DomainResult<String>> deleteShareArticle(int articleId);

    Observable<DomainResult<ArticleListBean.Data>> shareArticle(String title, String link);

    Observable<DomainResult<ArticleListBean.Data>> collectOutside(String title, String author, String link);

    Observable<DomainResult<ArticleListBean.Data>> updateCollectedArticle(int articleId, String title, String link, String author);
}
