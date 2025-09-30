package com.rapid.android.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.presentation.viewmodel.BaseViewModel;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.data.repository.user.UserRepository;
import com.rapid.android.data.session.AuthSessionManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingViewModel extends BaseViewModel {

    private final UserRepository userRepository = RepositoryProvider.getUserRepository();

    private final MutableLiveData<Boolean> darkMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> autoUpdate = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> notifications = new MutableLiveData<>(true);
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LiveData<Boolean> getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean enabled) {
        darkMode.setValue(enabled);
        saveSetting("dark_mode", enabled);
        // 这里可以触发主题切换
        applyTheme(enabled);
    }

    public LiveData<Boolean> getAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean enabled) {
        autoUpdate.setValue(enabled);
        saveSetting("auto_update", enabled);
    }

    public LiveData<Boolean> getNotifications() {
        return notifications;
    }

    public void setNotifications(boolean enabled) {
        notifications.setValue(enabled);
        saveSetting("notifications", enabled);
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

    // 私有方法
    private void saveSetting(String key, boolean value) {
        // 这里应该保存到SharedPreferences或其他存储方式
        // 示例：SharedPreferences.Editor editor = preferences.edit();
        // editor.putBoolean(key, value);
        // editor.apply();
    }

    private void applyTheme(boolean isDarkMode) {
        // 这里可以应用主题切换逻辑
        // 例如：AppCompatDelegate.setDefaultNightMode(isDarkMode ? 
        //       AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // 初始化设置
    public void loadSettings() {
        // 从SharedPreferences加载设置
        // 示例：
        // SharedPreferences preferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        // autoLogin.setValue(preferences.getBoolean("auto_login", false));
        // biometricLogin.setValue(preferences.getBoolean("biometric_login", false));
        // ... 其他设置
    }
    
    public void logout() {
        autoDispose(
                userRepository.logout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    operationMessage.setValue("登出成功");
                                    AuthSessionManager.notifyLogout();
                                },
                                error -> operationMessage.setValue("登出失败: " + error.getMessage())
                        )
        );
    }
}
