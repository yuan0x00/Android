package com.rapid.android.feature.developer;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rapid.android.R;
import com.rapid.android.core.network.client.NetworkClientManager;
import com.rapid.android.core.storage.PreferenceHelper;
import com.rapid.android.core.ui.presentation.BaseViewModel;

import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;

public class DeveloperViewModel extends BaseViewModel {

    private final MutableLiveData<FormError> formErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> messageResLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> proxyEnable = new MutableLiveData<>();
    private final MutableLiveData<String> host = new MutableLiveData<>();
    private final MutableLiveData<Integer> port = new MutableLiveData<>();

    public DeveloperViewModel() {
        proxyEnable.setValue(getPref().getBoolean("developer_proxy_enable", false));
        host.setValue(getPref().getString("developer_host", "192.168.1.1"));
        port.setValue(getPref().getInt("developer_port", 8080));
    }

    private PreferenceHelper getPref() {
        return PreferenceHelper.getDefault();
    }

    LiveData<FormError> getFormError() {
        return formErrorLiveData;
    }

    LiveData<Integer> getMessageRes() {
        return messageResLiveData;
    }

    MutableLiveData<String> getHost() {
        return host;
    }

    MutableLiveData<Integer> getPort() {
        return port;
    }
    MutableLiveData<Boolean> getProxyEnable() {
        return proxyEnable;
    }

    void saveProxyConfig(boolean enabled, @Nullable String hostInput, @Nullable String portInput) {

        String host = hostInput != null ? hostInput.trim() : "";
        int port = 0;

        if (enabled) {
            if (TextUtils.isEmpty(host)) {
                messageResLiveData.setValue(R.string.proxy_config_error_host);
                return;
            }

            if (TextUtils.isEmpty(portInput)) {
                messageResLiveData.setValue(R.string.proxy_config_error_port);
                return;
            }

            try {
                port = Integer.parseInt(portInput.trim());
            } catch (NumberFormatException ex) {
                messageResLiveData.setValue(R.string.proxy_config_error_port);
                return;
            }

            if (port < 1 || port > 65535) {
                messageResLiveData.setValue(R.string.proxy_config_error_port);
                return;
            }
        }

        OkHttpClient.Builder okHttpBuilder = NetworkClientManager.getDefaultClient().getCurrentOkHttpBuilder();

        if (enabled) {
            // 使用 OkHttp 内置的 Proxy 配置
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            okHttpBuilder.proxy(proxy);

        } else {
            // 禁用代理：设置为 NO_PROXY 或 null
            okHttpBuilder.proxy(Proxy.NO_PROXY);
        }

        NetworkClientManager.reInitializeDefaultClient(
                "https://www.wanandroid.com",
                okHttpBuilder,
                NetworkClientManager.getDefaultClient().getCurrentRetrofitBuilder()
        );

        PreferenceHelper.getDefault().putString("developer_host", host);
        PreferenceHelper.getDefault().putInt("developer_port", port);
        PreferenceHelper.getDefault().putBoolean("developer_proxy_enable", enabled);

        messageResLiveData.setValue(enabled ? R.string.proxy_config_saved : R.string.proxy_config_disabled);
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
