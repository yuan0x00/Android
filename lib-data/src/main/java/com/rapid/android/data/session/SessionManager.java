package com.rapid.android.data.session;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.domain.model.UserInfoBean;
import com.rapid.android.domain.repository.UserRepository;
import com.rapid.android.domain.result.DomainError;
import com.rapid.android.domain.result.DomainResult;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 统一的会话管理器
 * 解决原AuthSessionManager和SessionStateRepository分离导致的状态不一致问题
 */
public class SessionManager {
    
    private static volatile SessionManager instance;
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // 单一状态源 - 会话状态
    private final MutableLiveData<SessionState> sessionState = new MutableLiveData<>(SessionState.guest());
    
    // 组合状态 - 从单一状态源派生
    private final MediatorLiveData<Boolean> loginState = new MediatorLiveData<>();
    private final MediatorLiveData<UserInfoBean> userInfo = new MediatorLiveData<>();
    
    private SessionManager() {
        this.userRepository = RepositoryProvider.getUserRepository();
        
        // 从sessionState派生loginState和userInfo
        loginState.addSource(sessionState, state -> loginState.setValue(state.isLoggedIn()));
        userInfo.addSource(sessionState, state -> userInfo.setValue(state.getUserInfo()));
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
    
    /**
     * 初始化会话管理器 - 应用启动时调用
     */
    public void initialize() {
        disposables.add(
            userRepository.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    isLoggedIn -> {
                        if (Boolean.TRUE.equals(isLoggedIn)) {
                            // 用户已登录，尝试获取用户信息
                            refreshUserInfoInternal();
                        } else {
                            // 用户未登录，设置为访客状态
                            setSessionState(SessionState.guest());
                        }
                    },
                    throwable -> {
                        // 检查登录状态失败，设置为访客状态
                        setSessionState(SessionState.guest());
                    }
                )
        );
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
     * 获取完整的会话状态
     */
    public LiveData<SessionState> getSessionState() {
        return this.sessionState;
    }
    
    /**
     * 设置会话状态
     */
    private void setSessionState(@NonNull SessionState state) {
        this.sessionState.postValue(state);
    }
    
    /**
     * 用户登录成功后调用
     */
    public void onLoginSuccess() {
        setSessionState(SessionState.loggedIn(null)); // 登录成功，但用户信息待获取
        refreshUserInfoInternal(); // 自动获取用户信息
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
                        setSessionState(SessionState.guest());
                    },
                    throwable -> {
                        // 即使登出失败，也清除本地状态
                        setSessionState(SessionState.guest());
                    }
                )
        );
    }
    
    /**
     * 用户被强制登出（如token过期）
     */
    public void forceLogout() {
        setSessionState(SessionState.guest());
    }
    
    /**
     * 刷新用户信息
     */
    public void refreshUserInfo() {
        refreshUserInfoInternal();
    }
    
    /**
     * 内部方法：刷新用户信息
     */
    private void refreshUserInfoInternal() {
        disposables.add(
            userRepository.fetchUserProfile()
                .map(this::extractUserInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .firstOrError()
                .subscribe(
                    userInfoBean -> {
                        // 更新会话状态
                        SessionState currentState = this.sessionState.getValue();
                        if (currentState != null && currentState.isLoggedIn()) {
                            setSessionState(SessionState.loggedIn(userInfoBean));
                        }
                    },
                    throwable -> {
                        // 获取用户信息失败，但保持登录状态
                        // 仅更新用户信息为null，不改变登录状态
                        SessionState currentState = this.sessionState.getValue();
                        if (currentState != null && currentState.isLoggedIn()) {
                            setSessionState(SessionState.loggedIn(null));
                        }
                    }
                )
        );
    }
    
    /**
     * 更新用户信息（例如在更新个人资料后）
     */
    public void updateUserInfo(@Nullable UserInfoBean userInfoBean) {
        SessionState currentState = this.sessionState.getValue();
        if (currentState != null && currentState.isLoggedIn()) {
            setSessionState(SessionState.loggedIn(userInfoBean));
        }
    }
    
    /**
     * 清理资源
     */
    public void dispose() {
        disposables.clear();
    }
    
    /**
     * 提取用户信息辅助方法
     */
    private @Nullable UserInfoBean extractUserInfo(DomainResult<UserInfoBean> result) {
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        }
        DomainError error = result != null ? result.getError() : null;
        String message = error != null ? error.getMessage() : "获取用户信息失败";
        // 不抛出异常，返回null让用户信息为null，但保持登录状态
        return null;
    }
    
    /**
     * 会话状态类 - 包含登录状态和用户信息
     */
    public static class SessionState {
        private final boolean isLoggedIn;
        private final @Nullable UserInfoBean userInfo;

        private SessionState(boolean isLoggedIn, @Nullable UserInfoBean userInfo) {
            this.isLoggedIn = isLoggedIn;
            this.userInfo = userInfo;
        }

        public static SessionState guest() {
            return new SessionState(false, null);
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