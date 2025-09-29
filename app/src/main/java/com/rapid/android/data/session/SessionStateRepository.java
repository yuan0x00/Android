package com.rapid.android.data.session;

import androidx.annotation.Nullable;

import com.rapid.android.data.model.UserInfoBean;
import com.rapid.android.domain.usecase.CheckLoginStatusUseCase;
import com.rapid.android.domain.usecase.GetUserProfileUseCase;
import com.rapid.android.domain.usecase.UseCaseProvider;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class SessionStateRepository {

    private static final SessionStateRepository INSTANCE = new SessionStateRepository();

    private final CheckLoginStatusUseCase checkLoginStatusUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private UserInfoBean cachedUserInfo;

    private SessionStateRepository() {
        this.checkLoginStatusUseCase = UseCaseProvider.getCheckLoginStatusUseCase();
        this.getUserProfileUseCase = UseCaseProvider.getUserProfileUseCase();
    }

    public static SessionStateRepository getInstance() {
        return INSTANCE;
    }

    public Single<UserInfoBean> getCachedUserInfo() {
        return checkLoginStatusUseCase.execute()
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
        return getUserProfileUseCase.execute()
                .subscribeOn(Schedulers.io())
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
