package com.core.data.repository.user;

import com.core.data.local.AuthStorage;
import com.core.data.mapper.DomainResultMapper;
import com.core.data.network.NetApis;
import com.core.data.network.PersistentCookieStore;
import com.core.domain.model.*;
import com.core.domain.repository.UserRepository;
import com.core.domain.result.DomainError;
import com.core.domain.result.DomainResult;
import com.core.network.base.BaseResponse;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class UserRepositoryImpl implements UserRepository {
    private final AuthStorage authStorage = AuthStorage.getInstance();

    @Override
    public Observable<DomainResult<LoginBean>> login(String username, String password) {
        return map(NetApis.Login().login(username, password));
    }

    @Override
    public Observable<DomainResult<String>> logout() {
        return map(NetApis.Login().logout())
            .flatMap(result -> {
                // 无论网络登出是否成功，都清除本地认证数据和持久化Cookie
                return authStorage.clearAuthData()
                    .andThen(clearPersistentCookies()) // 添加清除持久化Cookie的步骤
                    .toObservable()
                    .map(ignore -> result); // 将Completable转换为Observable并返回原始结果
            })
            .onErrorResumeNext(throwable -> {
                // 即使网络登出失败，也清除本地认证数据和持久化Cookie
                return authStorage.clearAuthData()
                    .andThen(clearPersistentCookies()) // 添加清除持久化Cookie的步骤
                    .toObservable()
                    .map(ignore -> DomainResult.<String>failure(DomainError.of(DomainError.UNKNOWN_CODE, "网络登出失败: " + throwable.getMessage())));
            });
    }
    
    /**
     * 清除持久化Cookie
     */
    private Completable clearPersistentCookies() {
        return Completable.fromAction(() -> {
            PersistentCookieStore cookieStore = PersistentCookieStore.getInstance();
            if (cookieStore != null) {
                cookieStore.clearCookies();
            }
        });
    }

    @Override
    public Single<Boolean> isLoggedIn() {
        return authStorage.isLoggedIn();
    }

    @Override
    public Completable saveAuthData(String token, String userId, String username, String password) {
        return authStorage.saveAuthData(token, userId, username, password);
    }

    @Override
    public Completable clearAuthData() {
        return authStorage.clearAuthData();
    }

    @Override
    public Single<String> getAuthToken() {
        return authStorage.getAuthToken();
    }

    @Override
    public Single<String> getUserId() {
        return authStorage.getUserId();
    }

    @Override
    public Observable<DomainResult<LoginBean>> reLogin() {
        return authStorage.getUsername()
            .flatMapObservable(username -> {
                if (username != null) {
                    return authStorage.getPassword()
                        .flatMapObservable(password -> {
                            if (password != null) {
                                return map(NetApis.Login().login(username, password));
                            } else {
                                return Observable.just(DomainResult.<LoginBean>failure(DomainError.of(DomainError.UNKNOWN_CODE, "Password not available")));
                            }
                        });
                } else {
                    return Observable.just(DomainResult.<LoginBean>failure(DomainError.of(DomainError.UNKNOWN_CODE, "Username not available")));
                }
            })
            .onErrorReturn(DomainResultMapper::mapError);
    }

    @Override
    public Observable<DomainResult<UserInfoBean>> fetchUserProfile() {
        return map(NetApis.User().userinfo());
    }

    @Override
    public Observable<DomainResult<CoinBean>> signIn() {
        return map(NetApis.User().signIn());
    }

    @Override
    public Observable<DomainResult<ArticleListBean>> favoriteArticles(int page) {
        return map(NetApis.User().collectList(page));
    }

    @Override
    public Observable<DomainResult<UserShareBean>> myShareArticles(int page, Integer pageSize) {
        return map(NetApis.User().privateShareArticles(page, pageSize));
    }

    @Override
    public Observable<DomainResult<String>> deleteShareArticle(int articleId) {
        return map(NetApis.User().deleteShareArticle(articleId));
    }

    @Override
    public Observable<DomainResult<ArticleListBean.Data>> shareArticle(String title, String link) {
        return map(NetApis.User().addShareArticle(title, link));
    }

    @Override
    public Observable<DomainResult<ArticleListBean.Data>> collectOutside(String title, String author, String link) {
        return map(NetApis.User().collectOutside(title, author, link));
    }

    @Override
    public Observable<DomainResult<ArticleListBean.Data>> updateCollectedArticle(int articleId, String title, String link, String author) {
        return map(NetApis.User().updateCollectedArticle(articleId, title, link, author));
    }

    private <T> Observable<DomainResult<T>> map(Observable<BaseResponse<T>> source) {
        return source
            .map(DomainResultMapper::map)
            .onErrorReturn(DomainResultMapper::mapError);
    }
}
