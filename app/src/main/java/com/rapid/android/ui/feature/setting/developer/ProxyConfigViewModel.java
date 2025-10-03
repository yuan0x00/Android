package com.rapid.android.ui.feature.setting.developer;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.core.ui.presentation.BaseViewModel;
import com.rapid.android.R;
import com.rapid.android.network.proxy.DeveloperProxyManager;
import com.rapid.android.network.proxy.ProxySettings;

public class ProxyConfigViewModel extends BaseViewModel implements DeveloperProxyManager.ProxySettingsListener {

    private final DeveloperProxyManager proxyManager;
    private final MutableLiveData<ProxySettings> settingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<FormError> formErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> messageResLiveData = new MutableLiveData<>();

    public ProxyConfigViewModel() {
        proxyManager = DeveloperProxyManager.getInstance();
        proxyManager.addListener(this);
        settingsLiveData.setValue(proxyManager.getSettings());
        formErrorLiveData.setValue(FormError.none());
    }

    LiveData<ProxySettings> getSettings() {
        return settingsLiveData;
    }

    LiveData<FormError> getFormError() {
        return formErrorLiveData;
    }

    LiveData<Integer> getMessageRes() {
        return messageResLiveData;
    }

    void refresh() {
        settingsLiveData.setValue(proxyManager.getSettings());
    }

    void saveProxyConfig(boolean enabled, @Nullable String hostInput, @Nullable String portInput, boolean autoDisable) {
        formErrorLiveData.setValue(FormError.none());

        String host = hostInput != null ? hostInput.trim() : "";
        int port = 0;

        if (enabled) {
            if (TextUtils.isEmpty(host)) {
                formErrorLiveData.setValue(FormError.host(R.string.proxy_config_error_host));
                messageResLiveData.setValue(R.string.proxy_config_error_host);
                return;
            }

            if (TextUtils.isEmpty(portInput)) {
                formErrorLiveData.setValue(FormError.port(R.string.proxy_config_error_port));
                messageResLiveData.setValue(R.string.proxy_config_error_port);
                return;
            }

            try {
                port = Integer.parseInt(portInput.trim());
            } catch (NumberFormatException ex) {
                formErrorLiveData.setValue(FormError.port(R.string.proxy_config_error_port));
                messageResLiveData.setValue(R.string.proxy_config_error_port);
                return;
            }

            if (port < 1 || port > 65535) {
                formErrorLiveData.setValue(FormError.port(R.string.proxy_config_error_port));
                messageResLiveData.setValue(R.string.proxy_config_error_port);
                return;
            }
        }

        ProxySettings current = proxyManager.getSettings();
        ProxySettings.Builder builder = current != null
                ? current.toBuilder()
                : ProxySettings.builder().lastFailure(0L, null);

        ProxySettings newSettings = builder
                .enabled(enabled)
                .host(host)
                .port(port)
                .autoDisableOnFailure(autoDisable)
                .build();

        ProxySettings saved = proxyManager.updateSettings(newSettings);
        settingsLiveData.setValue(saved);
        messageResLiveData.setValue(enabled ? R.string.proxy_config_saved : R.string.proxy_config_disabled);
    }

    @Override
    public void onProxySettingsChanged(@NonNull ProxySettings settings, boolean failureTriggered) {
        settingsLiveData.postValue(settings);
        if (failureTriggered) {
            messageResLiveData.postValue(R.string.proxy_config_degraded_notice);
        }
    }

    @Override
    protected void onCleared() {
        proxyManager.removeListener(this);
        super.onCleared();
    }

    static final class FormError {
        private final Integer hostErrorRes;
        private final Integer portErrorRes;

        private FormError(@Nullable Integer hostErrorRes, @Nullable Integer portErrorRes) {
            this.hostErrorRes = hostErrorRes;
            this.portErrorRes = portErrorRes;
        }

        static FormError none() {
            return new FormError(null, null);
        }

        static FormError host(int resId) {
            return new FormError(resId, null);
        }

        static FormError port(int resId) {
            return new FormError(null, resId);
        }

        @Nullable
        Integer getHostErrorRes() {
            return hostErrorRes;
        }

        @Nullable
        Integer getPortErrorRes() {
            return portErrorRes;
        }
    }
}
