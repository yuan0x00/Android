package com.rapid.android.network.proxy;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.core.common.app.BaseApplication;

final class ProxySettingsStore {

    private static final String PREF_NAME = "developer_proxy_settings";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_HOST = "host";
    private static final String KEY_PORT = "port";
    private static final String KEY_AUTO_DISABLE = "auto_disable";
    private static final String KEY_LAST_FAILURE_AT = "last_failure_at";
    private static final String KEY_LAST_FAILURE_REASON = "last_failure_reason";

    private final SharedPreferences preferences;

    ProxySettingsStore() {
        Context context = BaseApplication.getAppContext();
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    synchronized ProxySettings load() {
        boolean enabled = preferences.getBoolean(KEY_ENABLED, false);
        String host = preferences.getString(KEY_HOST, "");
        int port = preferences.getInt(KEY_PORT, 0);
        boolean autoDisable = preferences.getBoolean(KEY_AUTO_DISABLE, true);
        long lastFailureAt = preferences.getLong(KEY_LAST_FAILURE_AT, 0L);
        String failureReason = preferences.getString(KEY_LAST_FAILURE_REASON, null);
        return ProxySettings.builder()
                .enabled(enabled)
                .host(host)
                .port(port)
                .autoDisableOnFailure(autoDisable)
                .lastFailure(lastFailureAt, failureReason)
                .build();
    }

    @NonNull
    synchronized ProxySettings save(@NonNull ProxySettings settings) {
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean(KEY_ENABLED, settings.isEnabled())
                .putString(KEY_HOST, settings.getHost())
                .putInt(KEY_PORT, settings.getPort())
                .putBoolean(KEY_AUTO_DISABLE, settings.isAutoDisableOnFailure())
                .putLong(KEY_LAST_FAILURE_AT, settings.getLastFailureTimestamp())
                .putString(KEY_LAST_FAILURE_REASON, settings.getLastFailureReason());
        editor.apply();
        return settings;
    }

    @NonNull
    synchronized ProxySettings recordFailure(long timestamp, @Nullable String reason, boolean disableProxy) {
        ProxySettings current = load();
        ProxySettings.Builder builder = current.toBuilder()
                .lastFailure(timestamp, reason);
        if (disableProxy) {
            builder.enabled(false);
        }
        ProxySettings updated = builder.build();
        save(updated);
        return updated;
    }
}
