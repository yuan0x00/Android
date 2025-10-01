package com.rapid.android.data.session;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.domain.model.UserInfoBean;
import com.rapid.android.domain.repository.UserRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 简化的会话管理器
 * 单一职责，简单清晰，解决复杂性问题
 */
public class SimpleSessionManager {
    
    private static volatile SimpleSessionManager instance;
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    
    // 简单的状态管理
    private final MutableLiveData<SessionState> sessionState = new MutableLiveData<>(SessionState.GUEST);
    // 公开的LiveData接口
    public final LiveData<SessionState> state = sessionState;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = loading;
    
    private SimpleSessionManager() {
        this.userRepository = RepositoryProvider.getUserRepository();
    }
    
    public static SimpleSessionManager getInstance() {
        if (instance == null) {
            synchronized (SimpleSessionManager.class) {
                if (instance == null) {
                    instance = new SimpleSessionManager();
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
    
    // 获取当前用户信息
    public @Nullable UserInfoBean getCurrentUserInfo() {
        SessionState state = sessionState.getValue();
        return state != null ? state.getUserInfo() : null;
    }
    
    // 初始化 - 应用启动时调用
    public void initialize() {
        loading.postValue(true);
        
        disposables.add(
            userRepository.isLoggedIn()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    isLoggedIn -> {
                        loading.postValue(false);
                        if (Boolean.TRUE.equals(isLoggedIn)) {
                            // 已登录，获取用户信息
                            refreshUserInfoInternal();
                        } else {
                            // 未登录，设置为访客状态
                            sessionState.postValue(SessionState.GUEST);
                        }
                    },
                    throwable -> {
                        loading.postValue(false);
                        sessionState.postValue(SessionState.GUEST);
                    }
                )
        );
    }
    
    // 登录成功后调用
    public void onLoginSuccess(@Nullable UserInfoBean userInfo) {
        SessionState newState = userInfo != null ? 
            SessionState.loggedIn(userInfo) : 
            SessionState.loggedIn(null);
        sessionState.postValue(newState);
    }
    
    // 登出
    public void logout() {
        loading.postValue(true);
        
        disposables.add(
            userRepository.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    result -> {
                        loading.postValue(false);
                        sessionState.postValue(SessionState.GUEST);
                    },
                    throwable -> {
                        loading.postValue(false);
                        sessionState.postValue(SessionState.GUEST);
                    }
                )
        );
    }
    
    // 强制登出（如token过期）
    public void forceLogout() {
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
        if (loading.getValue() == Boolean.TRUE) return; // 防止重复刷新
        
        loading.postValue(true);
        
        disposables.add(
            userRepository.fetchUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .firstOrError()
                .subscribe(
                    result -> {
                        loading.postValue(false);
                        if (result != null && result.isSuccess() && result.getData() != null) {
                            SessionState currentState = sessionState.getValue();
                            if (currentState != null && currentState.isLoggedIn()) {
                                sessionState.postValue(SessionState.loggedIn(result.getData()));
                            }
                        }
                        // 如果获取失败，保持当前状态，不改变登录状态
                    },
                    throwable -> {
                        loading.postValue(false);
                        // 保持当前状态，不改变登录状态，只用户信息为null
                        SessionState currentState = sessionState.getValue();
                        if (currentState != null && currentState.isLoggedIn()) {
                            sessionState.postValue(SessionState.loggedIn(null));
                        }
                    }
                )
        );
    }
    
    // 主动更新用户信息
    public void updateUserInfo(@Nullable UserInfoBean userInfo) {
        SessionState currentState = sessionState.getValue();
        if (currentState != null && currentState.isLoggedIn()) {
            sessionState.postValue(SessionState.loggedIn(userInfo));
        }
    }
    
    // 清理资源
    public void dispose() {
        disposables.clear();
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
        
        public String getDisplayName() {
            if (!isLoggedIn || userInfo == null) {
                return "未登录用户";
            }
            
            // 从用户信息中提取显示名称
            String nickname = userInfo.getUserInfo() != null ? 
                userInfo.getUserInfo().getNickname() : null;
            if (TextUtils.isEmpty(nickname)) {
                nickname = userInfo.getUserInfo() != null ? 
                    userInfo.getUserInfo().getPublicName() : null;
            }
            if (TextUtils.isEmpty(nickname)) {
                nickname = userInfo.getUserInfo() != null ? 
                    userInfo.getUserInfo().getUsername() : null;
            }
            
            return TextUtils.isEmpty(nickname) ? "锤友" : nickname;
        }
    }
}