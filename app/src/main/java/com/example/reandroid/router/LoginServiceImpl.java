package com.example.reandroid.router;

import android.content.Context;

import com.example.router.service.ILoginService;

public class LoginServiceImpl implements ILoginService {
    @Override
    public boolean isLogin() {
        return false;
    }

    @Override
    public void login(Context context, LoginCallback callback) {

    }
}
