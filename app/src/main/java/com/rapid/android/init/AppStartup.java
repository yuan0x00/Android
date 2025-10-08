package com.rapid.android.init;

import android.os.StrictMode;

import com.google.android.material.color.DynamicColors;
import com.rapid.android.BuildConfig;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.data.DataInitializer;
import com.rapid.android.core.data.network.AuthHeaderProvider;
import com.rapid.android.core.data.network.PersistentCookieStore;
import com.rapid.android.core.data.network.TokenRefreshHandlerImpl;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.network.NetManager;
import com.rapid.android.core.network.client.NetworkClient;
import com.rapid.android.core.network.client.NetworkConfig;
import com.rapid.android.core.network.interceptor.AuthInterceptor;
import com.rapid.android.network.proxy.DeveloperProxyManager;
import com.rapid.android.utils.ThemeManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class AppStartup {

    private final BaseApplication application;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "network-config-worker");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });
    private final NetworkConfig.AuthFailureListener authFailureHandler =
            () -> SessionManager.getInstance().forceLogout();
    private AuthInterceptor.TokenRefreshHandler tokenRefreshHandler;
    private DeveloperProxyManager.ProxySettingsListener proxyListener;

    AppStartup(BaseApplication application) {
        this.application = application;
    }

    void initialize() {
        applyTheme();
        enableStrictMode();
        DataInitializer.init();
        initNetwork();
    }

    void onTerminate() {
        DeveloperProxyManager.getInstance().removeListener(proxyListener);
        networkExecutor.shutdownNow();
    }

    private void applyTheme() {
        ThemeManager.applySavedTheme();
        DynamicColors.applyToActivitiesIfAvailable(application);
    }

    private void enableStrictMode() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectCustomSlowCalls()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build());
    }

    private void initNetwork() {
        tokenRefreshHandler = new TokenRefreshHandlerImpl();
        DeveloperProxyManager proxyManager = DeveloperProxyManager.getInstance();
        proxyListener = (settings, failureTriggered) -> {
            if (!failureTriggered) {
                scheduleNetworkRebuild();
            }
        };
        proxyManager.addListener(proxyListener);
        networkExecutor.execute(() -> {
            rebuildNetworkClient();
            SessionManager.getInstance().initialize();
        });
    }

    private void scheduleNetworkRebuild() {
        networkExecutor.execute(this::rebuildNetworkClient);
    }

    private void rebuildNetworkClient() {
        NetworkClient.configure(createNetworkConfig());
        NetManager.reset();
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
                .cookieStore(new PersistentCookieStore(application.getApplicationContext()))
                .proxySelector(proxyManager.getProxySelector())
                .build();
    }
}
