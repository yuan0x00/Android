package com.rapid.android.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.base.vm.BaseViewModel;
import com.rapid.android.data.session.AuthSessionManager;
import com.rapid.android.domain.usecase.LoginUserUseCase;
import com.rapid.android.domain.usecase.UseCaseProvider;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {
    private final LoginUserUseCase loginUserUseCase;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel() {
        this.loginUserUseCase = UseCaseProvider.getLoginUserUseCase();
    }

    public void login(String username, String password) {
        autoDispose(
                loginUserUseCase.execute(username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    boolean success = response != null && response.getData() != null;
                                    loginSuccess.setValue(success);
                                    if (success) {
                                        AuthSessionManager.notifyLogin();
                                        errorMessage.setValue(null);
                                    } else {
                                        errorMessage.setValue("登录失败");
                                    }
                                },
                                throwable -> errorMessage.setValue(throwable.getMessage())
                        )
        );
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
