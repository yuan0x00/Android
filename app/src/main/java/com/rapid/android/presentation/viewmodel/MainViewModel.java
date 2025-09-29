package com.rapid.android.presentation.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.core.base.vm.BaseViewModel;
import com.core.net.base.BaseResponse;
import com.rapid.android.data.model.LoginBean;
import com.rapid.android.data.session.AuthSessionManager;
import com.rapid.android.domain.usecase.CheckLoginStatusUseCase;
import com.rapid.android.domain.usecase.ReLoginUseCase;
import com.rapid.android.domain.usecase.UseCaseProvider;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainViewModel extends BaseViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final CheckLoginStatusUseCase checkLoginStatusUseCase;
    private final ReLoginUseCase reLoginUseCase;

    public MainViewModel() {
        this.checkLoginStatusUseCase = UseCaseProvider.getCheckLoginStatusUseCase();
        this.reLoginUseCase = UseCaseProvider.getReLoginUseCase();
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshLoginState() {
        autoDispose(
                checkLoginStatusUseCase.execute()
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
                reLoginUseCase.execute()
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
