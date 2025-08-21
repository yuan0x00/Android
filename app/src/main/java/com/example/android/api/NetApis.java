package com.example.android.api;

import com.example.core.network.NetApiManager;

public final class NetApis {

    public static void init() {
        NetApis.Login();
        NetApis.Main();
    }

    public static MainApi Main() {
        return NetApiManager.createNetApi(MainApi.class);
    }

    public static LoginApi Login() {
        return NetApiManager.createNetApi(LoginApi.class);
    }
}
