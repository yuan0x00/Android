package com.rapid.android.data.session;

import androidx.annotation.Nullable;

import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.data.repository.user.UserRepository;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class SessionStateRepository {

    private static final SessionStateRepository INSTANCE = new SessionStateRepository();

    private final UserRepository userRepository;
    private UserInfoBean cachedUserInfo;

    private SessionStateRepository() {
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    public static SessionStateRepository getInstance() {
        return INSTANCE;
    }

    public Single<UserInfoBean> getCachedUserInfo() {
        return userRepository.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .flatMap(isLoggedIn -> {
                    if (!Boolean.TRUE.equals(isLoggedIn)) {
                        cacheUserInfo(null);
                        return Single.fromCallable(() -> (UserInfoBean) null);
                    }
                    if (cachedUserInfo != null) {
                        return Single.just(cachedUserInfo);
                    }
                    UserInfoBean sessionUser = AuthSessionManager.userInfo().getValue();
                    if (sessionUser != null) {
                        cacheUserInfo(sessionUser);
                        return Single.just(sessionUser);
                    }
                    return refreshUserInfo();
                });
    }

    public Single<UserInfoBean> refreshUserInfo() {
        return userRepository.fetchUserProfile()
                .map(response -> response.requireData())
                .subscribeOn(Schedulers.io())
                .firstOrError()
                .doOnSuccess(this::cacheUserInfo)
                .doOnError(throwable -> cacheUserInfo(null));
    }

    private void cacheUserInfo(@Nullable UserInfoBean info) {
        AuthSessionManager.updateUserInfo(info);
        cachedUserInfo = info;
    }

    public void restore() {
        try {
            UserInfoBean info = getCachedUserInfo().blockingGet();
            if (info == null) {
                AuthSessionManager.notifyLogout();
            } else {
                AuthSessionManager.notifyLogin();
                AuthSessionManager.updateUserInfo(info);
            }
        } catch (Exception e) {
            AuthSessionManager.notifyLogout();
        }
    }
}
