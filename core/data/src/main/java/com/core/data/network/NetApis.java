package com.core.data.network;

import com.core.data.api.ContentApi;
import com.core.data.api.HomeApi;
import com.core.data.api.LoginApi;
import com.core.data.api.UserApi;
import com.core.network.NetManager;

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
