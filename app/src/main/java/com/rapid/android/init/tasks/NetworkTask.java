package com.rapid.android.init.tasks;

import com.rapid.android.BuildConfig;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.tasks.StorageTask;
import com.rapid.android.core.data.network.AuthHeaderProvider;
import com.rapid.android.core.data.network.PersistentCookieStore;
import com.rapid.android.core.data.network.TokenRefreshHandlerImpl;
import com.rapid.android.core.data.session.SessionManager;
import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;
import com.rapid.android.core.log.LogKit;
import com.rapid.android.core.network.NetManager;
import com.rapid.android.core.network.client.NetworkClient;
import com.rapid.android.core.network.client.NetworkConfig;
import com.rapid.android.core.network.interceptor.AuthInterceptor;
import com.rapid.android.network.proxy.DeveloperProxyManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkTask extends Task {

    private static final String TAG = "NetworkTask";
    private final NetworkConfig.AuthFailureListener authFailureHandler =
            () -> SessionManager.getInstance().forceLogout();
    private final AtomicBoolean rebuildPending = new AtomicBoolean(false);
    private AuthInterceptor.TokenRefreshHandler tokenRefreshHandler;
    private DeveloperProxyManager.ProxySettingsListener proxyListener;

    @Override
    public List<Class<? extends Task>> getDependencies() {
        return List.of(StorageTask.class);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        initNetwork();
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
        rebuildNetworkClient("app start");
        SessionManager.getInstance().initialize();
    }

    private void scheduleNetworkRebuild(String reason) {
        if (!rebuildPending.compareAndSet(false, true)) {
            LogKit.d(TAG, "Skip network rebuild (%s): already pending", reason);
            return;
        }
        rebuildNetworkClient(reason);
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
        return new PersistentCookieStore(BaseApplication.getAppContext());
    }
}
