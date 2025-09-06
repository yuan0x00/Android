package com.example.android.api;

import com.example.core.net.NetManager;

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
