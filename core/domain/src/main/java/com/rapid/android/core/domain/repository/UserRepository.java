package com.rapid.android.core.domain.repository;

import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.domain.result.DomainResult;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public interface UserRepository {

    Observable<DomainResult<LoginBean>> login(String username, String password);

    Observable<DomainResult<RegisterBean>> register(String username, String password, String rePassword);

    Observable<DomainResult<String>> logout();

    Observable<DomainResult<LoginBean>> reLogin();

    Observable<DomainResult<UserInfoBean>> fetchUserProfile();

    Observable<DomainResult<CoinBean>> signIn();

    Observable<DomainResult<ArticleListBean>> favoriteArticles(int page);

    Observable<DomainResult<UserShareBean>> myShareArticles(int page, Integer pageSize);

    Observable<DomainResult<String>> deleteShareArticle(int articleId);

    Observable<DomainResult<ArticleListBean.Data>> shareArticle(String title, String link);

    Observable<DomainResult<ArticleListBean.Data>> collectOutside(String title, String author, String link);

    Observable<DomainResult<ArticleListBean.Data>> updateCollectedArticle(int articleId, String title, String link, String author);

    Observable<DomainResult<List<UserToolBean>>> userTools();

    Observable<DomainResult<UserToolBean>> addUserTool(String name, String link);

    Observable<DomainResult<UserToolBean>> updateUserTool(int id, String name, String link);

    Observable<DomainResult<String>> deleteUserTool(int id);

    Observable<DomainResult<String>> collectArticle(int id);

    Observable<DomainResult<String>> unCollectArticle(int id);

    Observable<DomainResult<String>> unCollectFavorite(int collectId, int originId);
}
