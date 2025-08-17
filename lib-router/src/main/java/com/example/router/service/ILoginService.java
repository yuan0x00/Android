package com.example.router.service;

import android.content.Context;

public interface ILoginService {
    boolean isLogin();

    void login(Context context, LoginCallback callback);

    interface LoginCallback {
        void onSuccess();

        void onError(String msg);
    }
}
