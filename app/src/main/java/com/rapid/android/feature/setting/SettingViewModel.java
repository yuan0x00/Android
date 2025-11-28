package com.rapid.android.feature.setting;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.permission.NotificationPermissionManager;
import com.rapid.android.core.storage.AppPreferencesStorage;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.store.ThemeStore;

public class SettingViewModel extends BaseViewModel {

    private final MutableLiveData<ThemeStore.ThemeMode> themeMode = new MutableLiveData<>(ThemeStore.getSavedThemeMode());
    private final MutableLiveData<Boolean> notifications = new MutableLiveData<>(AppPreferencesStorage.isNotificationsEnabled());
    private final MutableLiveData<Boolean> homeTopEnabled = new MutableLiveData<>(AppPreferencesStorage.isHomeTopEnabled());
    private final MutableLiveData<Boolean> noImageMode = new MutableLiveData<>(AppPreferencesStorage.isNoImageModeEnabled());
    private final MutableLiveData<Boolean> autoHideBottomBar = new MutableLiveData<>(AppPreferencesStorage.isAutoHideBottomBarEnabled());
    private final MutableLiveData<Integer> operationMessageRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<ThemeStore.ThemeMode> getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(ThemeStore.ThemeMode mode) {
        ThemeStore.applyThemeMode(mode);
        AppPreferencesStorage.setThemeModeValue(mode.getValue());
        themeMode.setValue(mode);
    }

    public LiveData<Boolean> getNotifications() {
        return notifications;
    }

    public void setNotifications(boolean enabled) {
        Context context = BaseApplication.getAppContext();
        boolean hasPermission = NotificationPermissionManager.isGranted(context);

        // 只有在有权限的情况下才能真正启用通知
        if (enabled && !hasPermission) {
            // 权限未授予，不更新状态
            notifications.setValue(false);
            operationMessageRes.setValue(R.string.setting_notification_permission_denied);
            return;
        }

        notifications.setValue(enabled);
        AppPreferencesStorage.setNotificationsEnabled(enabled);

        // 记录用户的通知偏好（即使权限未授予，也记录用户意图）
        AppPreferencesStorage.setNotificationPreferenceRequested(enabled);

        operationMessageRes.setValue(enabled
                ? R.string.setting_notifications_on
                : R.string.setting_notifications_off);
    }

    public LiveData<Boolean> getHomeTopEnabled() {
        return homeTopEnabled;
    }

    public void setHomeTopEnabled(boolean enabled) {
        homeTopEnabled.setValue(enabled);
        AppPreferencesStorage.setHomeTopEnabled(enabled);
    }

    public LiveData<Boolean> getNoImageMode() {
        return noImageMode;
    }

    public void setNoImageMode(boolean enabled) {
        noImageMode.setValue(enabled);
        AppPreferencesStorage.setNoImageModeEnabled(enabled);
        operationMessageRes.setValue(enabled
                ? R.string.settings_no_image_mode_on
                : R.string.settings_no_image_mode_off);
    }

    public LiveData<Boolean> getAutoHideBottomBarEnabled() {
        return autoHideBottomBar;
    }

    public void setAutoHideBottomBarEnabled(boolean enabled) {
        autoHideBottomBar.setValue(enabled);
        AppPreferencesStorage.setAutoHideBottomBarEnabled(enabled);
        operationMessageRes.setValue(enabled
                ? R.string.settings_auto_hide_bottom_bar_on
                : R.string.settings_auto_hide_bottom_bar_off);
    }

    public LiveData<Integer> getOperationMessageRes() {
        return operationMessageRes;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void logoutWithCallback(LogoutCallback callback) {
        isLoading.setValue(true);
        operationMessageRes.setValue(null);
        // 使用统一的会话管理器来处理登出
        SessionManager.getInstance().logout();
        isLoading.setValue(false);
        if (callback != null) {
            callback.onLogoutComplete(true, R.string.setting_logout_success);
        }
    }

    public interface LogoutCallback {
        void onLogoutComplete(boolean success, @StringRes int messageRes);
    }
}
