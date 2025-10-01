package com.rapid.android.data.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.rapid.android.domain.model.UserInfoBean;

/**
 * 简化的AuthSessionManager（桥接层）
 * 保持原API接口，内部使用SimpleSessionManager
 */
public final class AuthSessionManager {

    private static final SimpleSessionManager simpleManager = SimpleSessionManager.getInstance();
    
    // 桥接到SimpleSessionManager的状态
    private static final MediatorLiveData<Boolean> loginState = new MediatorLiveData<>();
    private static final MediatorLiveData<UserInfoBean> userInfo = new MediatorLiveData<>();
    private static final MediatorLiveData<AuthEvent> authEvents = new MediatorLiveData<>();

    static {
        // 监听SimpleSessionManager的状态变化并适配到旧的API
        loginState.addSource(simpleManager.state, sessionState -> {
            if (sessionState != null) {
                loginState.setValue(sessionState.isLoggedIn());
            }
        });
        
        userInfo.addSource(simpleManager.state, sessionState -> {
            if (sessionState != null) {
                userInfo.setValue(sessionState.getUserInfo());
            }
        });
    }

    public static LiveData<AuthEvent> authEvents() {
        return authEvents;
    }

    public static void notifyLogin() {
        // 从SimpleSessionManager获取当前用户信息，如果有的话
        UserInfoBean currentUserInfo = simpleManager.getCurrentUserInfo();
        simpleManager.onLoginSuccess(currentUserInfo);
    }

    public static void notifyLogout() {
        simpleManager.logout();
    }

    public static void notifyUnauthorized() {
        simpleManager.forceLogout();
    }

    public static LiveData<Boolean> loginState() {
        return loginState;
    }

    public static void updateUserInfo(UserInfoBean infoBean) {
        simpleManager.updateUserInfo(infoBean);
    }

    public static LiveData<UserInfoBean> userInfo() {
        return userInfo;
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
}