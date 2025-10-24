package com.rapid.android.init.tasks;

import com.rapid.android.BuildConfig;
import com.rapid.android.core.common.app.BaseApplication;
import com.rapid.android.core.common.app.init.AsyncTask;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkTask extends AsyncTask {

    private static final String TAG = "NetworkTask";

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

    @Override
    public String getName() {
        return "Network";
    }

    @Override
    public List<String> getDependencies() {
        return List.of("AuthStorage");
    }

    @Override
    public void execute() throws Exception {
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
        return new PersistentCookieStore(BaseApplication.getAppContext());
    }
}
