package com.rapid.android.data.session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.data.model.UserInfoBean;

public final class AuthSessionManager {

    private static final MutableLiveData<AuthEvent> authEvents = new MutableLiveData<>();
    private static final MutableLiveData<Boolean> loginState = new MutableLiveData<>(false);
    private static final MutableLiveData<UserInfoBean> userInfo = new MutableLiveData<>();
    private AuthSessionManager() {
    }

    public static LiveData<AuthEvent> authEvents() {
        return authEvents;
    }

    public static void notifyLogin() {
        loginState.postValue(true);
        authEvents.postValue(new AuthEvent(EventType.LOGIN));
    }

    public static void notifyLogout() {
        loginState.postValue(false);
        authEvents.postValue(new AuthEvent(EventType.LOGOUT));
    }

    public static void notifyUnauthorized() {
        loginState.postValue(false);
        authEvents.postValue(new AuthEvent(EventType.UNAUTHORIZED));
    }

    public static LiveData<Boolean> loginState() {
        return loginState;
    }

    public static void updateUserInfo(UserInfoBean infoBean) {
        userInfo.postValue(infoBean);
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
