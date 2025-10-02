package com.rapid.android.ui.feature.setting;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.domain.repository.UserRepository;
import com.rapid.android.utils.ThemeManager;

public class SettingViewModel extends BaseViewModel {

    private final UserRepository userRepository = RepositoryProvider.getUserRepository();

    private final MutableLiveData<ThemeManager.ThemeMode> themeMode = new MutableLiveData<>(ThemeManager.ThemeMode.SYSTEM);
    private final MutableLiveData<Boolean> autoUpdate = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notifications = new MutableLiveData<>(true);
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<ThemeManager.ThemeMode> getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(ThemeManager.ThemeMode mode) {
        themeMode.setValue(mode);
        ThemeManager.applyThemeMode(mode);
    }

    public LiveData<Boolean> getAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean enabled) {
        autoUpdate.setValue(enabled);
    }

    public LiveData<Boolean> getNotifications() {
        return notifications;
    }

    public void setNotifications(boolean enabled) {
        notifications.setValue(enabled);
    }

    public LiveData<String> getOperationMessage() {
        return operationMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void clearCache() {
        isLoading.setValue(true);
        // 模拟清除缓存操作
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟网络请求
                operationMessage.postValue("缓存清除成功");
            } catch (InterruptedException e) {
                operationMessage.postValue("缓存清除失败");
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
    }
    
    public void logoutWithCallback(LogoutCallback callback) {
        isLoading.setValue(true);
        operationMessage.setValue(null);
        // 使用统一的会话管理器来处理登出
        com.lib.data.session.SessionManager.getInstance().logout();
        operationMessage.setValue("登出成功");
        isLoading.setValue(false);
        if (callback != null) {
            callback.onLogoutComplete(true, "登出成功");
        }
    }
    
    public interface LogoutCallback {
        void onLogoutComplete(boolean success, String message);
    }
}
