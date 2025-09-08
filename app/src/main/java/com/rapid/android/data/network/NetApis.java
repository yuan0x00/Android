package com.rapid.android.data.network;

import com.rapid.android.data.api.LoginApi;
import com.rapid.android.data.api.MainApi;
import com.rapid.core.net.NetManager;

public final class NetApis {

    public static void init() {
        NetApis.Login();
        NetApis.Main();
    }

    public static MainApi Main() {
        return NetManager.createNetApi(MainApi.class);
    }

    public static LoginApi Login() {
        return NetManager.createNetApi(LoginApi.class);
    }
}
