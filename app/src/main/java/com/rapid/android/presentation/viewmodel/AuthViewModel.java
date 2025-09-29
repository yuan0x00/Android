package com.rapid.android.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.base.vm.BaseViewModel;
import com.rapid.android.domain.usecase.CheckLoginStatusUseCase;
import com.rapid.android.domain.usecase.LoginUserUseCase;
import com.rapid.android.domain.usecase.UseCaseProvider;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthViewModel extends BaseViewModel {
    private final CheckLoginStatusUseCase checkLoginStatusUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public AuthViewModel() {
        this.checkLoginStatusUseCase = UseCaseProvider.getCheckLoginStatusUseCase();
        this.loginUserUseCase = UseCaseProvider.getLoginUserUseCase();
    }

    public LiveData<Boolean> checkLoginStatus() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        
        autoDispose(
                checkLoginStatusUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result::setValue,
                                throwable -> {
                                    result.setValue(false);
                                    errorMessage.setValue(throwable.getMessage());
                                }
                        )
        );
        
        return result;
    }

    public void reLogin() {
        // 这先检查是否已登录，如果已登录则无需操作
        autoDispose(
                checkLoginStatusUseCase.execute()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isLoggedIn -> {
                                    if (!isLoggedIn) {
                                        // 从本地存储获取用户名和密码信息进行重新登录
                                        // 实际实现需结合UserLocalDataSource
                                    }
                                },
                                throwable -> errorMessage.setValue(throwable.getMessage())
                        )
        );
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
