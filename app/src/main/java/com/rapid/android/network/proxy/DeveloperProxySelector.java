package com.rapid.android.network.proxy;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class DeveloperProxySelector extends ProxySelector {

    private static final List<Proxy> DIRECT = Collections.singletonList(Proxy.NO_PROXY);

    private final DeveloperProxyManager manager;
    private final AtomicBoolean failureHandled = new AtomicBoolean(false);
    private volatile ProxySettings settings;

    DeveloperProxySelector(DeveloperProxyManager manager, ProxySettings initialSettings) {
        this.manager = manager;
        this.settings = initialSettings;
    }

    void updateSettings(@NonNull ProxySettings settings) {
        this.settings = settings;
        failureHandled.set(false);
    }

    ProxySettings getSettings() {
        return settings;
    }

    void applyFailureUpdate(@NonNull ProxySettings updatedSettings) {
        this.settings = updatedSettings;
    }

    @Override
    public List<Proxy> select(URI uri) {
        failureHandled.set(false);
        ProxySettings current = settings;
        if (current != null && current.isConfigured()) {
            com.rapid.android.core.log.LogKit.d("DeveloperProxy", "select proxy for %s -> %s:%d",
                    String.valueOf(uri), current.getHost(), current.getPort());
            InetSocketAddress address = InetSocketAddress.createUnresolved(
                    current.getHost(),
                    current.getPort());
            return java.util.Arrays.asList(new Proxy(Proxy.Type.HTTP, address), Proxy.NO_PROXY);
        }
        com.rapid.android.core.log.LogKit.d("DeveloperProxy", "select direct for %s (proxy disabled)", String.valueOf(uri));
        return DIRECT;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        ProxySettings current = settings;
        if (current == null || !current.isEnabled()) {
            return;
        }
        if (failureHandled.compareAndSet(false, true)) {
            com.rapid.android.core.log.LogKit.w("DeveloperProxy", ioe,
                    "connectFailed uri=%s proxy=%s:%d", String.valueOf(uri), current.getHost(), current.getPort());
            manager.handleProxyFailure(uri, current, ioe);
        }
    }
}
