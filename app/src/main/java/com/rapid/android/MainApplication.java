package com.rapid.android;

import android.os.StrictMode;

import com.core.common.app.BaseApplication;
import com.core.network.client.NetworkClient;
import com.core.network.client.NetworkConfig;
import com.lib.data.DataInitializer;
import com.lib.data.network.NetApis;

public class MainApplication extends BaseApplication {

    private final com.core.network.client.NetworkConfig.AuthFailureListener authFailureHandler =
            () -> com.lib.data.session.SessionManager.getInstance().forceLogout();

    @Override
    public void onCreate() {
        super.onCreate();

        enableStrictMode();
        // 初始化数据层
        DataInitializer.init();
        initNetWork();
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
        NetworkConfig config = NetworkConfig.builder()
                .baseUrl("https://www.wanandroid.com/")
                .enableLogging(BuildConfig.DEBUG)
                .allowInsecureSsl(BuildConfig.DEBUG)
                .crashReportEndpointProvider(() -> "https://www.wanandroid.com/app/crash/upload")
                .authFailureListener(authFailureHandler)
                .headerProvider(new com.lib.data.network.AuthHeaderProvider())
                .cookieStore(new com.lib.data.network.PersistentCookieStore(getApplicationContext()))
                .build();
        NetworkClient.configure(config);
        NetApis.init();

        // 初始化统一的会话管理器
        com.lib.data.session.SessionManager.getInstance().initialize();
    }

}
