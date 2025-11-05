package com.rapid.android.core.data.repository.user;

import com.rapid.android.core.data.mapper.DomainResultMapper;
import com.rapid.android.core.data.network.NetApis;
import com.rapid.android.core.domain.model.*;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.domain.result.DomainError;
import com.rapid.android.core.domain.result.DomainResult;
import com.rapid.android.core.network.base.BaseResponse;
import com.rapid.android.core.storage.AuthStorage;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class UserRepositoryImpl implements UserRepository {
    private final AuthStorage authStorage = AuthStorage.getInstance();

    @Override
    public Observable<DomainResult<LoginBean>> login(String username, String password) {
        return map(NetApis.Login().login(username, password));
    }

    @Override
    public Observable<DomainResult<RegisterBean>> register(String username, String password, String rePassword) {
        return map(NetApis.Login().register(username, password, rePassword));
    }

    @Override
    public Observable<DomainResult<String>> logout() {
        return map(NetApis.Login().logout())
                .flatMap(result -> {
                    // 无论网络登出是否成功，都清除本地认证数据和持久化Cookie
                    return authStorage.clearAuthData()
                            .toObservable()
                            .map(ignore -> result); // 将Completable转换为Observable并返回原始结果
                })
                .onErrorResumeNext(throwable -> {
                    // 即使网络登出失败，也清除本地认证数据和持久化Cookie
                    return authStorage.clearAuthData()
                            .toObservable()
                            .map(ignore -> DomainResult.failure(DomainError.of(DomainError.UNKNOWN_CODE, "网络登出失败: " + throwable.getMessage())));
                });
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

    @Override
    public Observable<DomainResult<List<UserToolBean>>> userTools() {
        return map(NetApis.User().userTools());
    }

    @Override
    public Observable<DomainResult<UserToolBean>> addUserTool(String name, String link) {
        return map(NetApis.User().addUserTool(name, link));
    }

    @Override
    public Observable<DomainResult<UserToolBean>> updateUserTool(int id, String name, String link) {
        return map(NetApis.User().updateUserTool(id, name, link));
    }

    @Override
    public Observable<DomainResult<String>> deleteUserTool(int id) {
        return map(NetApis.User().deleteUserTool(id));
    }

    @Override
    public Observable<DomainResult<String>> collectArticle(int id) {
        return map(NetApis.User().collect(id));
    }

    @Override
    public Observable<DomainResult<String>> unCollectArticle(int id) {
        return map(NetApis.User().unCollect(id));
    }

    @Override
    public Observable<DomainResult<String>> unCollectFavorite(int collectId, int originId) {
        return map(NetApis.User().unCollectInMine(collectId, originId));
    }

    private <T> Observable<DomainResult<T>> map(Observable<BaseResponse<T>> source) {
        return source
                .map(DomainResultMapper::map)
                .onErrorReturn(DomainResultMapper::mapError);
    }
}
