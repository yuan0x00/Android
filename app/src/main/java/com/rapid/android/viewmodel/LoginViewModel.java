package com.rapid.android.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.auth.AuthManager;
import com.rapid.android.data.model.LoginBean;
import com.rapid.core.base.vm.BaseViewModel;
import com.rapid.core.net.base.BaseResponse;

import io.reactivex.rxjava3.observers.DisposableObserver;

public class LoginViewModel extends BaseViewModel {
    private final AuthManager authManager;
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel() {
        authManager = AuthManager.getInstance();
    }

    public void login(String username, String password) {
        authManager.login(username, password, new DisposableObserver<BaseResponse<LoginBean>>() {
            @Override
            public void onNext(BaseResponse<LoginBean> response) {
                loginSuccess.setValue(response.getData() != null);
                errorMessage.setValue(response.getData() == null ? "登录失败" : null);
            }

            @Override
            public void onError(Throwable e) {
                errorMessage.setValue(e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public void relogin() {
        authManager.relogin(new DisposableObserver<BaseResponse<LoginBean>>() {
            @Override
            public void onNext(BaseResponse<LoginBean> response) {
                loginSuccess.setValue(response.getData() != null);
                errorMessage.setValue(response.getData() == null ? "重新登录失败" : null);
            }

            @Override
            public void onError(Throwable e) {
                errorMessage.setValue(e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public boolean isLoggedIn() {
        return authManager.isLoggedIn();
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
