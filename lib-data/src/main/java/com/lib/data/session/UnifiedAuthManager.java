package com.lib.data.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.model.UserInfoBean;
import com.lib.domain.repository.UserRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 统一的身份验证管理器
 * 整合了原AuthSessionManager和SessionStateRepository的功能
 * 提供单一的状态源和统一的API
 */
public class UnifiedAuthManager {
    
    private static volatile UnifiedAuthManager instance;
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // 统一的状态管理 - 单一数据源
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.guest());
    
    // 从统一状态派生的公开状态
    private final MediatorLiveData<Boolean> loginState = new MediatorLiveData<>();
    private final MediatorLiveData<UserInfoBean> userInfo = new MediatorLiveData<>();
    private final MutableLiveData<AuthEvent> authEvents = new MutableLiveData<>();
    
    // 内存缓存
    private UserInfoBean cachedUserInfo;
    
    private UnifiedAuthManager() {
        this.userRepository = RepositoryProvider.getUserRepository();
        
        // 建立派生状态的依赖关系
        loginState.addSource(authState, state -> loginState.setValue(state.isLoggedIn()));
        userInfo.addSource(authState, state -> userInfo.setValue(state.getUserInfo()));
    }
    
    public static UnifiedAuthManager getInstance() {
        if (instance == null) {
            synchronized (UnifiedAuthManager.class) {
                if (instance == null) {
                    instance = new UnifiedAuthManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化认证管理器 - 应用启动时调用
     */
    public void initialize() {
        restore();
    }
    
    /**
     * 恢复认证状态 - 应用启动或进程重启时调用
     */
    public void restore() {
        disposables.add(
            userRepository.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    isLoggedIn -> {
                        if (Boolean.TRUE.equals(isLoggedIn)) {
                            // 用户已登录，设置登录状态并获取用户信息
                            setAuthState(AuthState.loggedIn(null));
                            
                            // 异步获取用户信息
                            refreshUserInfoInternal()
                                .subscribe(
                                    userInfo -> {
                                        // 成功获取用户信息，更新状态
                                        setAuthState(AuthState.loggedIn(userInfo));
                                    },
                                    throwable -> {
                                        // 获取用户信息失败，但保持登录状态
                                        // 用户信息为null，但登录状态保持true
                                        setAuthState(AuthState.loggedIn(null));
                                    }
                                );
                        } else {
                            // 用户未登录，设置为访客状态
                            setAuthState(AuthState.guest());
                        }
                    },
                    throwable -> {
                        // 检查登录状态失败，设置为访客状态
                        setAuthState(AuthState.guest());
                    }
                )
        );
    }
    
    /**
     * 用户登录成功后调用
     */
    public void onLoginSuccess(@Nullable UserInfoBean userInfo) {
        setAuthState(AuthState.loggedIn(userInfo));
        notifyAuthEvent(AuthEventType.LOGIN);
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        disposables.add(
            userRepository.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        // 无论网络登出是否成功，都清除本地状态
                        setAuthState(AuthState.guest());
                        notifyAuthEvent(AuthEventType.LOGOUT);
                    },
                    throwable -> {
                        // 即使登出失败，也清除本地状态
                        setAuthState(AuthState.guest());
                        notifyAuthEvent(AuthEventType.LOGOUT);
                    }
                )
        );
    }
    
    /**
     * 因认证失败被强制登出
     */
    public void forceLogout() {
        setAuthState(AuthState.guest());
        notifyAuthEvent(AuthEventType.UNAUTHORIZED);
    }
    
    /**
     * 刷新用户信息
     */
    public Single<UserInfoBean> refreshUserInfo() {
        return refreshUserInfoInternal();
    }
    
    /**
     * 内部方法：刷新用户信息
     */
    private Single<UserInfoBean> refreshUserInfoInternal() {
        return userRepository.fetchUserProfile()
            .map(this::extractUserInfo)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .firstOrError()
            .doOnSuccess(userInfoBean -> {
                // 更新缓存和状态
                cachedUserInfo = userInfoBean;
                // 仅在当前为登录状态时才更新用户信息
                AuthState currentState = this.authState.getValue();
                if (currentState != null && currentState.isLoggedIn()) {
                    setAuthState(AuthState.loggedIn(userInfoBean));
                }
            })
            .doOnError(throwable -> {
                // 获取用户信息失败时，保留登录状态，但用户信息为null
                AuthState currentState = this.authState.getValue();
                if (currentState != null && currentState.isLoggedIn()) {
                    setAuthState(AuthState.loggedIn(null));
                }
            });
    }
    
    /**
     * 获取已缓存的用户信息（同步）
     */
    public @Nullable UserInfoBean getCachedUserInfo() {
        AuthState currentState = authState.getValue();
        return currentState != null ? currentState.getUserInfo() : null;
    }
    
    /**
     * 获取已缓存的用户信息（异步）- 包含网络获取逻辑
     */
    public Single<UserInfoBean> getCachedOrRefreshUserInfo() {
        return userRepository.isLoggedIn()
            .subscribeOn(Schedulers.io())
            .flatMap(isLoggedIn -> {
                if (!Boolean.TRUE.equals(isLoggedIn)) {
                    setAuthState(AuthState.guest());
                    return Single.fromCallable(() -> (UserInfoBean) null);
                }
                
                // 检查内存缓存
                if (cachedUserInfo != null) {
                    return Single.just(cachedUserInfo);
                }
                
                // 检查当前状态中的用户信息
                AuthState currentState = authState.getValue();
                if (currentState != null && currentState.getUserInfo() != null) {
                    UserInfoBean stateUserInfo = currentState.getUserInfo();
                    cachedUserInfo = stateUserInfo;
                    return Single.just(stateUserInfo);
                }
                
                // 执行网络获取
                return refreshUserInfoInternal();
            })
            .observeOn(AndroidSchedulers.mainThread());
    }
    
    /**
     * 更新用户信息（如更新个人资料后）
     */
    public void updateUserInfo(@Nullable UserInfoBean userInfo) {
        AuthState currentState = authState.getValue();
        if (currentState != null && currentState.isLoggedIn()) {
            setAuthState(AuthState.loggedIn(userInfo));
        }
    }
    
    /**
     * 获取登录状态
     */
    public LiveData<Boolean> getLoginState() {
        return loginState;
    }
    
    /**
     * 获取用户信息
     */
    public LiveData<UserInfoBean> getUserInfo() {
        return userInfo;
    }
    
    /**
     * 获取认证事件
     */
    public LiveData<AuthEvent> getAuthEvents() {
        return authEvents;
    }
    
    /**
     * 手动设置登录状态（用于内部状态同步）
     */
    private void setAuthState(@NonNull AuthState state) {
        authState.postValue(state);
    }
    
    /**
     * 通知认证事件
     */
    private void notifyAuthEvent(@NonNull AuthEventType type) {
        authEvents.postValue(new AuthEvent(type));
    }
    
    /**
     * 提取用户信息的辅助方法
     */
    private @Nullable UserInfoBean extractUserInfo(DomainResult<UserInfoBean> result) {
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        }
        DomainError error = result != null ? result.getError() : null;
        String message = error != null ? error.getMessage() : "获取用户信息失败";
        // 不抛出异常，返回null，让调用方处理
        return null;
    }
    
    /**
     * 清理资源
     */
    public void dispose() {
        disposables.clear();
    }
    
    /**
     * 认证事件类型
     */
    public enum AuthEventType {
        LOGIN,
        LOGOUT,
        UNAUTHORIZED
    }
    
    /**
     * 认证状态类 - 统一管理登录状态和用户信息
     */
    public static class AuthState {
        private final boolean isLoggedIn;
        private final @Nullable UserInfoBean userInfo;

        private AuthState(boolean isLoggedIn, @Nullable UserInfoBean userInfo) {
            this.isLoggedIn = isLoggedIn;
            this.userInfo = userInfo;
        }

        public static AuthState guest() {
            return new AuthState(false, null);
        }

        public static AuthState loggedIn(@Nullable UserInfoBean userInfo) {
            return new AuthState(true, userInfo);
        }

        public boolean isLoggedIn() {
            return isLoggedIn;
        }

        public @Nullable UserInfoBean getUserInfo() {
            return userInfo;
        }
    }
    
    /**
     * 认证事件包装类
     */
    public static final class AuthEvent {
        private final AuthEventType type;
        
        public AuthEvent(AuthEventType type) {
            this.type = type;
        }
        
        public AuthEventType getType() {
            return type;
        }
    }
}