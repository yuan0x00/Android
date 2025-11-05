package com.rapid.android.core.data.session;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.core.data.repository.RepositoryProvider;
import com.rapid.android.core.domain.model.LoginBean;
import com.rapid.android.core.domain.model.UserInfoBean;
import com.rapid.android.core.domain.repository.UserRepository;
import com.rapid.android.core.storage.AuthStorage;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SessionManager {

    private static volatile SessionManager instance;
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final AtomicBoolean fetchingProfile = new AtomicBoolean(false);
    private final MutableLiveData<SessionState> sessionState = new MutableLiveData<>(SessionState.GUEST);
    public final LiveData<SessionState> state = sessionState;
    private final AuthStorage authStorage;

    private SessionManager() {
        authStorage = AuthStorage.getInstance();
        userRepository = RepositoryProvider.getUserRepository();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    // 获取当前会话状态
    public SessionState getCurrentState() {
        return sessionState.getValue();
    }

    // 检查是否已登录
    public boolean isLoggedIn() {
        SessionState state = sessionState.getValue();
        return state != null && state.isLoggedIn();
    }

    // 初始化 - 应用启动时调用
    public void initialize() {
        disposables.add(
                authStorage.isLoggedIn()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isLoggedIn -> {
                                    if (Boolean.TRUE.equals(isLoggedIn)) {
                                        // 已登录，获取用户信息
                                        sessionState.postValue(createCachedSessionState());
                                        refreshUserInfoInternal();
                                    } else {
                                        // 未登录，设置为访客状态
                                        sessionState.postValue(SessionState.GUEST);
                                    }
                                },
                                throwable -> sessionState.postValue(SessionState.GUEST)
                        )
        );
    }

    // 登录成功后调用
    public void onLoginSuccess(@Nullable LoginBean loginData) {
        UserInfoBean userInfo = null;
        if (loginData != null) {
            userInfo = new UserInfoBean(loginData, null);
        }
        SessionState newState = userInfo != null ?
                SessionState.loggedIn(userInfo) :
                SessionState.loggedIn(null);
        sessionState.postValue(newState);
    }

    // 登出
    public void logout() {
        disposables.add(
                userRepository.logout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    clearActiveRequests();
                                    sessionState.postValue(SessionState.GUEST);
                                },
                                throwable -> {
                                    clearActiveRequests();
                                    sessionState.postValue(SessionState.GUEST);
                                }
                        )
        );
    }

    // 强制登出（如token过期）
    public void forceLogout() {
        clearActiveRequests();
        sessionState.postValue(SessionState.GUEST);
    }

    // 刷新用户信息
    public void refreshUserInfo() {
        if (isLoggedIn()) {
            refreshUserInfoInternal();
        }
    }

    // 内部方法：刷新用户信息
    private void refreshUserInfoInternal() {
        if (!fetchingProfile.compareAndSet(false, true)) {
            return;
        }
        disposables.add(
                userRepository.fetchUserProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .firstOrError()
                        .subscribe(
                                result -> {
                                    fetchingProfile.set(false);
                                    if (result != null && result.isSuccess() && result.getData() != null) {
                                        SessionState currentState = sessionState.getValue();
                                        if (currentState != null && currentState.isLoggedIn()) {
                                            sessionState.postValue(SessionState.loggedIn(result.getData()));
                                        }
                                    }
                                    // 如果获取失败，保持当前状态，不改变登录状态
                                },
                                throwable -> {
                                    fetchingProfile.set(false);
                                    // 保持当前状态，不改变登录状态，只用户信息为null
                                    SessionState currentState = sessionState.getValue();
                                    if (currentState != null && currentState.isLoggedIn()) {
                                        sessionState.postValue(SessionState.loggedIn(null));
                                    }
                                }
                        )
        );
    }

    // 清理资源
    public void dispose() {
        clearActiveRequests();
    }

    private void clearActiveRequests() {
        disposables.clear();
        fetchingProfile.set(false);
    }

    private SessionState createCachedSessionState() {
        String username = authStorage.peekUsername();
        String userId = authStorage.peekUserId();
        String token = authStorage.peekToken();

        LoginBean cachedLogin = new LoginBean();
        cachedLogin.setUsername(username != null ? username : "");
        if (userId != null) {
            try {
                cachedLogin.setId(Integer.parseInt(userId));
            } catch (NumberFormatException ignored) {
                cachedLogin.setId(0);
            }
        }
        cachedLogin.setToken(token != null ? token : "");

        return SessionState.loggedIn(new UserInfoBean(cachedLogin, null));
    }

    // 更新用户信息
    public void updateUserInfo(@Nullable UserInfoBean userInfo) {
        SessionState currentState = sessionState.getValue();
        if (currentState != null && currentState.isLoggedIn()) {
            sessionState.postValue(SessionState.loggedIn(userInfo));
        }
    }

    // 会话状态类
    public static class SessionState {
        public static final SessionState GUEST = new SessionState(false, null);

        private final boolean isLoggedIn;
        private final @Nullable UserInfoBean userInfo;

        private SessionState(boolean isLoggedIn, @Nullable UserInfoBean userInfo) {
            this.isLoggedIn = isLoggedIn;
            this.userInfo = userInfo;
        }

        public static SessionState loggedIn(@Nullable UserInfoBean userInfo) {
            return new SessionState(true, userInfo);
        }

        public boolean isLoggedIn() {
            return isLoggedIn;
        }

        public @Nullable UserInfoBean getUserInfo() {
            return userInfo;
        }

    }
}
