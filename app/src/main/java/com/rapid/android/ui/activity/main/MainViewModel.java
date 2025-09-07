package com.rapid.android.ui.activity.main;

import androidx.lifecycle.MutableLiveData;

import com.rapid.android.api.NetApis;
import com.rapid.android.bean.LoginBean;
import com.rapid.core.base.vm.BaseViewModel;
import com.rapid.core.net.base.BaseResponse;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainViewModel extends BaseViewModel {
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MutableLiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // 登录方法
    public void login(String username, String password) {
        Observable<BaseResponse<LoginBean>> observable = NetApis.Login().login(username, password);
        autoDispose(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<>() {

                    @Override
                    public void onNext(BaseResponse<LoginBean> loginBeanBaseResponse) {
                        if (loginBeanBaseResponse.getData() != null) {
                            loginSuccess.setValue(true);  // 登录成功
                        } else {
                            errorMessage.setValue("登陆失败");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorMessage.setValue(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}
