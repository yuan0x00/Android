package com.rapid.android.network.proxy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rapid.android.core.log.LogKit;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class DeveloperProxyManager {
    private static final Object LOCK = new Object();
    private static DeveloperProxyManager instance;

    private final ProxySettingsStore store;
    private final DeveloperProxySelector proxySelector;
    private final Set<ProxySettingsListener> listeners = new CopyOnWriteArraySet<>();

    private DeveloperProxyManager() {
        this.store = new ProxySettingsStore();
        ProxySettings initial = store.load();
        this.proxySelector = new DeveloperProxySelector(this, initial);
    }

    @NonNull
    public static DeveloperProxyManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DeveloperProxyManager();
                }
            }
        }
        return instance;
    }

    public DeveloperProxySelector getProxySelector() {
        return proxySelector;
    }

    @NonNull
    public ProxySettings getSettings() {
        return proxySelector.getSettings();
    }

    @NonNull
    public ProxySettings updateSettings(@NonNull ProxySettings newSettings) {
        ProxySettings persisted = store.save(newSettings);
        proxySelector.updateSettings(persisted);
        notifyListeners(persisted, false);
        return persisted;
    }

    void handleProxyFailure(@NonNull URI uri, @NonNull ProxySettings current, @NonNull IOException error) {
        ProxySettings updated = store.recordFailure(System.currentTimeMillis(), error.getMessage(), false);
        proxySelector.applyFailureUpdate(updated);
        LogKit.i("DeveloperProxy", "代理请求失败，已降级为直连: %s", uri);
        notifyListeners(updated, true);
    }

    public void addListener(@NonNull ProxySettingsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(@Nullable ProxySettingsListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(@NonNull ProxySettings settings, boolean failureTriggered) {
        for (ProxySettingsListener listener : listeners) {
            try {
                listener.onProxySettingsChanged(settings, failureTriggered);
            } catch (Exception ex) {
                // 忽略监听器异常避免影响整体流程
            }
        }
    }

    public interface ProxySettingsListener {
        void onProxySettingsChanged(@NonNull ProxySettings settings, boolean failureTriggered);
    }
}
