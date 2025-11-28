package com.rapid.android.init.tasks;

import com.rapid.android.core.initializer.Task;
import com.rapid.android.core.initializer.TaskType;
import com.rapid.android.core.network.client.NetworkClient;
import com.rapid.android.core.network.client.NetworkClientManager;
import com.rapid.android.core.storage.PreferenceHelper;
import com.rapid.android.network.cookie.MyCookieJar;
import com.rapid.android.network.interceptor.TokenInterceptor;

import java.net.InetSocketAddress;
import java.net.Proxy;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class NetworkTask extends Task {

    @Override
    public TaskType getTaskType() {
        return TaskType.BLOCKING;
    }

    @Override
    public void run() {
        OkHttpClient.Builder okHttpBuilder = NetworkClient.getDefaultOkHttpBuilder();
        Retrofit.Builder retrofitBuilder = NetworkClient.getDefaultRetrofitBuilder();

        PreferenceHelper preferenceHelper = PreferenceHelper.getDefault();

        boolean enabled = preferenceHelper.getBoolean("developer_proxy_enable", false);
        String host = preferenceHelper.getString("developer_host", "");
        int port = preferenceHelper.getInt("developer_port", 0);

        if (enabled) {
            // 使用 OkHttp 内置的 Proxy 配置
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            okHttpBuilder.proxy(proxy);

        } else {
            // 禁用代理：设置为 NO_PROXY 或 null
            okHttpBuilder.proxy(Proxy.NO_PROXY);
        }

        okHttpBuilder.cookieJar(new MyCookieJar());
        okHttpBuilder.addInterceptor(new TokenInterceptor());

        NetworkClientManager.reInitializeDefaultClient(
                "https://www.wanandroid.com",
                okHttpBuilder,
                retrofitBuilder
        );
    }
}
