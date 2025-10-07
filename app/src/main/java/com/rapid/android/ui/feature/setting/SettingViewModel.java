package com.rapid.android.ui.feature.setting;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.ui.presentation.BaseViewModel;
import com.rapid.android.utils.AppPreferences;
import com.rapid.android.utils.ThemeManager;

public class SettingViewModel extends BaseViewModel {

    private final MutableLiveData<ThemeManager.ThemeMode> themeMode = new MutableLiveData<>(ThemeManager.ThemeMode.SYSTEM);
    private final MutableLiveData<Boolean> notifications = new MutableLiveData<>(AppPreferences.isNotificationsEnabled());
    private final MutableLiveData<Integer> operationMessageRes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<ThemeManager.ThemeMode> getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(ThemeManager.ThemeMode mode) {
        themeMode.setValue(mode);
        ThemeManager.applyThemeMode(mode);
    }

    public LiveData<Boolean> getNotifications() {
        return notifications;
    }

    public void setNotifications(boolean enabled) {
        notifications.setValue(enabled);
        AppPreferences.setNotificationsEnabled(enabled);
        operationMessageRes.setValue(enabled
                ? R.string.setting_notifications_on
                : R.string.setting_notifications_off);
    }

    public LiveData<Integer> getOperationMessageRes() {
        return operationMessageRes;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void clearCache() {
        isLoading.setValue(true);
        // 模拟清除缓存操作
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                operationMessageRes.postValue(R.string.setting_clear_cache_success);
            } catch (InterruptedException e) {
                operationMessageRes.postValue(R.string.setting_clear_cache_failed);
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    // 初始化设置
    public void loadSettings() {
        ThemeManager.ThemeMode saved = ThemeManager.getSavedThemeMode();
        themeMode.setValue(saved);
        ThemeManager.applyThemeMode(saved);
        notifications.setValue(AppPreferences.isNotificationsEnabled());
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
