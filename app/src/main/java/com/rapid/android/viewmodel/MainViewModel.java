package com.rapid.android.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.core.network.base.BaseResponse;
import com.core.presentation.viewmodel.BaseViewModel;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.repository.RepositoryProvider;
import com.rapid.android.data.repository.user.UserRepository;
import com.rapid.android.data.session.AuthSessionManager;

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
                                        AuthSessionManager.notifyLogin();
                                    } else {
                                        attemptReLogin();
                                    }
                                }, throwable -> {
                                    AuthSessionManager.notifyLogout();
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
                        .subscribe(this::handleReLoginSuccess, this::handleReLoginError)
        );
    }

    private void handleReLoginSuccess(BaseResponse<LoginBean> response) {
        if (response != null && response.isSuccess() && response.getData() != null) {
            AuthSessionManager.notifyLogin();
            errorMessage.setValue(null);
        } else {
            AuthSessionManager.notifyLogout();
            errorMessage.setValue(response != null ? response.getErrorMsg() : "自动登录失败");
        }
    }

    private void handleReLoginError(Throwable throwable) {
        AuthSessionManager.notifyLogout();
        errorMessage.setValue(throwable.getMessage());
    }
}
