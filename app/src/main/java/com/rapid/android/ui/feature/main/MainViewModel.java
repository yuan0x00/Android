package com.rapid.android.ui.feature.main;

import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.lib.data.repository.RepositoryProvider;
import com.lib.data.session.AuthSessionManager;
import com.lib.domain.model.LoginBean;
import com.lib.domain.repository.UserRepository;
import com.lib.domain.result.DomainError;
import com.lib.domain.result.DomainResult;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final UserRepository userRepository;

    public MainViewModel() {
        this.userRepository = RepositoryProvider.getUserRepository();
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshLoginState() {
        autoDispose(
                userRepository.isLoggedIn()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isLogged -> {
                                    if (isLogged != null && isLogged) {
                                        // 使用简化的会话管理器
                                        com.lib.data.session.SimpleSessionManager.getInstance().refreshUserInfo();
                                    } else {
                                        attemptReLogin();
                                    }
                                }, throwable -> {
                                    com.lib.data.session.SimpleSessionManager.getInstance().forceLogout();
                                    errorMessage.setValue(throwable.getMessage());
                                }
                        )
        );
    }

    private void attemptReLogin() {
        autoDispose(
                userRepository.reLogin()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleReLoginResult, this::handleReLoginError)
        );
    }

    private void handleReLoginResult(DomainResult<LoginBean> result) {
        if (result != null && result.isSuccess() && result.getData() != null) {
            AuthSessionManager.notifyLogin();
            errorMessage.setValue(null);
        } else {
            AuthSessionManager.notifyLogout();
            DomainError error = result != null ? result.getError() : null;
            errorMessage.setValue(error != null ? error.getMessage() : "自动登录失败");
        }
    }

    private void handleReLoginError(Throwable throwable) {
        AuthSessionManager.notifyLogout();
        errorMessage.setValue(throwable.getMessage());
    }
}
