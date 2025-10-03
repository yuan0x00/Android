package com.lib.data.repository.user;

import com.core.network.base.BaseResponse;
import com.lib.data.local.AuthStorage;
import com.lib.data.mapper.DomainResultMapper;
import com.lib.data.network.NetApis;
import com.lib.data.network.PersistentCookieStore;
import com.lib.domain.model.ArticleListBean;
import com.lib.domain.model.CoinBean;
import com.lib.domain.model.LoginBean;
import com.lib.domain.model.UserInfoBean;
import com.lib.domain.repository.UserRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;

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
            .onErrorReturn(throwable -> DomainResultMapper.mapError(throwable));
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

    private <T> Observable<DomainResult<T>> map(Observable<BaseResponse<T>> source) {
        return source
            .map(DomainResultMapper::map)
            .onErrorReturn(DomainResultMapper::mapError);
    }
}
