package com.example.android;

import android.os.StrictMode;

import com.example.android.api.NetApis;
import com.example.android.router.LoginServiceImpl;
import com.example.core.base.BaseApplication;
import com.example.router.RouterService;

public class MainApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        enableStrictMode();
        initNetWork();
        initRouter();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) { // 仅在 debug 构建中启用
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()              // 检测主线程读文件
                    .detectDiskWrites()             // 检测主线程写文件
                    .detectCustomSlowCalls()        // 检测自定义慢方法
//                     .detectNetwork()             // 暂时关闭网络监控
                    .penaltyLog()                   // 记录日志
//                    .penaltyDeath()                 // 崩溃定位问题
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()   // 检测 SQLite 数据库游标未关闭
                    .detectLeakedClosableObjects()  // 检测可关闭对象未释放
                    .detectActivityLeaks()          // 检测 Activity 泄漏
//                     .detectUriExposures()        // 检测 URI 权限泄露（包含网络监控）
                    .penaltyLog()                   // 记录日志
//                    .penaltyDeath()                 // 崩溃定位问题
                    .build());
        }
    }

    private void initNetWork() {
//        NetApiManager.setRetrofit();
        NetApis.init();
    }

    private void initRouter() {
        // 注册服务实现
        RouterService.loginService = new LoginServiceImpl();
        // 检查是否都注册了
        RouterService.init();
    }
}
