package com.example.reandroid.ui.page.main;

import androidx.lifecycle.MutableLiveData;

import com.example.network.NetWork;
import com.example.reandroid.bean.BaseResponse;
import com.example.reandroid.bean.LoginBean;
import com.example.reandroid.network.RetrofitApi;
import com.example.reandroid.ui.base.BaseViewModel;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

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
        Observable<BaseResponse<LoginBean>> observable = NetWork.createService(RetrofitApi.class).login(username, password);
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
