package com.rapid.android.init;

import android.os.StrictMode;

import com.google.android.material.color.DynamicColors;
import com.rapid.android.BuildConfig;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.crash.GlobalCrashHandler;
import com.rapid.android.core.data.DataInitializer;
import com.rapid.android.core.data.network.AuthHeaderProvider;
import com.rapid.android.core.data.network.PersistentCookieStore;
import com.rapid.android.core.data.network.TokenRefreshHandlerImpl;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.log.LogKit;
import com.rapid.android.core.network.NetManager;
import com.rapid.android.core.network.client.NetworkClient;
import com.rapid.android.core.network.client.NetworkConfig;
import com.rapid.android.core.network.interceptor.AuthInterceptor;
import com.rapid.android.network.proxy.DeveloperProxyManager;
import com.rapid.android.utils.ThemeManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class AppStartup {

    private static final String TAG = "AppStartup";

    private final BaseApplication application;
    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "network-config-worker");
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });
    private final NetworkConfig.AuthFailureListener authFailureHandler =
            () -> SessionManager.getInstance().forceLogout();
    private final AtomicBoolean rebuildPending = new AtomicBoolean(false);
    private AuthInterceptor.TokenRefreshHandler tokenRefreshHandler;
    private DeveloperProxyManager.ProxySettingsListener proxyListener;

    AppStartup(BaseApplication application) {
        this.application = application;
    }

    void initialize() {
        applyTheme();
        enableStrictMode();
        GlobalCrashHandler.setCrashReporter(new AppCrashReporter());
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
                scheduleNetworkRebuild("proxy updated");
            }
        };
        proxyManager.addListener(proxyListener);
        networkExecutor.execute(() -> {
            rebuildNetworkClient("app start");
            SessionManager.getInstance().initialize();
        });
    }

    private void scheduleNetworkRebuild(String reason) {
        if (!rebuildPending.compareAndSet(false, true)) {
            LogKit.d(TAG, "Skip network rebuild (%s): already pending", reason);
            return;
        }
        networkExecutor.execute(() -> rebuildNetworkClient(reason));
    }

    private void rebuildNetworkClient(String reason) {
        try {
            LogKit.i(TAG, "Rebuilding network client (%s)", reason);
            NetworkClient.configure(createNetworkConfig());
            NetManager.reset();
        } finally {
            rebuildPending.set(false);
        }
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
                .cookieStore(resolveCookieStore())
                .proxySelector(proxyManager.getProxySelector())
                .build();
    }

    private NetworkConfig.CookieStore resolveCookieStore() {
        PersistentCookieStore existing = PersistentCookieStore.getInstance();
        if (existing != null) {
            return existing;
        }
        return new PersistentCookieStore(application.getApplicationContext());
    }
}
