package com.example.reandroid;

import com.example.core.base.BaseApplication;
import com.example.reandroid.router.LoginServiceImpl;
import com.example.router.RouterService;

public class MainApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // 注册服务实现
        RouterService.loginService = new LoginServiceImpl();
        // 检查是否都注册了
        RouterService.init();
    }
}
