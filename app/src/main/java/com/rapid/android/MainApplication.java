package com.rapid.android;

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import com.core.common.app.BaseApplication;
import com.core.data.DataInitializer;
import com.core.data.network.AuthHeaderProvider;
import com.core.data.network.NetApis;
import com.core.data.network.PersistentCookieStore;
import com.core.data.network.TokenRefreshHandlerImpl;
import com.core.data.session.SessionManager;
import com.core.network.NetManager;
import com.core.network.client.NetworkClient;
import com.core.network.client.NetworkConfig;
import com.core.network.interceptor.AuthInterceptor;
import com.google.android.material.color.DynamicColors;
import com.rapid.android.network.proxy.DeveloperProxyManager;
import com.rapid.android.utils.ThemeManager;

public class MainApplication extends BaseApplication {

    private final NetworkConfig.AuthFailureListener authFailureHandler =
            () -> SessionManager.getInstance().forceLogout();
    private AuthInterceptor.TokenRefreshHandler tokenRefreshHandler;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final DeveloperProxyManager.ProxySettingsListener proxyListener =
            (settings, failureTriggered) -> {
                if (!failureTriggered) {
                    scheduleNetworkRebuild();
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        ThemeManager.applySavedTheme();
        DynamicColors.applyToActivitiesIfAvailable(this);

        enableStrictMode();
        // 初始化数据层
        DataInitializer.init();
        initNetwork();
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

    private void initNetwork() {
        tokenRefreshHandler = new TokenRefreshHandlerImpl();
        DeveloperProxyManager proxyManager = DeveloperProxyManager.getInstance();
        proxyManager.addListener(proxyListener);

        rebuildNetworkClient();
        NetApis.init();

        // 初始化统一的会话管理器
        SessionManager.getInstance().initialize();
    }

    private void scheduleNetworkRebuild() {
        mainHandler.post(this::rebuildNetworkClient);
    }

    private void rebuildNetworkClient() {
        NetworkConfig config = createNetworkConfig();
        NetworkClient.configure(config);
        NetManager.reset();
        NetApis.init();
    }

    private NetworkConfig createNetworkConfig() {
        DeveloperProxyManager proxyManager = DeveloperProxyManager.getInstance();
        return NetworkConfig.builder()
                .baseUrl("https://www.wanandroid.com/")
                .enableLogging(BuildConfig.DEBUG)
                .allowInsecureSsl(BuildConfig.DEBUG)
                .crashReportEndpointProvider(() -> "https://www.wanandroid.com/app/crash/upload")
                .authFailureListener(authFailureHandler)
                .businessUnauthorizedCodes(-1001)
                .tokenRefreshHandler(tokenRefreshHandler)
                .headerProvider(new AuthHeaderProvider())
                .cookieStore(new PersistentCookieStore(getApplicationContext()))
                .proxySelector(proxyManager.getProxySelector())
                .build();
    }

    @Override
    public void onTerminate() {
        DeveloperProxyManager.getInstance().removeListener(proxyListener);
        super.onTerminate();
    }

}
