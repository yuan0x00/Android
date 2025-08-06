package com.example.reandroid.ui.page.main;

import androidx.lifecycle.MutableLiveData;

import com.example.reandroid.bean.BaseResponse;
import com.example.reandroid.bean.LoginBean;
import com.example.reandroid.net.ApiClient;
import com.example.reandroid.ui.base.BaseViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends BaseViewModel {
    // LiveData 暴露给 UI
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // 提供给 UI 观察的结果
    public MutableLiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // 登录方法
    public void login(String username, String password) {
        Call<BaseResponse<LoginBean>> call = ApiClient.getApiService().login(username, password);
        call.enqueue(new Callback<BaseResponse<LoginBean>>() {
            @Override
            public void onResponse(Call<BaseResponse<LoginBean>> call, Response<BaseResponse<LoginBean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<LoginBean> body = response.body();
                    if (body.getData() != null) {
                        loginSuccess.setValue(true);  // 登录成功
                    } else {
                        errorMessage.setValue(body.getErrorMsg()); // 业务错误
                    }
                } else {
                    errorMessage.setValue("请求失败，状态码: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<LoginBean>> call, Throwable t) {
                errorMessage.setValue("网络错误: " + t.getMessage());
            }
        });
    }
}
