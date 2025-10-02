package com.lib.data.session;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.lib.data.local.AuthStorage;
import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.model.LoginBean;
import com.lib.domain.model.UserInfoBean;
import com.lib.domain.repository.UserRepository;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 统一的会话管理器
 * 整合了原SimpleSessionManager和AuthSessionManager的功能
 * 提供单一的状态源和统一的API
 */
public class SessionManager {
    
    private static volatile SessionManager instance;
    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final AtomicBoolean fetchingProfile = new AtomicBoolean(false);
    
    // 简单的状态管理
    private final MutableLiveData<SessionState> sessionState = new MutableLiveData<>(SessionState.GUEST);
    // 公开的LiveData接口
    public final LiveData<SessionState> state = sessionState;
    
    // 桥接原AuthSessionManager的接口
    private final MediatorLiveData<Boolean> loginState = new MediatorLiveData<>();
    private final MediatorLiveData<UserInfoBean> userInfo = new MediatorLiveData<>();
    private final MediatorLiveData<AuthEvent> authEvents = new MediatorLiveData<>();
    
    private SessionManager() {
        this.userRepository = RepositoryProvider.getUserRepository();
        
        // 监听SimpleSessionManager的状态变化并适配到旧的API
        loginState.addSource(sessionState, sessionState -> {
            if (sessionState != null) {
                loginState.setValue(sessionState.isLoggedIn());
            }
        });
        
        userInfo.addSource(sessionState, sessionState -> {
            if (sessionState != null) {
                userInfo.setValue(sessionState.getUserInfo());
            }
        });
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
    
    // 获取当前用户信息
    public @Nullable UserInfoBean getCurrentUserInfo() {
        SessionState state = sessionState.getValue();
        return state != null ? state.getUserInfo() : null;
    }
    
    // 初始化 - 应用启动时调用
    public void initialize() {
        disposables.add(
            userRepository.isLoggedIn()
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
                    throwable -> {
                        sessionState.postValue(SessionState.GUEST);
                    }
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
        AuthStorage authStorage = AuthStorage.getInstance();
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

    // 以下是桥接原AuthSessionManager的API
    public LiveData<AuthEvent> authEvents() {
        return authEvents;
    }

    public LiveData<Boolean> loginState() {
        return loginState;
    }

    public LiveData<UserInfoBean> userInfo() {
        return userInfo;
    }

    // 通知登录 - 兼容旧API
    public void notifyLogin() {
        // 从当前状态中获取用户信息，如果有的话
        UserInfoBean currentUserInfo = getCurrentUserInfo();
        if (currentUserInfo != null) {
            onLoginSuccess(currentUserInfo.getUserInfo());
        } else {
            onLoginSuccess(null);
        }
    }

    // 通知登出 - 兼容旧API  
    public void notifyLogout() {
        forceLogout();
    }

    // 更新用户信息
    public void updateUserInfo(@Nullable UserInfoBean userInfo) {
        SessionState currentState = sessionState.getValue();
        if (currentState != null && currentState.isLoggedIn()) {
            sessionState.postValue(SessionState.loggedIn(userInfo));
        }
    }

    public enum EventType {
        LOGIN,
        LOGOUT,
        UNAUTHORIZED
    }

    public static final class AuthEvent {
        private final EventType type;

        public AuthEvent(EventType type) {
            this.type = type;
        }

        public EventType getType() {
            return type;
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
            
            return TextUtils.isEmpty(nickname) ? "用户" : nickname;
        }
    }
}
