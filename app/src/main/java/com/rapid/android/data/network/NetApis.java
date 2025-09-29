package com.rapid.android.data.network;

import com.core.net.NetManager;
import com.rapid.android.data.api.ContentApi;
import com.rapid.android.data.api.HomeApi;
import com.rapid.android.data.api.LoginApi;
import com.rapid.android.data.api.UserApi;

public final class NetApis {

    public static void init() {
        NetApis.Login();
        NetApis.Home();
        NetApis.User();
        NetApis.Content();
    }

    public static LoginApi Login() {
        return NetManager.createNetApi(LoginApi.class);
    }

    public static HomeApi Home() {
        return NetManager.createNetApi(HomeApi.class);
    }

    public static UserApi User() {
        return NetManager.createNetApi(UserApi.class);
    }

    public static ContentApi Content() {
        return NetManager.createNetApi(ContentApi.class);
    }
}
